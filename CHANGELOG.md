# Changelog

All notable changes to the Payment Initiation Service are documented here.

---

## [1.0.0] - 2026-03-27

### Initial Release — Payment Initiation Service (PAIN 001 v9 ISO)

---

### Added

#### Core Payment Flow
- End-to-end payment initiation API using ISO 20022 PAIN 001 v9 message format
- `POST /api/v1/payments/initiate` — accepts payment request, processes and publishes to Kafka
- `GET /api/v1/payments/{paymentId}` — returns full payment record with current status from MongoDB

#### Duplicate Payment Detection
- Payment `paymentId` checked against MongoDB before processing begins
- Duplicate requests rejected immediately at **Step 1** of the flow
- Returns `HTTP 409 Conflict` with clear rejection message
- Original payment record untouched; no duplicate document written to MongoDB

#### Kafka Integration (Synchronous ACK)
- PAIN 001 ISO message published to topic `payment.pain001.topic`
- Synchronous broker acknowledgement via `kafkaTemplate.send().get(10s)`
- Status updated to `RECEIVED` only after confirmed Kafka broker ACK
- Logs include topic, partition, and offset on successful publish

#### MongoDB Status Tracking
- Payment status persisted at every step of the processing flow:
  - `PROCESSING` → record created
  - `VALIDATED` → business rules passed
  - `STORED` → persisted to MongoDB
  - `KAFKA_SENT` → message dispatched to Kafka
  - `RECEIVED` → Kafka broker ACK confirmed
  - `FAILED` → any step threw an exception
- `createdAt` and `updatedAt` timestamps maintained throughout

#### Structured Logging
- `[API]` prefix logs every inbound request and outbound response (controller layer)
- `[PAYMENT]` prefix logs each step (STEP 1–7) in the service layer
- `[KAFKA]` prefix logs serialization, dispatch, and broker ACK with partition/offset
- `WARN` level used for duplicate rejections; `ERROR` for failures with exception detail

#### Infrastructure (Docker Compose)
- MongoDB with authentication (`admin/password`, `payment_db`)
- Kafka in **KRaft mode** (no Zookeeper) using Confluent image
- Kafka UI (`provectuslabs/kafka-ui`) on port `8088` for message inspection
- All services on isolated `payment-network` bridge network

#### Build & Compatibility Fixes
- Upgraded Lombok `1.18.30` → `1.18.36` for Java 21 compatibility
- Added `annotationProcessorPaths` to Maven compiler plugin for Lombok
- Imported `spring-boot-dependencies` BOM in `<dependencyManagement>` to resolve all transitive version conflicts (logback, spring-kafka, jackson)
- Registered `JavaTimeModule` on `ObjectMapper` for `LocalDateTime` serialisation
- Migrated Kafka Docker config from Zookeeper mode to KRaft mode
- Fixed `@PathVariable("paymentId")` explicit naming required by Spring 6 without `-parameters` compiler flag
- Added `findFirstByPaymentIdOrderByCreatedAtDesc` repository method as safe fallback for status queries

### Changed
- `PaymentStatus` enum updated: replaced `RECEIVED/COMPLETED` with a richer set:
  `PROCESSING`, `VALIDATED`, `STORED`, `KAFKA_SENT`, `RECEIVED`, `FAILED`, `DUPLICATE`
- `PaymentController` rewritten: structured API logging, `DuplicatePaymentException` → HTTP 409
- `PaymentService` rewritten: 7-step flow with per-step status persistence and error handling
- `Pain001Producer` rewritten: synchronous Kafka send with broker ACK wait and offset logging
- MongoDB URI updated to include credentials: `mongodb://admin:password@localhost:27017/payment_db?authSource=admin`

### Fixed
- `IncorrectResultSizeDataAccessException` on status GET when duplicate documents existed — resolved by not persisting duplicate records and using `findFirst` query
- Port 8080 conflict on restart — resolved by killing existing process before startup
- Kafka UI container not started — added to `docker compose up` lifecycle

---

## API Reference

### POST /api/v1/payments/initiate
**Request:**
```json
{
  "paymentId": "PAY-20260327-001",
  "debtorAccount": "DE89370400440532013000",
  "debtorName": "John Doe",
  "creditorAccount": "FR1420041010050500013M02606",
  "creditorName": "Jane Smith",
  "amount": 1500.75,
  "currency": "EUR",
  "paymentPurpose": "Invoice Payment",
  "remittanceInformation": "INV-2024-001",
  "requestedExecutionDate": "2026-03-28",
  "priority": "HIGH"
}
```
**Success Response (200):**
```json
{
  "status": "SUCCESS",
  "paymentStatus": "RECEIVED",
  "paymentId": "PAY-20260327-001",
  "kafkaMessageId": "uuid",
  "databaseId": "mongo-object-id",
  "message": "Payment processed successfully",
  "statusCode": 200
}
```
**Duplicate Response (409):**
```json
{
  "status": "DUPLICATE",
  "paymentId": "PAY-20260327-001",
  "message": "Duplicate payment rejected: paymentId 'PAY-20260327-001' already exists",
  "statusCode": 409
}
```

### GET /api/v1/payments/{paymentId}
Returns the full MongoDB payment document including status, timestamps, and kafkaMessageId.

---

## Infrastructure

| Service     | Port  | Credentials         |
|-------------|-------|---------------------|
| App (HTTP)  | 8080  | —                   |
| MongoDB     | 27017 | admin / password    |
| Kafka       | 9092  | —                   |
| Kafka UI    | 8088  | http://localhost:8088 |
