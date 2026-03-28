package com.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentStatusMessage {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("originalPaymentId")
    private String originalPaymentId;

    @JsonProperty("transactionStatus")
    private String transactionStatus;

    @JsonProperty("statusReasonDescription")
    private String statusReasonDescription;

    @JsonProperty("processedAt")
    private LocalDateTime processedAt;

    public PaymentStatusMessage() {
    }

    public PaymentStatusMessage(String messageId, String originalPaymentId, String transactionStatus,
                                String statusReasonDescription, LocalDateTime processedAt) {
        this.messageId = messageId;
        this.originalPaymentId = originalPaymentId;
        this.transactionStatus = transactionStatus;
        this.statusReasonDescription = statusReasonDescription;
        this.processedAt = processedAt;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getOriginalPaymentId() {
        return originalPaymentId;
    }

    public void setOriginalPaymentId(String originalPaymentId) {
        this.originalPaymentId = originalPaymentId;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getStatusReasonDescription() {
        return statusReasonDescription;
    }

    public void setStatusReasonDescription(String statusReasonDescription) {
        this.statusReasonDescription = statusReasonDescription;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
