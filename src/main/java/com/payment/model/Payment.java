package com.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    private String paymentId;
    private String debtorAccount;
    private String debtorName;
    private String creditorAccount;
    private String creditorName;
    private BigDecimal amount;
    private String currency;
    private String paymentPurpose;
    private String remittanceInformation;
    private String executionDate;
    private String requestedExecutionDate;
    private String priority;
    private String debtorRoutingNumber;
    private String creditorRoutingNumber;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String kafkaMessageId;

    public enum PaymentStatus {
        PROCESSING,
        VALIDATED,
        STORED,
        KAFKA_SENT,
        RECEIVED,       // Kafka broker acknowledged - message committed
        FAILED,
        DUPLICATE
    }
}
