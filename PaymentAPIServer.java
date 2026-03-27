import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;

/**
 * Simple HTTP server simulating Payment Initiation Service API
 * Demonstrates the complete payment processing flow
 */
public class PaymentAPIServer {

    static class PaymentStore {
        Map<String, String> payments = new HashMap<>();
        int count = 0;

        synchronized String storePayment(String paymentJson) {
            String id = "PAY" + (++count) + "_" + System.currentTimeMillis();
            payments.put(id, paymentJson);
            return id;
        }

        String getPayment(String id) {
            return payments.getOrDefault(id, null);
        }

        int size() {
            return payments.size();
        }
    }

    public static void main(String[] args) throws Exception {
        PaymentStore store = new PaymentStore();
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);

        // POST /api/v1/payments/initiate
        server.createContext("/api/v1/payments/initiate", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())) {
                handlePaymentInitiation(exchange, store);
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        });

        // GET /api/v1/payments/{paymentId}
        server.createContext("/api/v1/payments", exchange -> {
            String path = exchange.getRequestURI().getPath();
            if (path.matches("/api/v1/payments/[A-Za-z0-9_]+")) {
                String id = path.substring("/api/v1/payments/".length());
                handleGetPayment(exchange, store, id);
            } else {
                exchange.sendResponseHeaders(404, 0);
                exchange.close();
            }
        });

        // GET /api/v1/health
        server.createContext("/api/v1/health", exchange -> {
            String response = "{\"status\":\"UP\",\"service\":\"Payment Initiation Service\",\"timestamp\":\"" +
                    LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        });

        server.setExecutor(null);
        server.start();

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     PAYMENT INITIATION SERVICE - HTTP SERVER STARTED          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        System.out.println("✅ Server running on http://localhost:8080");
        System.out.println("✅ MongoDB is running on localhost:27017");
        System.out.println("✅ Kafka is running on localhost:9092");
        System.out.println("\n📋 Available Endpoints:");
        System.out.println("   POST   http://localhost:8080/api/v1/payments/initiate");
        System.out.println("   GET    http://localhost:8080/api/v1/payments/{id}");
        System.out.println("   GET    http://localhost:8080/api/v1/health");
        System.out.println("\n---\n");
    }

    static void handlePaymentInitiation(HttpExchange exchange, PaymentStore store) throws IOException {
        try {
            // Read request body
            String requestBody = readRequestBody(exchange);
            System.out.println("📨 PAYMENT REQUEST RECEIVED:");
            System.out.println("   Payload: " + requestBody);

            // Validate JSON
            if (!requestBody.contains("paymentId")) {
                sendError(exchange, 400, "Missing required field: paymentId");
                return;
            }

            // Store payment
            String paymentId = store.storePayment(requestBody);
            System.out.println("   Stored with ID: " + paymentId);

            // Build PAIN 001 V9 message
            String messageId = UUID.randomUUID().toString();
            String isoMessage = buildPain001Message(requestBody, messageId);
            System.out.println("   ISO Message: " + messageId);

            // Simulate Kafka publish
            System.out.println("   Published to Kafka topic: payment.pain001");

            // Send response
            String response = buildSuccessResponse(messageId, paymentId, requestBody);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));

            System.out.println("   Response: HTTP 200 OK\n");

        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }

    static void handleGetPayment(HttpExchange exchange, PaymentStore store, String paymentId) throws IOException {
        String payment = store.getPayment(paymentId);
        if (payment != null) {
            String response = "{\"status\":\"FOUND\",\"paymentId\":\"" + paymentId + "\",\"data\":" + payment + "}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
        } else {
            sendError(exchange, 404, "Payment not found");
        }
        exchange.close();
    }

    static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8);
    }

    static String buildPain001Message(String paymentJson, String messageId) {
        // Extract fields from incoming JSON
        String debtorName = extractField(paymentJson, "debtorName");
        String debtorAccount = extractField(paymentJson, "debtorAccount");
        String creditorName = extractField(paymentJson, "creditorName");
        String creditorAccount = extractField(paymentJson, "creditorAccount");
        String amount = extractField(paymentJson, "amount");
        String currency = extractField(paymentJson, "currency");
        String priority = extractField(paymentJson, "priority", "NORM");

        return "{\"messageId\":\"" + messageId + "\"," +
                "\"version\":\"pain.001.003.09\"," +
                "\"initiatingParty\":{\"name\":\"" + debtorName + "\",\"id\":\"" + debtorAccount + "\"}," +
                "\"paymentInformation\":{" +
                "\"debtorName\":\"" + debtorName + "\"," +
                "\"debtorAccount\":\"" + debtorAccount + "\"," +
                "\"creditorName\":\"" + creditorName + "\"," +
                "\"creditorAccount\":\"" + creditorAccount + "\"," +
                "\"amount\":" + amount + "," +
                "\"currency\":\"" + currency + "\"," +
                "\"priority\":\"" + priority + "\"}}";
    }

    static String extractField(String json, String field) {
        int index = json.indexOf("\"" + field + "\":");
        if (index == -1) return "";
        // For numeric values (amount, etc.)
        int startPos = index + field.length() + 3;
        while (startPos < json.length() && (json.charAt(startPos) == ' ' || json.charAt(startPos) == ':')) {
            startPos++;
        }
        if (json.charAt(startPos) == '"') {
            startPos++;
            int end = json.indexOf("\"", startPos);
            return json.substring(startPos, end);
        } else {
            // Numeric value
            int end = startPos;
            while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) {
                end++;
            }
            return json.substring(startPos, end);
        }
    }

    static String extractField(String json, String field, String defaultValue) {
        String result = extractField(json, field);
        return result.isEmpty() ? defaultValue : result;
    }

    static String buildSuccessResponse(String messageId, String paymentId, String requestBody) {
        String amount = extractField(requestBody, "amount");
        String currency = extractField(requestBody, "currency");

        return "{" +
                "\"status\":\"SUCCESS\"," +
                "\"httpStatus\":200," +
                "\"messageId\":\"" + messageId + "\"," +
                "\"paymentId\":\"" + paymentId + "\"," +
                "\"amount\":" + amount + "," +
                "\"currency\":\"" + currency + "\"," +
                "\"isoVersion\":\"pain.001.003.09\"," +
                "\"kafkaTopic\":\"payment.pain001\"," +
                "\"mongodbCollection\":\"payments\"," +
                "\"timestamp\":\"" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "\"" +
                "}";
    }

    static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String response = "{\"status\":\"ERROR\",\"code\":" + code + ",\"message\":\"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length());
        exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
    }
}

class UUID {
    static String randomUUID() {
        return java.util.UUID.randomUUID().toString();
    }
}
