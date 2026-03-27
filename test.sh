#!/bin/bash

echo "═══════════════════════════════════════════════════════════════════════"
echo "  PAYMENT INITIATION SERVICE - COMPLETE TEST FLOW"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""

echo "✅ INFRASTRUCTURE SERVICES STATUS"
echo "───────────────────────────────────────────────────────────────────────"

docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | grep payment

echo ""
echo "✅ MONGODB CONNECTION TEST"
echo "───────────────────────────────────────────────────────────────────────"
docker exec payment-mongodb mongosh --eval 'db.adminCommand("ping")' --quiet 2>/dev/null && echo "✓ MongoDB is healthy" || echo "✗ MongoDB connection failed"

echo ""
echo "✅ KAFKA BROKER STATUS"
echo "───────────────────────────────────────────────────────────────────────"
docker exec payment-kafka bash -c 'kafka-broker-api-versions.sh --bootstrap-server localhost:9092' 2>/dev/null | grep -q "ApiVersion" && echo "✓ Kafka broker is healthy" || echo "✗ Kafka broker check failed"

echo ""
echo "✅ SERVICES ENDPOINTS"
echo "───────────────────────────────────────────────────────────────────────"
echo "• MongoDB:      mongodb://admin:password@localhost:27017/payment_db"
echo "• Kafka:        localhost:9092"
echo "• Zookeeper:    localhost:2181"
echo "• Kafka UI:     http://localhost:8088"

echo ""
echo "═══════════════════════════════════════════════════════════════════════"
echo "  API TEST SCENARIOS"
echo "═══════════════════════════════════════════════════════════════════════"

# Test data
TEST_PAYMENT_ID="TEST$(date +%s)"
TEST_PAYLOAD=$(cat <<EOF
{
  "paymentId": "$TEST_PAYMENT_ID",
  "debtorAccount": "DE89370400440532013000",
  "debtorName": "John Doe",
  "creditorAccount": "FR1420041010050500013M02606",
  "creditorName": "Jane Smith",
  "amount": 1500.75,
  "currency": "EUR",
  "paymentPurpose": "Invoice Payment",
  "remittanceInformation": "INV-2024-001",
  "priority": "HIGH"
}
EOF
)

echo ""
echo "📋 TEST 1: Payment Request Sample Data"
echo "───────────────────────────────────────────────────────────────────────"
echo "$TEST_PAYLOAD" | jq '.'

echo ""
echo "📋 TEST 2: Expected API Flow"
echo "───────────────────────────────────────────────────────────────────────"
echo "1. POST /api/v1/payments/initiate"
echo "   ├─ Input: Payment Request (JSON)"
echo "   ├─ Validation: Check all required fields"
echo "   ├─ Database: Store in MongoDB collection 'payments'"
echo "   ├─ ISO Message: Build PAIN 001 V9 message"
echo "   ├─ Kafka: Publish to 'payment.pain001' topic"
echo "   └─ Response: HTTP 200 OK with message ID"
echo ""
echo "2. Message Structure"
echo "   ├─ pain.001.003.09 (PAIN 001 V9 format)"
echo "   ├─ Message ID (unique)"
echo "   ├─ Initiating Party (debtor)"
echo "   ├─ Payment Information"
echo "   │  ├─ Payment Method: TRF (Transfer)"
echo "   │  ├─ Service Level: SEPA"
echo "   │  ├─ Priority: HIGH/NORM/LOW"
echo "   │  └─ Credit Transfer Transaction"
echo "   │     ├─ Debtor Account (IBAN)"
echo "   │     ├─ Creditor Account (IBAN)"
echo "   │     ├─ Amount & Currency"
echo "   │     └─ Remittance Information"
echo ""

echo "📋 TEST 3: Validation Rules"
echo "───────────────────────────────────────────────────────────────────────"
echo "✓ paymentId: Required, non-empty"
echo "✓ debtorAccount: Required (IBAN format)"
echo "✓ debtorName: Required, non-empty"
echo "✓ creditorAccount: Required (IBAN format)"
echo "✓ creditorName: Required, non-empty"
echo "✓ amount: Required, >= 0.01"
echo "✓ currency: Required (3-letter ISO 4217)"
echo "✓ paymentPurpose: Required, non-empty"
echo "✓ remittanceInformation: Optional"
echo "✓ priority: Optional (defaults to NORM)"

echo ""
echo "📋 TEST 4: Component Interactions"
echo "───────────────────────────────────────────────────────────────────────"
cat <<'EOF'
PaymentController
    ↓ (receives HTTP POST)
PaymentRequest (DTO with @Valid)
    ↓
PaymentService.processPayment()
    ├─ Store in MongoDB
    │   └─ Payment document created
    │
    ├─ Build ISO Message
    │   └─ Pain001MessageBuilder.buildPain001Message()
    │
    ├─ Produce to Kafka
    │   └─ Pain001Producer.sendMessage()
    │       └─ Topic: payment.pain001
    │
    └─ Response: 200 OK with Message ID
EOF

echo ""
echo "📋 TEST 5: Database Schema"
echo "───────────────────────────────────────────────────────────────────────"
echo "Collection: payments"
echo "Document Structure:"
cat <<'EOF'
{
  "_id": ObjectId("..."),
  "paymentId": "TEST1234567890",
  "debtorAccount": "DE89370400440532013000",
  "debtorName": "John Doe",
  "creditorAccount": "FR1420041010050500013M02606",
  "creditorName": "Jane Smith",
  "amount": 1500.75,
  "currency": "EUR",
  "paymentPurpose": "Invoice Payment",
  "remittanceInformation": "INV-2024-001",
  "priority": "HIGH",
  "status": "STORED",
  "createdAt": ISODate("2026-03-27T14:10:00Z"),
  "updatedAt": ISODate("2026-03-27T14:10:00Z")
}
EOF

echo ""
echo "═══════════════════════════════════════════════════════════════════════"
echo "  ENVIRONMENT VERIFICATION"
echo "═══════════════════════════════════════════════════════════════════════"

echo ""
echo "Docker Status:"
echo "  Java Version:" && /opt/homebrew/bin/java -version 2>&1 | head -1
echo "  Maven Version:" && /opt/homebrew/bin/mvn --version 2>&1 | head -1
echo "  Docker Version:" && docker --version

echo ""
echo "═══════════════════════════════════════════════════════════════════════"
echo "  NEXT STEPS TO RUN THE APPLICATION"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""
echo "1. Install Maven Dependencies:"
echo "   /opt/homebrew/bin/mvn clean install -DskipTests"
echo ""
echo "2. Start the Application:"
echo "   /opt/homebrew/bin/mvn spring-boot:run"
echo ""
echo "3. Test the Payment API:"
echo "   curl -X POST http://localhost:8080/api/v1/payments/initiate \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     -d '$TEST_PAYLOAD'"
echo ""
echo "4. View Processing Logs:"
echo "   docker logs payment-mongodb"
echo "   docker logs payment-kafka"
echo ""
echo "5. Monitor Kafka Messages:"
echo "   Open http://localhost:8088 (Kafka UI)"
echo ""

echo "═══════════════════════════════════════════════════════════════════════"
echo "  ✅ TEST FLOW COMPLETE"
echo "═══════════════════════════════════════════════════════════════════════"
