package com.payment.controller;

import com.payment.exception.DuplicatePaymentException;
import com.payment.model.Payment;
import com.payment.model.PaymentRequest;
import com.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        log.info("[API] POST /api/v1/payments/initiate | paymentId={} | debtor={} | creditor={} | amount={} {}",
                request.getPaymentId(), request.getDebtorName(), request.getCreditorName(),
                request.getAmount(), request.getCurrency());

        try {
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
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentStatus(@PathVariable("paymentId") String paymentId) {
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
                    response.put("requestedExecutionDate", payment.getRequestedExecutionDate());
                    response.put("kafkaMessageId", payment.getKafkaMessageId());
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
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Payment Initiation Service");
        return ResponseEntity.ok(response);
    }
}
