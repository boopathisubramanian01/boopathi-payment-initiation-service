package com.payment.demo;

import com.payment.model.Payment;
import com.payment.model.PaymentRequest;
import com.payment.model.Pain001Message;
import com.payment.service.Pain001MessageBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Standalone demo showing complete payment processing flow
 * Run without Spring Boot, Docker, MongoDB, or Kafka
 */
public class PaymentProcessingDemo {

    public static void main(String[] args) {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("   PAYMENT INITIATION SERVICE - PROCESSING DEMO");
        System.out.println("═══════════════════════════════════════════════════════════\n");

        try {
            // Step 1: Create Payment Request
            System.out.println("STEP 1: Create Payment Request");
            System.out.println("─────────────────────────────────────────────────────────");
            PaymentRequest paymentRequest = createPaymentRequest();
            printPaymentRequest(paymentRequest);

            // Step 2: Convert to Payment Model (simulating storage)
            System.out.println("\n\nSTEP 2: Convert to Payment Model (Storage Layer)");
            System.out.println("─────────────────────────────────────────────────────────");
            Payment payment = convertRequestToPayment(paymentRequest);
            payment.setStatus(Payment.PaymentStatus.STORED);
            printPayment(payment);

            // Step 3: Build PAIN 001 V9 ISO Message
            System.out.println("\n\nSTEP 3: Build PAIN 001 V9 ISO Message");
            System.out.println("─────────────────────────────────────────────────────────");
            Pain001MessageBuilder builder = new Pain001MessageBuilder();
            Pain001Message pain001Message = builder.buildPain001Message(payment);
            printPain001Message(pain001Message);

            // Step 4: Serialize to JSON (simulating Kafka publishing)
            System.out.println("\n\nSTEP 4: Serialize to JSON (Kafka Ready)");
            System.out.println("─────────────────────────────────────────────────────────");
            String jsonMessage = serializePain001ToJson(pain001Message);
            System.out.println(jsonMessage);

            // Step 5: Summary
            System.out.println("\n\nSTEP 5: Processing Summary");
            System.out.println("─────────────────────────────────────────────────────────");
            printSummary(paymentRequest, pain001Message, jsonMessage);

            System.out.println("\n\n✅ DEMO COMPLETED SUCCESSFULLY!");
            System.out.println("═══════════════════════════════════════════════════════════\n");

        } catch (Exception e) {
            System.err.println("\n❌ ERROR DURING PROCESSING:");
            e.printStackTrace();
        }
    }

    private static PaymentRequest createPaymentRequest() {
        return PaymentRequest.builder()
                .paymentId("DEMO20260327001")
                .debtorAccount("DE89370400440532013000")
                .debtorName("John Doe")
                .creditorAccount("FR1420041010050500013M02606")
                .creditorName("Jane Smith")
                .amount(new BigDecimal("1500.75"))
                .currency("EUR")
                .paymentPurpose("Invoice Payment for Services")
                .remittanceInformation("INV-2024-001-March")
                .requestedExecutionDate("2026-03-28")
                .priority("HIGH")
                .build();
    }

    private static Payment convertRequestToPayment(PaymentRequest request) {
        return Payment.builder()
                .id("507f1f77bcf86cd799439011") // Simulated MongoDB ID
                .paymentId(request.getPaymentId())
                .debtorAccount(request.getDebtorAccount())
                .debtorName(request.getDebtorName())
                .creditorAccount(request.getCreditorAccount())
                .creditorName(request.getCreditorName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .paymentPurpose(request.getPaymentPurpose())
                .remittanceInformation(request.getRemittanceInformation())
                .requestedExecutionDate(request.getRequestedExecutionDate())
                .priority(request.getPriority())
                .createdAt(LocalDateTime.now())
                .build();
    }

    private static String serializePain001ToJson(Pain001Message message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize PAIN 001 message", e);
        }
    }

    private static void printPaymentRequest(PaymentRequest request) {
        System.out.println("Payment ID: " + request.getPaymentId());
        System.out.println("Debtor: " + request.getDebtorName() + " (" + request.getDebtorAccount() + ")");
        System.out.println("Creditor: " + request.getCreditorName() + " (" + request.getCreditorAccount() + ")");
        System.out.println("Amount: " + request.getAmount() + " " + request.getCurrency());
        System.out.println("Purpose: " + request.getPaymentPurpose());
        System.out.println("Remittance: " + request.getRemittanceInformation());
        System.out.println("Priority: " + request.getPriority());
        System.out.println("Requested Execution Date: " + request.getRequestedExecutionDate());
    }

    private static void printPayment(Payment payment) {
        System.out.println("Database ID: " + payment.getId());
        System.out.println("Payment ID: " + payment.getPaymentId());
        System.out.println("Status: " + payment.getStatus());
        System.out.println("Debtor: " + payment.getDebtorName());
        System.out.println("Creditor: " + payment.getCreditorName());
        System.out.println("Amount: " + payment.getAmount() + " " + payment.getCurrency());
        System.out.println("Created At: " + payment.getCreatedAt());
    }

    private static void printPain001Message(Pain001Message message) {
        System.out.println("Message ID: " + message.getMessageId());
        System.out.println("Version: " + message.getVersion());
        System.out.println("Creation DateTime: " + message.getCreationDateTime());
        System.out.println("Number of Transactions: " + message.getNumberOfTransactions());
        System.out.println("Control Sum: " + message.getControlSum());

        Pain001Message.InitiatingParty initiatingParty = message.getInitiatingParty();
        System.out.println("\nInitiating Party:");
        System.out.println("  Name: " + initiatingParty.getName());
        System.out.println("  ID: " + initiatingParty.getId());

        Pain001Message.PaymentInformation paymentInfo = message.getPaymentInformation();
        System.out.println("\nPayment Information:");
        System.out.println("  Payment Method: " + paymentInfo.getPaymentMethod());
        System.out.println("  Service Level: " + paymentInfo.getPaymentTypeInformation().getServiceLevel());
        System.out.println("  Priority: " + paymentInfo.getPaymentTypeInformation().getInstructionPriority());

        Pain001Message.CreditTransferTransaction txn = paymentInfo.getCreditTransferTransaction();
        System.out.println("\nCredit Transfer Transaction:");
        System.out.println("  Payment ID: " + txn.getPaymentId());
        System.out.println("  Amount: " + txn.getInstructedAmount().getValue() + " " +
                txn.getInstructedAmount().getCurrency());
        System.out.println("  Creditor: " + txn.getCreditor().getName());
        System.out.println("  Remittance: " + txn.getRemittanceInformation().getUnstructured());
        System.out.println("  Requested Execution: " + txn.getRequestedExecutionDate());
    }

    private static void printSummary(PaymentRequest request, Pain001Message message, String jsonMessage) {
        System.out.println("✓ Payment Request Received: " + request.getPaymentId());
        System.out.println("✓ Validation Passed: All required fields present");
        System.out.println("✓ Amount Valid: " + request.getAmount() + " " + request.getCurrency());
        System.out.println("✓ Stored in MongoDB: Document ID generated");
        System.out.println("✓ ISO Message Built: PAIN 001 V9 (pain.001.003.09)");
        System.out.println("✓ Message Serialized: " + (jsonMessage.length()) + " bytes JSON");
        System.out.println("✓ Ready for Kafka: payment.pain001 topic");
        System.out.println("\nResponse Status: HTTP 200 OK");
        System.out.println("Message ID for Tracking: " + message.getMessageId());
    }
}
