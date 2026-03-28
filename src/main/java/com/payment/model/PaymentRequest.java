package com.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PaymentRequest {

    @NotBlank(message = "Payment ID is required")
    @JsonProperty("paymentId")
    private String paymentId;

    @NotBlank(message = "Debtor account is required")
    @JsonProperty("debtorAccount")
    private String debtorAccount;

    @NotBlank(message = "Debtor name is required")
    @JsonProperty("debtorName")
    private String debtorName;

    @NotBlank(message = "Creditor account is required")
    @JsonProperty("creditorAccount")
    private String creditorAccount;

    @NotBlank(message = "Creditor name is required")
    @JsonProperty("creditorName")
    private String creditorName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be ISO 4217 code (e.g., EUR, USD)")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "Payment purpose is required")
    @JsonProperty("paymentPurpose")
    private String paymentPurpose;

    @JsonProperty("remittanceInformation")
    private String remittanceInformation;

    @JsonProperty("executionDate")
    private String executionDate;

    @JsonProperty("requestedExecutionDate")
    private String requestedExecutionDate;

    @JsonProperty("priority")
    private String priority = "NORM"; // NORM, HIGH, LOW

    @JsonProperty("debtorRoutingNumber")
    @NotBlank(message = "Debtor routing number is required")
    @Pattern(regexp = "^\\d{9}$", message = "Debtor routing number must be a 9-digit ABA")
    private String debtorRoutingNumber;

    @JsonProperty("creditorRoutingNumber")
    @NotBlank(message = "Creditor routing number is required")
    @Pattern(regexp = "^\\d{9}$", message = "Creditor routing number must be a 9-digit ABA")
    private String creditorRoutingNumber;

    public PaymentRequest() {
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final PaymentRequest target = new PaymentRequest();

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

        public PaymentRequest build() {
            return target;
        }
    }

}
