package com.payment.service;

import com.payment.exception.DuplicatePaymentException;
import com.payment.kafka.Pain001Producer;
import com.payment.model.Pain001Message;
import com.payment.model.Payment;
import com.payment.model.Payment.PaymentStatus;
import com.payment.model.PaymentStatusMessage;
import com.payment.model.PaymentRequest;
import com.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private Pain001MessageBuilder pain001Builder;

    @Autowired
    private Pain001Producer pain001Producer;

    /**
     * On startup, recover payments that were left in a transient in-flight state
     * (PROCESSING, VALIDATED, STORED, KAFKA_SENT) due to a previous service crash or
     * forced restart. These orphaned records can never advance normally, so we mark
     * them RJCT with a descriptive reason.
     */
    @PostConstruct
    public void recoverOrphanedPayments() {
        List<PaymentStatus> orphanStatuses = List.of(
                PaymentStatus.PROCESSING,
                PaymentStatus.VALIDATED,
                PaymentStatus.STORED,
                PaymentStatus.KAFKA_SENT
        );
        List<Payment> orphans = paymentRepository.findByStatusIn(orphanStatuses);
        if (orphans.isEmpty()) {
            log.info("[RECOVERY] No orphaned payments found on startup");
            return;
        }
        log.warn("[RECOVERY] Found {} orphaned payment(s) in transient states — marking as RJCT", orphans.size());
        for (Payment orphan : orphans) {
            log.warn("[RECOVERY] Orphaned payment | paymentId={} | stuck status={} | createdAt={}",
                    orphan.getPaymentId(), orphan.getStatus(), orphan.getCreatedAt());
            orphan.setStatus(PaymentStatus.RJCT);
            orphan.setPain002StatusReason("Payment interrupted: service restarted while payment was in status " + orphan.getStatus());
            orphan.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(orphan);
        }
        log.warn("[RECOVERY] Orphan recovery complete — {} payment(s) set to RJCT", orphans.size());
    }

    public Payment processPayment(PaymentRequest request) {
        String paymentId = request.getPaymentId();

        log.info("[PAYMENT] ══════════════════════════════════════════════════");
        log.info("[PAYMENT] New payment initiation request received");
        log.info("[PAYMENT] paymentId={} | debtor={} | creditor={} | amount={} {}",
                paymentId, request.getDebtorName(), request.getCreditorName(),
                request.getAmount(), request.getCurrency());

        // ── Step 1: Duplicate check ─────────────────────────────────────────
        log.info("[PAYMENT] STEP 1 - Checking for duplicate | paymentId={}", paymentId);
        Optional<Payment> existing = paymentRepository.findByPaymentId(paymentId);
        if (existing.isPresent()) {
            Payment dup = existing.get();
            log.warn("[PAYMENT] DUPLICATE REJECTED | paymentId={} | originalStatus={} | originalCreatedAt={}",
                    paymentId, dup.getStatus(), dup.getCreatedAt());
            // Do not create a new record — original record already persisted with its final status
            throw new DuplicatePaymentException(paymentId);
        }
        log.info("[PAYMENT] STEP 1 - No duplicate found, proceeding | paymentId={}", paymentId);

        // ── Step 2: Create and persist initial record ────────────────────────
        log.info("[PAYMENT] STEP 2 - Creating payment record | paymentId={}", paymentId);
        Payment payment = mapRequestToPayment(request);
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setCreatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);
        log.info("[PAYMENT] STEP 2 - Payment record created | paymentId={} | databaseId={}",
                paymentId, savedPayment.getId());

        // ── Step 3: Business validation ──────────────────────────────────────
        log.info("[PAYMENT] STEP 3 - Running business validation | paymentId={}", paymentId);
        try {
            validatePayment(request);
            savedPayment.setStatus(PaymentStatus.VALIDATED);
            paymentRepository.save(savedPayment);
            log.info("[PAYMENT] STEP 3 - Validation passed | paymentId={}", paymentId);
        } catch (Exception e) {
            savedPayment.setStatus(PaymentStatus.RJCT);
            savedPayment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(savedPayment);
            log.error("[PAYMENT] STEP 3 - Validation FAILED | paymentId={} | error={}", paymentId, e.getMessage());
            throw e;
        }

        // ── Step 4: Persist validated record ────────────────────────────────
        log.info("[PAYMENT] STEP 4 - Persisting validated payment | paymentId={}", paymentId);
        savedPayment.setStatus(PaymentStatus.STORED);
        savedPayment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(savedPayment);
        log.info("[PAYMENT] STEP 4 - Payment persisted in MongoDB | paymentId={} | databaseId={}",
                paymentId, savedPayment.getId());

        // ── Step 5: Build PAIN 001 ISO message ──────────────────────────────
        log.info("[PAYMENT] STEP 5 - Building PAIN 001 v9 ISO message | paymentId={}", paymentId);
        Pain001Message pain001Message;
        try {
            pain001Message = pain001Builder.buildPain001Message(savedPayment);
            log.info("[PAYMENT] STEP 5 - PAIN 001 message built | paymentId={} | isoMessageId={}",
                    paymentId, pain001Message.getMessageId());
        } catch (Exception e) {
            savedPayment.setStatus(PaymentStatus.RJCT);
            savedPayment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(savedPayment);
            log.error("[PAYMENT] STEP 5 - ISO message build FAILED | paymentId={} | error={}", paymentId, e.getMessage());
            throw e;
        }

        // ── Step 6: Publish to Kafka (wait for broker ACK) ──────────────────
        log.info("[PAYMENT] STEP 6 - Publishing to Kafka | paymentId={}", paymentId);
        savedPayment.setStatus(PaymentStatus.KAFKA_SENT);
        savedPayment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(savedPayment);

        String kafkaMessageId;
        try {
            kafkaMessageId = pain001Producer.publishPain001Message(pain001Message);
        } catch (Exception e) {
            savedPayment.setStatus(PaymentStatus.RJCT);
            savedPayment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(savedPayment);
            log.error("[PAYMENT] STEP 6 - Kafka publish FAILED | paymentId={} | error={}", paymentId, e.getMessage());
            throw e;
        }

        // ── Step 7: Kafka ACK confirmed — update status to RCVD ─────────────
        log.info("[PAYMENT] STEP 7 - Kafka broker ACK confirmed | paymentId={} | kafkaMessageId={}",
                paymentId, kafkaMessageId);
        savedPayment.setKafkaMessageId(kafkaMessageId);
        savedPayment.setStatus(PaymentStatus.RCVD);
        savedPayment.setUpdatedAt(LocalDateTime.now());
        Payment finalPayment = paymentRepository.save(savedPayment);

        log.info("[PAYMENT] ✅ Payment successfully processed and received by Kafka");
        log.info("[PAYMENT] paymentId={} | databaseId={} | kafkaMessageId={} | finalStatus={}",
                paymentId, finalPayment.getId(), kafkaMessageId, finalPayment.getStatus());
        log.info("[PAYMENT] ══════════════════════════════════════════════════");

        return finalPayment;
    }

    public Optional<Payment> getPaymentStatus(String paymentId) {
        log.info("[PAYMENT] Status query | paymentId={}", paymentId);
        Optional<Payment> result = paymentRepository.findFirstByPaymentIdOrderByCreatedAtDesc(paymentId);
        result.ifPresentOrElse(
                p -> log.info("[PAYMENT] Status found | paymentId={} | status={}", paymentId, p.getStatus()),
                () -> log.warn("[PAYMENT] Status not found | paymentId={}", paymentId)
        );
        return result;
    }

        public Map<String, Object> getDashboardData(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100));
        List<Payment> payments = paymentRepository.findAll(Sort.by(
            Sort.Order.desc("updatedAt"),
            Sort.Order.desc("createdAt")
        ));

        List<Payment> recentPayments = payments.stream()
            .limit(safeLimit)
            .toList();

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (Payment.PaymentStatus status : Payment.PaymentStatus.values()) {
            long count = payments.stream()
                .filter(payment -> payment.getStatus() == status)
                .count();
            if (count > 0) {
            statusCounts.put(status.name(), count);
            }
        }

        long receivedCount = statusCounts.getOrDefault("RCVD", 0L);
        receivedCount += statusCounts.getOrDefault("RECEIVED", 0L);

        long rejectedCount = statusCounts.getOrDefault("RJCT", 0L)
            + statusCounts.getOrDefault("CANC", 0L)
            + statusCounts.getOrDefault("FAILED", 0L);
        long completedCount = statusCounts.getOrDefault("ACSC", 0L);
        long pendingCount = statusCounts.getOrDefault("PDNG", 0L)
            + statusCounts.getOrDefault("ACTC", 0L)
            + statusCounts.getOrDefault("ACCP", 0L)
            + statusCounts.getOrDefault("ACSP", 0L)
            + statusCounts.getOrDefault("RGTC", 0L)
            + statusCounts.getOrDefault("SCHD", 0L)
            + statusCounts.getOrDefault("PROCESSING", 0L)
            + statusCounts.getOrDefault("VALIDATED", 0L)
            + statusCounts.getOrDefault("STORED", 0L)
            + statusCounts.getOrDefault("KAFKA_SENT", 0L);

        BigDecimal totalAmount = payments.stream()
            .map(Payment::getAmount)
            .filter(amount -> amount != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalPayments", payments.size());
        summary.put("received", receivedCount);
        summary.put("inProgress", pendingCount);
        summary.put("completed", completedCount);
        summary.put("rejected", rejectedCount);
        summary.put("totalAmount", totalAmount);

        return Map.of(
            "summary", summary,
            "statusCounts", statusCounts,
            "payments", recentPayments.stream().map(this::toPaymentSummary).toList()
        );
        }

    /**
     * Scheduled job: every 2 minutes, auto-advance payments stuck in intermediate
     * ISO states (ACTC, ACCP, ACSP, PDNG, RCVD) for more than 5 minutes to ACSC.
     * Simulates final settlement completion in the absence of a follow-up Kafka message.
     */
    @Scheduled(fixedDelay = 120_000)
    public void autoSettleStaleInProgressPayments() {
        List<PaymentStatus> inProgressStatuses = List.of(
                PaymentStatus.RCVD,
                PaymentStatus.ACTC,
                PaymentStatus.ACCP,
                PaymentStatus.ACSP,
                PaymentStatus.PDNG
        );
        LocalDateTime staleThreshold = LocalDateTime.now().minusMinutes(5);
        List<Payment> stale = paymentRepository.findByStatusIn(inProgressStatuses).stream()
                .filter(p -> p.getUpdatedAt() != null
                        ? p.getUpdatedAt().isBefore(staleThreshold)
                        : p.getCreatedAt() != null && p.getCreatedAt().isBefore(staleThreshold))
                .toList();
        if (stale.isEmpty()) {
            log.debug("[SCHEDULER] No stale in-progress payments to settle");
            return;
        }
        log.info("[SCHEDULER] Auto-settling {} stale in-progress payment(s) older than 5 minutes", stale.size());
        for (Payment p : stale) {
            log.info("[SCHEDULER] Auto-settling | paymentId={} | currentStatus={} | age={}",
                    p.getPaymentId(), p.getStatus(), p.getUpdatedAt());
            p.setStatus(PaymentStatus.ACSC);
            p.setPain002StatusReason("Auto-settled: payment reached final settlement after processing window");
            p.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(p);
        }
        log.info("[SCHEDULER] Auto-settlement complete — {} payment(s) moved to ACSC", stale.size());
    }

    /**
     * Manual recovery: immediately advance all in-progress payments to ACSC.
     * Returns count of payments recovered.
     */
    public int recoverInProgressPayments() {
        List<PaymentStatus> inProgressStatuses = List.of(
                PaymentStatus.RCVD,
                PaymentStatus.ACTC,
                PaymentStatus.ACCP,
                PaymentStatus.ACSP,
                PaymentStatus.PDNG,
                PaymentStatus.RGTC  // legacy
        );
        List<Payment> inProgress = paymentRepository.findByStatusIn(inProgressStatuses);
        log.info("[RECOVERY] Manual recovery triggered — {} in-progress payment(s) found", inProgress.size());
        for (Payment p : inProgress) {
            log.info("[RECOVERY] Recovering | paymentId={} | status={}", p.getPaymentId(), p.getStatus());
            p.setStatus(PaymentStatus.ACSC);
            p.setPain002StatusReason("Manually recovered: payment force-settled via admin recovery");
            p.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(p);
        }
        log.info("[RECOVERY] Manual recovery complete — {} payment(s) set to ACSC", inProgress.size());
        return inProgress.size();
    }

    public void updatePaymentStatusFromPain002(PaymentStatusMessage statusMessage) {
        String paymentId = statusMessage.getOriginalPaymentId();
        String transactionStatus = statusMessage.getTransactionStatus();

        Optional<Payment> paymentOptional = paymentRepository.findFirstByPaymentIdOrderByCreatedAtDesc(paymentId);
        if (paymentOptional.isEmpty()) {
            log.warn("[PAYMENT] PAIN 002 status ignored, payment not found | paymentId={} | status={}",
                    paymentId, transactionStatus);
            return;
        }

        Payment payment = paymentOptional.get();
        PaymentStatus mappedStatus = mapPain002Status(transactionStatus);

        payment.setStatus(mappedStatus);
        payment.setPain002MessageId(statusMessage.getMessageId());
        payment.setPain002StatusReason(statusMessage.getStatusReasonDescription());
        payment.setPain002ProcessedAt(statusMessage.getProcessedAt());
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        log.info("[PAYMENT] PAIN 002 status updated | paymentId={} | pain002Status={} | internalStatus={}",
                paymentId, transactionStatus, mappedStatus);
    }

    private PaymentStatus mapPain002Status(String transactionStatus) {
        if (transactionStatus == null || transactionStatus.isBlank()) {
            return PaymentStatus.RJCT;
        }

        String normalized = transactionStatus.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "RCVD" -> PaymentStatus.RCVD;
            case "ACTC" -> PaymentStatus.ACTC;
            case "ACCP" -> PaymentStatus.ACCP;
            case "ACSP" -> PaymentStatus.ACSP;
            case "ACSC" -> PaymentStatus.ACSC;
            case "PDNG" -> PaymentStatus.PDNG;
            case "RJCT" -> PaymentStatus.RJCT;
            case "CANC" -> PaymentStatus.CANC;
            default -> {
                log.warn("[PAYMENT] Unknown PAIN 002 status code, defaulting to RJCT | status={}", transactionStatus);
                yield PaymentStatus.RJCT;
            }
        };
    }

    private Map<String, Object> toPaymentSummary(Payment payment) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("paymentId", payment.getPaymentId());
        summary.put("debtorName", payment.getDebtorName());
        summary.put("creditorName", payment.getCreditorName());
        summary.put("amount", payment.getAmount());
        summary.put("currency", payment.getCurrency());
        summary.put("status", payment.getStatus());
        summary.put("requestedExecutionDate", payment.getRequestedExecutionDate());
        summary.put("pain002StatusReason", payment.getPain002StatusReason());
        summary.put("createdAt", payment.getCreatedAt());
        summary.put("updatedAt", payment.getUpdatedAt());
        return summary;
    }

    private void validatePayment(PaymentRequest request) {
        if (request.getAmount() == null || request.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (request.getDebtorAccount() == null || request.getDebtorAccount().isBlank()) {
            throw new IllegalArgumentException("Debtor account is required");
        }
        if (request.getCreditorAccount() == null || request.getCreditorAccount().isBlank()) {
            throw new IllegalArgumentException("Creditor account is required");
        }
    }

    private Payment mapRequestToPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setPaymentId(request.getPaymentId());
        payment.setDebtorAccount(request.getDebtorAccount());
        payment.setDebtorName(request.getDebtorName());
        payment.setCreditorAccount(request.getCreditorAccount());
        payment.setCreditorName(request.getCreditorName());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setPaymentPurpose(request.getPaymentPurpose());
        payment.setRemittanceInformation(request.getRemittanceInformation());
        payment.setExecutionDate(request.getExecutionDate());
        payment.setRequestedExecutionDate(request.getRequestedExecutionDate());
        payment.setPriority(request.getPriority());
        payment.setDebtorRoutingNumber(request.getDebtorRoutingNumber());
        payment.setCreditorRoutingNumber(request.getCreditorRoutingNumber());
        return payment;
    }
}
