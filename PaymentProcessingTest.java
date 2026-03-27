import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Standalone Payment Processing Demonstration
 * Shows the complete flow without external dependencies
 */
public class PaymentProcessingTest {

    // Simple data models (matching Spring Boot DTOs)
    static class PaymentRequest {
        String paymentId;
        String debtorAccount;
        String debtorName;
        String creditorAccount;
        String creditorName;
        BigDecimal amount;
        String currency;
        String paymentPurpose;
        String remittanceInformation;
        String priority = "NORM";
    }

    static class Payment {
        String id;
        String paymentId;
        String debtorAccount;
        String debtorName;
        String creditorAccount;
        String creditorName;
        BigDecimal amount;
        String currency;
        String paymentPurpose;
        String remittanceInformation;
        String priority;
        String status;
        LocalDateTime createdAt;
    }

    static class Pain001Message {
        String messageId;
        String version = "pain.001.003.09";
        LocalDateTime creationDateTime;
        int numberOfTransactions = 1;
        BigDecimal controlSum;
        Map<String, Object> initiatingParty = new HashMap<>();
        Map<String, Object> paymentInformation = new HashMap<>();
    }

    public static void main(String[] args) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║   PAYMENT INITIATION SERVICE - STANDALONE DEMONSTRATION       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // STEP 1: Create Payment Request
        System.out.println("STEP 1: PAYMENT REQUEST RECEIVED");
        System.out.println("─────────────────────────────────────────────────────────────────");

        PaymentRequest request = new PaymentRequest();
        request.paymentId = "PAY20260327001";
        request.debtorAccount = "DE89370400440532013000";
        request.debtorName = "John Doe";
        request.creditorAccount = "FR1420041010050500013M02606";
        request.creditorName = "Jane Smith";
        request.amount = new BigDecimal("1500.75");
        request.currency = "EUR";
        request.paymentPurpose = "Invoice Payment";
        request.remittanceInformation = "INV-2024-001";
        request.priority = "HIGH";

        printPaymentRequest(request);

        // STEP 2: Validate
        System.out.println("\n\nSTEP 2: VALIDATION");
        System.out.println("─────────────────────────────────────────────────────────────────");

        ValidationResult validationResult = validatePaymentRequest(request);
        if (validationResult.isValid) {
            System.out.println("✅ All validations passed");
            for (String msg : validationResult.messages) {
                System.out.println("   ✓ " + msg);
            }
        } else {
            System.out.println("❌ Validation failed");
            for (String error : validationResult.messages) {
                System.out.println("   ✗ " + error);
            }
            return;
        }

        // STEP 3: Store in MongoDB
        System.out.println("\n\nSTEP 3: STORE IN MONGODB");
        System.out.println("─────────────────────────────────────────────────────────────────");

        Payment payment = convertRequestToPayment(request);
        payment.id = "507f1f77bcf86cd799439011";
        payment.status = "STORED";
        payment.createdAt = LocalDateTime.now();

        System.out.println("✓ Document inserted into 'payments' collection");
        System.out.println("  Database ID: " + payment.id);
        System.out.println("  Payment ID: " + payment.paymentId);
        System.out.println("  Status: " + payment.status);
        System.out.println("  Created: " + payment.createdAt.format(DateTimeFormatter.ISO_DATE_TIME));

        // STEP 4: Build PAIN 001 V9 Message
        System.out.println("\n\nSTEP 4: BUILD PAIN 001 V9 ISO MESSAGE");
        System.out.println("─────────────────────────────────────────────────────────────────");

        Pain001Message pain001Message = buildPain001Message(payment);
        printPain001Message(pain001Message);

        // STEP 5: Publish to Kafka
        System.out.println("\n\nSTEP 5: PUBLISH TO KAFKA");
        System.out.println("─────────────────────────────────────────────────────────────────");

