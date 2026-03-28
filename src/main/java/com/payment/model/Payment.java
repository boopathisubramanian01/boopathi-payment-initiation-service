package com.payment.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    private String pain002MessageId;
    private String pain002StatusReason;
    private LocalDateTime pain002ProcessedAt;

    public Payment() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(String debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public String getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(String creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentPurpose() {
        return paymentPurpose;
    }

    public void setPaymentPurpose(String paymentPurpose) {
        this.paymentPurpose = paymentPurpose;
    }

    public String getRemittanceInformation() {
        return remittanceInformation;
    }

    public void setRemittanceInformation(String remittanceInformation) {
        this.remittanceInformation = remittanceInformation;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(String executionDate) {
        this.executionDate = executionDate;
    }

    public String getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(String requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDebtorRoutingNumber() {
        return debtorRoutingNumber;
    }

    public void setDebtorRoutingNumber(String debtorRoutingNumber) {
        this.debtorRoutingNumber = debtorRoutingNumber;
    }

    public String getCreditorRoutingNumber() {
        return creditorRoutingNumber;
    }

    public void setCreditorRoutingNumber(String creditorRoutingNumber) {
        this.creditorRoutingNumber = creditorRoutingNumber;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getKafkaMessageId() {
        return kafkaMessageId;
    }

    public void setKafkaMessageId(String kafkaMessageId) {
        this.kafkaMessageId = kafkaMessageId;
    }

    public String getPain002MessageId() {
        return pain002MessageId;
    }

    public void setPain002MessageId(String pain002MessageId) {
        this.pain002MessageId = pain002MessageId;
    }

    public String getPain002StatusReason() {
        return pain002StatusReason;
    }

    public void setPain002StatusReason(String pain002StatusReason) {
        this.pain002StatusReason = pain002StatusReason;
    }

    public LocalDateTime getPain002ProcessedAt() {
        return pain002ProcessedAt;
    }

    public void setPain002ProcessedAt(LocalDateTime pain002ProcessedAt) {
        this.pain002ProcessedAt = pain002ProcessedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Payment target = new Payment();

        public Builder id(String id) {
            target.setId(id);
            return this;
        }

        public Builder paymentId(String paymentId) {
            target.setPaymentId(paymentId);
            return this;
        }

        public Builder debtorAccount(String debtorAccount) {
            target.setDebtorAccount(debtorAccount);
            return this;
        }

        public Builder debtorName(String debtorName) {
            target.setDebtorName(debtorName);
            return this;
        }

        public Builder creditorAccount(String creditorAccount) {
            target.setCreditorAccount(creditorAccount);
            return this;
        }

        public Builder creditorName(String creditorName) {
            target.setCreditorName(creditorName);
            return this;
        }

        public Builder amount(BigDecimal amount) {
            target.setAmount(amount);
            return this;
        }

        public Builder currency(String currency) {
            target.setCurrency(currency);
            return this;
        }

        public Builder paymentPurpose(String paymentPurpose) {
            target.setPaymentPurpose(paymentPurpose);
            return this;
        }

        public Builder remittanceInformation(String remittanceInformation) {
            target.setRemittanceInformation(remittanceInformation);
            return this;
        }

        public Builder executionDate(String executionDate) {
            target.setExecutionDate(executionDate);
            return this;
        }

        public Builder requestedExecutionDate(String requestedExecutionDate) {
            target.setRequestedExecutionDate(requestedExecutionDate);
            return this;
        }

        public Builder priority(String priority) {
            target.setPriority(priority);
            return this;
        }

        public Builder debtorRoutingNumber(String debtorRoutingNumber) {
            target.setDebtorRoutingNumber(debtorRoutingNumber);
            return this;
        }

        public Builder creditorRoutingNumber(String creditorRoutingNumber) {
            target.setCreditorRoutingNumber(creditorRoutingNumber);
            return this;
        }

        public Builder status(PaymentStatus status) {
            target.setStatus(status);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            target.setCreatedAt(createdAt);
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            target.setUpdatedAt(updatedAt);
            return this;
        }

        public Builder kafkaMessageId(String kafkaMessageId) {
            target.setKafkaMessageId(kafkaMessageId);
            return this;
        }

        public Builder pain002MessageId(String pain002MessageId) {
            target.setPain002MessageId(pain002MessageId);
            return this;
        }

        public Builder pain002StatusReason(String pain002StatusReason) {
            target.setPain002StatusReason(pain002StatusReason);
            return this;
        }

        public Builder pain002ProcessedAt(LocalDateTime pain002ProcessedAt) {
            target.setPain002ProcessedAt(pain002ProcessedAt);
            return this;
        }

        public Payment build() {
            return target;
        }
    }

    public enum PaymentStatus {
        PROCESSING,
        VALIDATED,
        STORED,
        KAFKA_SENT,
        RCVD,
        RECEIVED,
        ACTC,
        ACCP,
        ACSP,
        ACSC,
        RGTC,
        SCHD,
        PDNG,
        RJCT,
        CANC,
        FAILED,
        DUPLICATE
    }
}
