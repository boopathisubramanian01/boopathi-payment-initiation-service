package com.payment.controller;

import com.payment.exception.DuplicatePaymentException;
import com.payment.model.Payment;
import com.payment.model.PaymentRequest;
import com.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("paymentId", request.getPaymentId());
        try {
        log.info("[API] POST /api/v1/payments/initiate | paymentId={} | debtor={} | creditor={} | amount={} {}",
                request.getPaymentId(), request.getDebtorName(), request.getCreditorName(),
                request.getAmount(), request.getCurrency());
            Payment processedPayment = paymentService.processPayment(request);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("statusCode", 200);
            response.put("message", "Payment processed successfully");
            response.put("paymentId", processedPayment.getPaymentId());
            response.put("paymentStatus", processedPayment.getStatus());
            response.put("kafkaMessageId", processedPayment.getKafkaMessageId());
            response.put("databaseId", processedPayment.getId());

            log.info("[API] Response 200 OK | paymentId={} | paymentStatus={} | kafkaMessageId={}",
                    processedPayment.getPaymentId(), processedPayment.getStatus(),
                    processedPayment.getKafkaMessageId());

            return ResponseEntity.ok(response);

        } catch (DuplicatePaymentException e) {
            log.warn("[API] Response 409 CONFLICT | paymentId={} | reason={}",
                    request.getPaymentId(), e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DUPLICATE");
            errorResponse.put("statusCode", 409);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("paymentId", e.getPaymentId());

            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);

        } catch (Exception e) {
            log.error("[API] Response 500 ERROR | paymentId={} | error={}",
                    request.getPaymentId(), e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("statusCode", 500);
            errorResponse.put("message", "Failed to process payment: " + e.getMessage());
            errorResponse.put("paymentId", request.getPaymentId());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable("paymentId") String paymentId) {
        MDC.put("traceId", UUID.randomUUID().toString());
        MDC.put("paymentId", paymentId);
        try {
        log.info("[API] GET /api/v1/payments/{} | Fetching payment status", paymentId);

        return paymentService.getPaymentStatus(paymentId)
                .map(payment -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("paymentId", payment.getPaymentId());
                    response.put("databaseId", payment.getId());
                    response.put("status", payment.getStatus());
                    response.put("debtorName", payment.getDebtorName());
                    response.put("debtorAccount", payment.getDebtorAccount());
                    response.put("creditorName", payment.getCreditorName());
                    response.put("creditorAccount", payment.getCreditorAccount());
                    response.put("amount", payment.getAmount());
                    response.put("currency", payment.getCurrency());
                    response.put("priority", payment.getPriority());
                    response.put("debtorRoutingNumber", payment.getDebtorRoutingNumber());
                    response.put("creditorRoutingNumber", payment.getCreditorRoutingNumber());
                    response.put("requestedExecutionDate", payment.getRequestedExecutionDate());
                    response.put("kafkaMessageId", payment.getKafkaMessageId());
                    response.put("pain002MessageId", payment.getPain002MessageId());
                    response.put("pain002StatusReason", payment.getPain002StatusReason());
                    response.put("pain002ProcessedAt", payment.getPain002ProcessedAt());
                    response.put("createdAt", payment.getCreatedAt());
                    response.put("updatedAt", payment.getUpdatedAt());

                    log.info("[API] Response 200 OK | paymentId={} | status={}", paymentId, payment.getStatus());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    log.warn("[API] Response 404 NOT FOUND | paymentId={}", paymentId);
                    Map<String, Object> error = new HashMap<>();
                    error.put("paymentId", paymentId);
                    error.put("message", "Payment not found");
                    error.put("statusCode", 404);
                    return ResponseEntity.status(404).body(error);
                });
        } finally {
            MDC.clear();
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(
            @RequestParam(name = "limit", defaultValue = "25") int limit) {
        log.info("[API] GET /api/v1/payments/dashboard | limit={}", limit);
        return ResponseEntity.ok(paymentService.getDashboardData(limit));
    }

    @PostMapping("/admin/recover")
    public ResponseEntity<Map<String, Object>> recoverInProgress() {
        log.info("[API] POST /api/v1/payments/admin/recover | Manual recovery triggered");
        int recovered = paymentService.recoverInProgressPayments();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("recovered", recovered);
        response.put("message", recovered + " in-progress payment(s) moved to ACSC");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Payment Initiation Service");
        return ResponseEntity.ok(response);
    }
}