        String jsonMessage = serializePain001(pain001Message);
        System.out.println("✓ Message serialized to JSON");
        System.out.println("✓ Published to Kafka topic: payment.pain001");
        System.out.println("✓ Message size: " + jsonMessage.length() + " bytes");
        System.out.println("\nJSON Payload (truncated):");
        String[] lines = jsonMessage.split("\n");
        for (int i = 0; i < Math.min(15, lines.length); i++) {
            System.out.println("  " + lines[i]);
        }
        if (lines.length > 15) {
            System.out.println("  ...");
        }

        // STEP 6: API Response
        System.out.println("\n\nSTEP 6: API RESPONSE");
        System.out.println("─────────────────────────────────────────────────────────────────");

        System.out.println("HTTP Status: 200 OK");
        System.out.println("\nResponse Body:");
        System.out.println("{");
        System.out.println("  \"status\": \"SUCCESS\",");
        System.out.println("  \"messageId\": \"" + pain001Message.messageId + "\",");
        System.out.println("  \"paymentId\": \"" + request.paymentId + "\",");
        System.out.println("  \"docId\": \"" + payment.id + "\",");
        System.out.println("  \"amount\": " + request.amount + ",");
        System.out.println("  \"currency\": \"" + request.currency + "\",");
        System.out.println("  \"isoVersion\": \"pain.001.003.09\"");
        System.out.println("}");

        // SUMMARY
        System.out.println("\n\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                       PROCESSING SUMMARY                       ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("✅ Payment received from API");
        System.out.println("✅ All validations passed");
        System.out.println("✅ Stored in MongoDB collection 'payments'");
        System.out.println("✅ ISO PAIN 001 V9 message created");
        System.out.println("✅ Published to Kafka topic 'payment.pain001'");
        System.out.println("✅ HTTP 200 OK response sent");
        System.out.println();
        System.out.println("Payment initialized successfully!");
        System.out.println("Message ID for tracking: " + pain001Message.messageId);
        System.out.println();
    }

    static void printPaymentRequest(PaymentRequest req) {
        System.out.println("Payment ID:        " + req.paymentId);
        System.out.println("Debtor:            " + req.debtorName + " (" + req.debtorAccount + ")");
        System.out.println("Creditor:          " + req.creditorName + " (" + req.creditorAccount + ")");
        System.out.println("Amount:            " + req.amount + " " + req.currency);
        System.out.println("Purpose:           " + req.paymentPurpose);
        System.out.println("Remittance:        " + req.remittanceInformation);
        System.out.println("Priority:          " + req.priority);
    }

    static ValidationResult validatePaymentRequest(PaymentRequest req) {
        ValidationResult result = new ValidationResult();

        if (req.paymentId != null && !req.paymentId.isEmpty())
            result.add("Payment ID is valid");
        else
            result.addError("Payment ID is required");

        if (req.debtorAccount != null && isValidIBAN(req.debtorAccount))
            result.add("Debtor account is valid IBAN");
        else
            result.addError("Debtor account must be valid IBAN");

        if (req.debtorName != null && !req.debtorName.isEmpty())
            result.add("Debtor name is valid");
        else
            result.addError("Debtor name is required");

        if (req.creditorAccount != null && isValidIBAN(req.creditorAccount))
            result.add("Creditor account is valid IBAN");
        else
            result.addError("Creditor account must be valid IBAN");

        if (req.creditorName != null && !req.creditorName.isEmpty())
            result.add("Creditor name is valid");
        else
            result.addError("Creditor name is required");

        if (req.amount != null && req.amount.compareTo(new BigDecimal("0.01")) >= 0)
            result.add("Amount is >= 0.01");
        else
            result.addError("Amount must be >= 0.01");

        if (req.currency != null && req.currency.length() == 3)
            result.add("Currency code is 3 characters (ISO 4217)");
        else
            result.addError("Currency must be 3-letter ISO 4217 code");

        if (req.paymentPurpose != null && !req.paymentPurpose.isEmpty())
            result.add("Payment purpose is valid");
        else
            result.addError("Payment purpose is required");

        return result;
    }

    static boolean isValidIBAN(String iban) {
        return iban != null && iban.length() >= 15 && iban.length() <= 34;
    }

