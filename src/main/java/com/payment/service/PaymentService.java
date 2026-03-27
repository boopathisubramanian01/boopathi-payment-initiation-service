package com.payment.service;

import com.payment.exception.DuplicatePaymentException;
import com.payment.kafka.Pain001Producer;
import com.payment.model.Pain001Message;
import com.payment.model.Payment;
import com.payment.model.Payment.PaymentStatus;
import com.payment.model.PaymentRequest;
import com.payment.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private Pain001MessageBuilder pain001Builder;

    @Autowired
    private Pain001Producer pain001Producer;

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
            savedPayment.setStatus(PaymentStatus.FAILED);
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
            savedPayment.setStatus(PaymentStatus.FAILED);
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
            savedPayment.setStatus(PaymentStatus.FAILED);
            savedPayment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(savedPayment);
            log.error("[PAYMENT] STEP 6 - Kafka publish FAILED | paymentId={} | error={}", paymentId, e.getMessage());
            throw e;
        }

        // ── Step 7: Kafka ACK confirmed — update status to RECEIVED ─────────
        log.info("[PAYMENT] STEP 7 - Kafka broker ACK confirmed | paymentId={} | kafkaMessageId={}",
                paymentId, kafkaMessageId);
        savedPayment.setKafkaMessageId(kafkaMessageId);
        savedPayment.setStatus(PaymentStatus.RECEIVED);
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
        return Payment.builder()
                .paymentId(request.getPaymentId())
                .debtorAccount(request.getDebtorAccount())
                .debtorName(request.getDebtorName())
                .creditorAccount(request.getCreditorAccount())
                .creditorName(request.getCreditorName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentPurpose(request.getPaymentPurpose())
                .remittanceInformation(request.getRemittanceInformation())
                .executionDate(request.getExecutionDate())
                .requestedExecutionDate(request.getRequestedExecutionDate())
                .priority(request.getPriority())
                .build();
    }
}
