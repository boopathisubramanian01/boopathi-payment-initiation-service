# Payment Initiation Service

A Spring Boot microservice for processing payment requests with ISO PAIN 001 V9 message generation and Kafka integration.

## Architecture Overview

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │ POST /api/v1/payments/initiate
       ▼
┌──────────────────────┐
│ PaymentController    │
└──────┬───────────────┘
       │ Validate (@Valid)
       ▼
┌──────────────────────┐
│ PaymentService       │
├─ Validate           │
├─ Store in MongoDB   │
├─ Build PAIN 001 msg │
└──────┬───────────────┘
       │ Publish
       ▼
┌──────────────────────┐
│ Kafka Topic          │
│ payment.pain001      │
└──────────────────────┘
```

## Prerequisites

1. Java 17 or higher
2. Maven 3.6+
3. MongoDB 4.0+ (open-source document database)
4. Kafka 2.8+ (message broker)

## Setup Instructions

### 1. Install MongoDB

**macOS (using Homebrew):**
```bash
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb-community
```

**Linux (Ubuntu):**
```bash
sudo apt-get update
sudo apt-get install -y mongodb
sudo systemctl start mongodb
```

**Docker:**
```bash
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

### 2. Install Kafka

**macOS (using Homebrew):**
```bash
brew install kafka
brew services start zookeeper
brew services start kafka
```

**Docker:**
```bash
docker-compose up -d
```

Create a `docker-compose.yml`:
```yaml
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
```

### 3. Build the project

```bash
mvn clean install -DskipTests
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

## API Endpoints

### 1. Initiate Payment

**Request:**
```bash
POST http://localhost:8080/api/v1/payments/initiate
Content-Type: application/json

{
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
}
```

**Response (200 OK):**
```json
{
  "status": "SUCCESS",
  "statusCode": 200,
  "message": "Payment processed successfully",
  "paymentId": "PAY20260327001",
  "kafkaMessageId": "550e8400-e29b-41d4-a716-446655440000",
  "databaseId": "507f1f77bcf86cd799439011"
}
```

### 2. Get Payment Status

**Request:**
```bash
GET http://localhost:8080/api/v1/payments/{paymentId}
```

**Response (200 OK):**
```json
{
  "paymentId": "PAY20260327001",
  "message": "Payment status retrieved"
}
```

### 3. Health Check

**Request:**
```bash
GET http://localhost:8080/api/v1/payments/health
```

**Response (200 OK):**
```json
{
  "status": "UP",
  "service": "Payment Initiation Service"
}
```

## Payment Processing Flow

1. **Receive Request**: API endpoint receives payment request JSON
2. **Validate**: @Valid annotation validates against annotations in PaymentRequest
3. **Store**: Payment document stored in MongoDB
4. **Build PAIN 001**: ISO pain.001.003.09 message created
5. **Publish to Kafka**: Message published to `payment.pain001` topic
6. **Return 200**: HTTP 200 OK response sent to client

## Validation Rules

- `paymentId`: Required, non-empty string
- `debtorAccount`: Required, non-empty string (IBAN format recommended)
- `debtorName`: Required, non-empty string
- `creditorAccount`: Required, non-empty string (IBAN format recommended)
- `creditorName`: Required, non-empty string
- `amount`: Required, decimal >= 0.01
- `currency`: Required, 3-letter ISO 4217 code (e.g., EUR, USD)
- `paymentPurpose`: Required, non-empty string
- `remittanceInformation`: Optional
- `priority`: Optional, defaults to NORM (NORM, HIGH, LOW)

## MongoDB Collections

### payments
```json
{
  "_id": "ObjectId",
  "paymentId": "string",
  "debtorAccount": "string",
  "debtorName": "string",
  "creditorAccount": "string",
  "creditorName": "string",
  "amount": "decimal",
  "currency": "string",
  "paymentPurpose": "string",
  "remittanceInformation": "string",
  "executionDate": "string",
  "requestedExecutionDate": "string",
  "priority": "string",
  "status": "enum",
  "kafkaMessageId": "string",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

## Kafka Topic

**Topic Name:** `payment.pain001`
**Message Format:** JSON (PAIN 001 V9 ISO message)

## Payment Statuses

- `RECEIVED`: Payment request received
- `VALIDATED`: Payment data validated
- `STORED`: Payment stored in database
- `KAFKA_SENT`: PAIN 001 message published to Kafka
- `COMPLETED`: Full process completed
- `FAILED`: Error during processing

## Environment Variables (optional)

Create `.env.local` or modify `application.properties`:

```properties
spring.data.mongodb.uri=mongodb://localhost:27017/payment_db
spring.kafka.bootstrap-servers=localhost:9092
```

## Error Handling

### Validation Error (400)
```json
{
  "status": "VALIDATION_ERROR",
  "statusCode": 400,
  "message": "Invalid payment request",
  "errors": "amount: must be greater than 0, currency: must be a valid ISO 4217 code"
}
```

### Server Error (500)
```json
{
  "status": "ERROR",
  "statusCode": 500,
  "message": "Failed to process payment: Connection refused"
}
```

## Testing

Run unit and integration tests:
```bash
mvn test
```

## Project Structure

```
src/
├── main/
│   ├── java/com/payment/
│   │   ├── PaymentInitiationServiceApplication.java
│   │   ├── config/
│   │   │   └── AppConfig.java
│   │   ├── controller/
│   │   │   └── PaymentController.java
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── kafka/
│   │   │   └── Pain001Producer.java
│   │   ├── model/
│   │   │   ├── Payment.java
│   │   │   ├── PaymentRequest.java
│   │   │   └── Pain001Message.java
│   │   ├── repository/
│   │   │   └── PaymentRepository.java
│   │   └── service/
│   │       ├── PaymentService.java
│   │       └── Pain001MessageBuilder.java
│   └── resources/
│       └── application.properties
└── test/
```

## Notes

- MongoDB is used as the document-based database (open-source)
- PAIN 001 V9 (pain.001.003.09) message format is used for ISO standard compliance
- Kafka ensures asynchronous processing and message durability
- All payments are validated before processing
- The service returns 200 OK immediately after Kafka publishing

## License

MIT