    static Payment convertRequestToPayment(PaymentRequest req) {
        Payment payment = new Payment();
        payment.paymentId = req.paymentId;
        payment.debtorAccount = req.debtorAccount;
        payment.debtorName = req.debtorName;
        payment.creditorAccount = req.creditorAccount;
        payment.creditorName = req.creditorName;
        payment.amount = req.amount;
        payment.currency = req.currency;
        payment.paymentPurpose = req.paymentPurpose;
        payment.remittanceInformation = req.remittanceInformation;
        payment.priority = req.priority;
        return payment;
    }

    static Pain001Message buildPain001Message(Payment payment) {
        Pain001Message msg = new Pain001Message();
        msg.messageId = UUID.randomUUID().toString();
        msg.creationDateTime = LocalDateTime.now();
        msg.controlSum = payment.amount;

        Map<String, Object> initiatingParty = new HashMap<>();
        initiatingParty.put("name", payment.debtorName);
        initiatingParty.put("id", payment.debtorAccount);
        msg.initiatingParty = initiatingParty;

        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("paymentMethod", "TRF");
        paymentInfo.put("debtorName", payment.debtorName);
        paymentInfo.put("debtorAccount", payment.debtorAccount);
        paymentInfo.put("creditorName", payment.creditorName);
        paymentInfo.put("creditorAccount", payment.creditorAccount);
        paymentInfo.put("amount", payment.amount);
        paymentInfo.put("currency", payment.currency);
        paymentInfo.put("purpose", payment.paymentPurpose);
        paymentInfo.put("remittance", payment.remittanceInformation);
        paymentInfo.put("priority", payment.priority);
        msg.paymentInformation = paymentInfo;

        return msg;
    }

    static void printPain001Message(Pain001Message msg) {
        System.out.println("ISO Format:        " + msg.version);
        System.out.println("Message ID:        " + msg.messageId);
        System.out.println("Created:           " + msg.creationDateTime.format(DateTimeFormatter.ISO_DATE_TIME));
        System.out.println("Transactions:      " + msg.numberOfTransactions);
        System.out.println("Control Sum:       " + msg.controlSum + " " + ((Map<String, Object>)msg.paymentInformation).get("currency"));
        System.out.println("\nPayment Details:");
        System.out.println("  Payment Method:  " + msg.paymentInformation.get("paymentMethod"));
        System.out.println("  Debtor:          " + msg.paymentInformation.get("debtorName"));
        System.out.println("  Creditor:        " + msg.paymentInformation.get("creditorName"));
        System.out.println("  Priority:        " + msg.paymentInformation.get("priority"));
    }

    static String serializePain001(Pain001Message msg) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"messageId\": \"").append(msg.messageId).append("\",\n");
        json.append("  \"version\": \"").append(msg.version).append("\",\n");
        json.append("  \"creationDateTime\": \"").append(msg.creationDateTime).append("\",\n");
        json.append("  \"numberOfTransactions\": ").append(msg.numberOfTransactions).append(",\n");
        json.append("  \"controlSum\": ").append(msg.controlSum).append(",\n");
        json.append("  \"initiatingParty\": {\n");
        json.append("    \"name\": \"").append(msg.initiatingParty.get("name")).append("\",\n");
        json.append("    \"id\": \"").append(msg.initiatingParty.get("id")).append("\"\n");
        json.append("  },\n");
        json.append("  \"paymentInformation\": {\n");
        for (String key : msg.paymentInformation.keySet()) {
            Object value = msg.paymentInformation.get(key);
            if (value instanceof String) {
                json.append("    \"").append(key).append("\": \"").append(value).append("\",\n");
            } else {
                json.append("    \"").append(key).append("\": ").append(value).append(",\n");
            }
        }
        json.setLength(json.length() - 2); // Remove last comma
        json.append("\n  }\n");
        json.append("}");
        return json.toString();
    }

    static class ValidationResult {
        boolean isValid = true;
        java.util.List<String> messages = new java.util.ArrayList<>();

        void add(String msg) {
            messages.add(msg);
        }

        void addError(String msg) {
            isValid = false;
            messages.add(msg);
        }
    }
}
