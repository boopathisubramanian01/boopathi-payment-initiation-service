# Quick Start Guide

## Option 1: Using Docker Compose (Recommended)

### Prerequisites
- Docker & Docker Compose installed

### Steps

1. **Start all services**
```bash
docker-compose up -d
```

2. **Wait for services to be healthy**
```bash
docker-compose ps
```

Expected output:
```
CONTAINER ID   IMAGE                          COMMAND                  STATUS
...            confluentinc/cp-kafka          ...                      Up (healthy)
...            confluentinc/cp-zookeeper      ...                      Up (healthy)
...            mongo:latest                   ...                      Up (healthy)
```

3. **Build the Spring Boot application**
```bash
mvn clean install -DskipTests
```

4. **Run the application**
```bash
mvn spring-boot:run
```

5. **Verify the service is running**
```bash
curl http://localhost:8080/api/v1/payments/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "Payment Initiation Service"
}
```

6. **Test the payment endpoint**
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "TEST001",
    "debtorAccount": "DE89370400440532013000",
    "debtorName": "Test Debtor",
    "creditorAccount": "FR1420041010050500013M02606",
    "creditorName": "Test Creditor",
    "amount": 100.00,
    "currency": "EUR",
    "paymentPurpose": "Test Payment"
  }'
```

7. **Monitor Kafka messages**
Visit Kafka UI: http://localhost:8081
- Topic: `payment.pain001`
- View published PAIN 001 messages in real-time

---

## Option 2: Manual Setup

### Prerequisites

#### MongoDB
```bash
# macOS (Homebrew)
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb-community

# Linux (Ubuntu)
sudo apt-get install -y mongodb
sudo systemctl start mongodb

# Verify
mongosh
```

#### Kafka
```bash
# macOS (Homebrew)
brew install kafka

# Start Zookeeper
zookeeper-server-start.sh /usr/local/etc/kafka/zookeeper.properties &

# Start Kafka
kafka-server-start.sh /usr/local/etc/kafka/server.properties &

# Create topic
kafka-topics.sh --create \
  --topic payment.pain001 \
  --bootstrap-server localhost:9092 \
  --partitions 1 \
  --replication-factor 1
```

### Build & Run Application
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

---

## Accessing Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Spring App | http://localhost:8080 | - |
| Kafka UI | http://localhost:8081 | - |
| MongoDB | mongodb://localhost:27017 | admin/password |
| MongoDB Shell | mongosh | - |

---

## Testing the Complete Flow

### 1. Send Payment Request
```bash
curl -X POST http://localhost:8080/api/v1/payments/initiate \
  -H "Content-Type: application/json" \
  -d '{
    "paymentId": "FLOW001",
    "debtorAccount": "DE89370400440532013000",
    "debtorName": "John Doe",
    "creditorAccount": "FR1420041010050500013M02606",
    "creditorName": "Jane Smith",
    "amount": 500.00,
    "currency": "EUR",
    "paymentPurpose": "Invoice",
    "priority": "HIGH"
  }'
```

### 2. Check MongoDB - Payment Stored
```bash
mongosh
use payment_db
db.payments.findOne({ paymentId: "FLOW001" })
```

Output should show:
- Payment with all details
- Status: `STORED` then `KAFKA_SENT`
- kafkaMessageId populated

### 3. Check Kafka - Message Published
Visit http://localhost:8081 (Kafka UI):
1. Go to Topics → `payment.pain001`
2. View the PAIN 001 JSON message
3. Verify message contains ISO format data

---

## Troubleshooting

### MongoDB Connection Error
```
Error: Unable to connect to MongoDB
```
**Solution:**
```bash
# Check if MongoDB is running
mongosh
# If not running, start it
docker-compose up -d mongodb
```

### Kafka Connection Error
```
Error: Could not connect to Kafka broker
```
**Solution:**
```bash
# Check if Kafka is running
docker-compose ps
# If not, restart
docker-compose restart kafka zookeeper
```

### Application Startup Error
```
Error: Spring Boot failed to start
```
**Solution:**
1. Check MongoDB and Kafka are running
2. Verify ports (8080, 27017, 9092) are available
3. Check logs: `mvn spring-boot:run`

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080
# Kill process
kill -9 <PID>
```

---

## Stopping Services

### Option 1: Docker Compose
```bash
docker-compose stop
docker-compose down          # Also removes containers
docker-compose down -v       # Also removes volumes
```

### Option 2: Manual
```bash
# Kill Spring Boot
Ctrl+C (in terminal)

# Stop Kafka
kafka-server-stop.sh

# Stop Zookeeper
zookeeper-server-stop.sh

# Stop MongoDB
brew services stop mongodb-community   # macOS
sudo systemctl stop mongodb            # Linux
```

---

## View Logs

### Spring Boot Logs
```bash
# Real-time with Maven
mvn spring-boot:run

# From jar file
java -jar target/payment-initiation-service-1.0.0.jar

# Docker logs
docker-compose logs -f payment-app
```

### MongoDB Logs
```bash
docker-compose logs -f mongodb
```

### Kafka Logs
```bash
docker-compose logs -f kafka
```

---

## Development Tips

### Hot Reload
Enable Spring Boot DevTools in IDE:
```bash
mvn spring-boot:run
# Code changes auto-reload
```

### Debug Mode
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

### Clear MongoDB
```bash
mongosh
use payment_db
db.payments.deleteMany({})   # Delete all payments
db.dropDatabase()            # Drop entire database
```

### Reset Kafka Topic
```bash
kafka-topics.sh --delete --topic payment.pain001 --bootstrap-server localhost:9092
kafka-topics.sh --create --topic payment.pain001 --bootstrap-server localhost:9092
```

---

## Next Steps

1. ✅ Services running
2. ✅ Payment API responding
3. ✅ Data persisting in MongoDB
4. ✅ Messages in Kafka
5. → Start processing payments!

For detailed API documentation, see `README.md`
For test examples, see `TEST_EXAMPLES.md`
