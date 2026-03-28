package com.payment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.model.PaymentStatusMessage;
import com.payment.service.PaymentService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class Pain002Consumer {

    private static final Logger log = LoggerFactory.getLogger(Pain002Consumer.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentService paymentService;

    @KafkaListener(
            topics = "${spring.kafka.topic.pain002}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, String> record) {
        try {
            PaymentStatusMessage statusMessage = objectMapper.readValue(record.value(), PaymentStatusMessage.class);

            if (statusMessage.getOriginalPaymentId() == null || statusMessage.getOriginalPaymentId().isBlank()) {
                log.warn("[PAIN002-CONSUMER] Ignoring status message with missing originalPaymentId | key={}", record.key());
                return;
            }

            log.info("[PAIN002-CONSUMER] Received status update | paymentId={} | status={} | topic={} | partition={} | offset={}",
                    statusMessage.getOriginalPaymentId(),
                    statusMessage.getTransactionStatus(),
                    record.topic(),
                    record.partition(),
                    record.offset());

            paymentService.updatePaymentStatusFromPain002(statusMessage);

        } catch (Exception e) {
            log.error("[PAIN002-CONSUMER] Failed to process PAIN 002 message | key={} | error={}",
                    record.key(), e.getMessage(), e);
        }
    }
}
