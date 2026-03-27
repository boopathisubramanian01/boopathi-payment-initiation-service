# Test Examples

## cURL Commands for Testing

### 1. Health Check
```bash
curl -X GET http://localhost:8080/api/v1/payments/health \
  -H "Content-Type: application/json"
```

### 2. Successful Payment Request
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY20260327001",
    "debtorAccount": "DE89370400440532013000",
    "debtorName": "John Doe",
    "creditorAccount": "FR1420041010050500013M02606",
    "creditorName": "Jane Smith",
    "amount": 1000.50,
    "currency": "EUR",
    "paymentPurpose": "Invoice Payment",
    "remittanceInformation": "INV-2024-001",
    "executionDate": "2026-03-27",
    "requestedExecutionDate": "2026-03-28",
    "priority": "NORM"
  }'
```

### 3. Payment with HIGH Priority
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY20260327002",
    "debtorAccount": "GB82WEST12345698765432",
    "debtorName": "Alice Johnson",
    "creditorAccount": "IT60X0542811101000000123456",
    "creditorName": "Bob Smith",
    "amount": 5000.00,
    "currency": "EUR",
    "paymentPurpose": "Salary Payment",
    "remittanceInformation": "Salary for March 2026",
    "requestedExecutionDate": "2026-03-28",
    "priority": "HIGH"
  }'
```

### 4. Minimal Payment Request
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY20260327003",
    "debtorAccount": "ES9121000418450200051332",
    "debtorName": "Maria Garcia",
    "creditorAccount": "NL91ABNA0417164300",
    "creditorName": "Person X",
    "amount": 100.00,
    "currency": "EUR",
    "paymentPurpose": "Transfer"
  }'
```

### 5. Invalid Request - Missing Required Field
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY20260327004",
    "debtorAccount": "DE89370400440532013000",
    "debtorName": "John Doe",
    "creditorName": "Jane Smith",
    "amount": 1000.50,
    "currency": "EUR",
    "paymentPurpose": "Invoice Payment"
  }'
```

### 6. Invalid Request - Invalid Amount
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY20260327005",
    "debtorAccount": "DE89370400440532013000",
    "debtorName": "John Doe",
    "creditorAccount": "FR1420041010050500013M02606",
    "creditorName": "Jane Smith",
    "amount": -500.00,
    "currency": "EUR",
    "paymentPurpose": "Invoice Payment"
  }'
```

### 7. Invalid Request - Invalid Currency Code
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "PAY20260327006",
    "debtorAccount": "DE89370400440532013000",
    "debtorName": "John Doe",
    "creditorAccount": "FR1420041010050500013M02606",
    "creditorName": "Jane Smith",
    "amount": 1000.50,
    "currency": "INVALID",
    "paymentPurpose": "Invoice Payment"
  }'
```

### 8. Get Payment Status
```bash
curl -X GET http://localhost:8080/api/v1/payments/PAY20260327001 \
  -H "Content-Type: application/json"
```

## Using Postman

Import the following collection into Postman:

```json
{
  "info": {
    "name": "Payment Initiation Service",
    "description": "API collection for Payment Initiation Service",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Health Check",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{base_url}}/api/v1/payments/health",
          "host": ["{{base_url}}"],
          "path": ["api", "v1", "payments", "health"]
        }
      }
    },
    {
      "name": "Initiate Payment",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json",
            "type": "text"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"paymentId\": \"PAY20260327001\",\n  \"debtorAccount\": \"DE89370400440532013000\",\n  \"debtorName\": \"John Doe\",\n  \"creditorAccount\": \"FR1420041010050500013M02606\",\n  \"creditorName\": \"Jane Smith\",\n  \"amount\": 1000.50,\n  \"currency\": \"EUR\",\n  \"paymentPurpose\": \"Invoice Payment\",\n  \"remittanceInformation\": \"INV-2024-001\",\n  \"requestedExecutionDate\": \"2026-03-28\",\n  \"priority\": \"NORM\"\n}"
        },
        "url": {
          "raw": "{{base_url}}/api/v1/payments/initiate",
          "host": ["{{base_url}}"],
          "path": ["api", "v1", "payments", "initiate"]
        }
      }
    },
    {
      "name": "Get Payment Status",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json",
            "type": "text"
          }
        ],
        "url": {
          "raw": "{{base_url}}/api/v1/payments/PAY20260327001",
          "host": ["{{base_url}}"],
          "path": ["api", "v1", "payments", "PAY20260327001"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080",
      "type": "string"
    }
  ]
}
```

## Kafka Verification

### Check if messages were published to Kafka

```bash
# Using Kafka console consumer
kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic payment.pain001 \
  --from-beginning
```

### Create topic manually (if needed)
```bash
kafka-topics.sh --create --topic payment.pain001 \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

## MongoDB Verification

```bash
# Connect to MongoDB
mongosh

# Switch to payment database
use payment_db

# View all payments
db.payments.find()

# View a specific payment
db.payments.findOne({ paymentId: "PAY20260327001" })

# Count payments
db.payments.countDocuments()
```

## Expected PAIN 001 V9 Message Structure

```json
{
  "messageId": "550e8400-e29b-41d4-a716-446655440000",
  "creationDateTime": "2026-03-27T10:30:45.123",
  "numberOfTransactions": 1,
  "controlSum": 1000.50,
  "version": "pain.001.003.09",
  "initiatingParty": {
    "name": "John Doe",
    "id": "DE89370400440532013000"
  },
  "paymentInformation": {
    "paymentInformationId": "550e8400-e29b-41d4-a716-446655440001",
    "paymentMethod": "TRF",
    "batchBooking": false,
    "numberOfTransactions": 1,
    "controlSum": 1000.50,
    "paymentTypeInformation": {
      "instructionPriority": "NORM",
      "serviceLevel": "SEPA"
    },
    "debtor": {
      "name": "John Doe",
      "identification": "DE89370400440532013000"
    },
    "debtorAccount": {
      "iban": "DE89370400440532013000",
      "currency": "EUR"
    },
    "creditTransferTransaction": {
      "paymentId": "PAY20260327001",
      "instructedAmount": {
        "currency": "EUR",
        "value": 1000.50
      },
      "creditor": {
        "name": "Jane Smith",
        "identification": "FR1420041010050500013M02606"
      },
      "creditorAccount": {
        "iban": "FR1420041010050500013M02606",
        "currency": "EUR"
      },
      "remittanceInformation": {
        "unstructured": "INV-2024-001"
      },
      "requestedExecutionDate": "2026-03-28"
    }
  }
}
```
