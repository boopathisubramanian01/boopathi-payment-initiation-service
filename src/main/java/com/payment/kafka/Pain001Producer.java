package com.payment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.model.Pain001Message;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class Pain001Producer {

        private static final Logger log = LoggerFactory.getLogger(Pain001Producer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.kafka.topic.pain001}")
    private String topicName;

    /**
     * Publishes a PAIN 001 message to Kafka and waits for broker acknowledgment.
     * Returns the Kafka message ID only after the broker confirms the message is committed.
     */
    public String publishPain001Message(Pain001Message message) {
        String messageId = message.getMessageId();
        String paymentId = message.getPaymentInformation() != null
                ? message.getPaymentInformation().getCreditTransferTransaction() != null
                    ? message.getPaymentInformation().getCreditTransferTransaction().getPaymentId()
                    : messageId
                : messageId;

        log.info("[KAFKA] Initiating publish | paymentId={} | messageId={} | topic={}",
                paymentId, messageId, topicName);

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            log.debug("[KAFKA] Serialized PAIN 001 message | paymentId={} | size={} bytes",
                    paymentId, jsonMessage.length());

            // Send and block until Kafka broker acknowledges (commit confirmed)
            log.info("[KAFKA] Sending message to broker | paymentId={} | messageId={}", paymentId, messageId);
            SendResult<String, String> result = kafkaTemplate
                    .send(topicName, messageId, jsonMessage)
                    .get(10, TimeUnit.SECONDS);

            RecordMetadata metadata = result.getRecordMetadata();
            log.info("[KAFKA] Broker ACK received | paymentId={} | messageId={} | topic={} | partition={} | offset={}",
                    paymentId, messageId,
                    metadata.topic(), metadata.partition(), metadata.offset());

            return messageId;

        } catch (Exception e) {
            log.error("[KAFKA] Failed to publish message | paymentId={} | messageId={} | error={}",
                    paymentId, messageId, e.getMessage(), e);
            throw new RuntimeException("Kafka publishing failed: " + e.getMessage(), e);
        }
    }
}
