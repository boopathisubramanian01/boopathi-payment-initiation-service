package com.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pain001Message {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("creationDateTime")
    private LocalDateTime creationDateTime;

    @JsonProperty("numberOfTransactions")
    private int numberOfTransactions;

    @JsonProperty("controlSum")
    private BigDecimal controlSum;

    @JsonProperty("initiatingParty")
    private InitiatingParty initiatingParty;

    @JsonProperty("paymentInformation")
    private PaymentInformation paymentInformation;

    @JsonProperty("version")
    private String version = "pain.001.003.09";

    public Pain001Message() {
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public LocalDateTime getCreationDateTime() {
        return creationDateTime;
    }

    public void setCreationDateTime(LocalDateTime creationDateTime) {
        this.creationDateTime = creationDateTime;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public void setNumberOfTransactions(int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
    }

    public BigDecimal getControlSum() {
        return controlSum;
    }

    public void setControlSum(BigDecimal controlSum) {
        this.controlSum = controlSum;
    }

    public InitiatingParty getInitiatingParty() {
        return initiatingParty;
    }

    public void setInitiatingParty(InitiatingParty initiatingParty) {
        this.initiatingParty = initiatingParty;
    }

    public PaymentInformation getPaymentInformation() {
        return paymentInformation;
    }

    public void setPaymentInformation(PaymentInformation paymentInformation) {
        this.paymentInformation = paymentInformation;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public static class InitiatingParty {
        @JsonProperty("name")
        private String name;
        @JsonProperty("id")
        private String id;

        public InitiatingParty() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class PaymentInformation {
        @JsonProperty("paymentInformationId")
        private String paymentInformationId;
        @JsonProperty("paymentMethod")
        private String paymentMethod; // TRF for transfer
        @JsonProperty("batchBooking")
        private boolean batchBooking;
        @JsonProperty("numberOfTransactions")
        private int numberOfTransactions;
        @JsonProperty("controlSum")
        private BigDecimal controlSum;
        @JsonProperty("paymentTypeInformation")
        private PaymentTypeInfo paymentTypeInformation;
        @JsonProperty("debtor")
        private Party debtor;
        @JsonProperty("debtorAccount")
        private Account debtorAccount;
        @JsonProperty("debtorRoutingNumber")
        private String debtorRoutingNumber;
        @JsonProperty("creditorRoutingNumber")
        private String creditorRoutingNumber;
        @JsonProperty("creditTransferTransaction")
        private CreditTransferTransaction creditTransferTransaction;

        public PaymentInformation() {
        }

        public String getPaymentInformationId() {
            return paymentInformationId;
        }

        public void setPaymentInformationId(String paymentInformationId) {
            this.paymentInformationId = paymentInformationId;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public boolean isBatchBooking() {
            return batchBooking;
        }

        public void setBatchBooking(boolean batchBooking) {
            this.batchBooking = batchBooking;
        }

        public int getNumberOfTransactions() {
            return numberOfTransactions;
        }

        public void setNumberOfTransactions(int numberOfTransactions) {
            this.numberOfTransactions = numberOfTransactions;
        }

        public BigDecimal getControlSum() {
            return controlSum;
        }

        public void setControlSum(BigDecimal controlSum) {
            this.controlSum = controlSum;
        }

        public PaymentTypeInfo getPaymentTypeInformation() {
            return paymentTypeInformation;
        }

        public void setPaymentTypeInformation(PaymentTypeInfo paymentTypeInformation) {
            this.paymentTypeInformation = paymentTypeInformation;
        }

        public Party getDebtor() {
            return debtor;
        }

        public void setDebtor(Party debtor) {
            this.debtor = debtor;
        }

        public Account getDebtorAccount() {
            return debtorAccount;
        }

        public void setDebtorAccount(Account debtorAccount) {
            this.debtorAccount = debtorAccount;
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

        public CreditTransferTransaction getCreditTransferTransaction() {
            return creditTransferTransaction;
        }

        public void setCreditTransferTransaction(CreditTransferTransaction creditTransferTransaction) {
            this.creditTransferTransaction = creditTransferTransaction;
        }
    }

    public static class PaymentTypeInfo {
        @JsonProperty("instructionPriority")
        private String instructionPriority; // NORM, HIGH, LOW
        @JsonProperty("serviceLevel")
        private String serviceLevel; // SEPA

        public PaymentTypeInfo() {
        }

        public String getInstructionPriority() {
            return instructionPriority;
        }

        public void setInstructionPriority(String instructionPriority) {
            this.instructionPriority = instructionPriority;
        }

        public String getServiceLevel() {
            return serviceLevel;
        }

        public void setServiceLevel(String serviceLevel) {
            this.serviceLevel = serviceLevel;
        }
    }

    public static class Party {
        @JsonProperty("name")
        private String name;
        @JsonProperty("identification")
        private String identification;

        public Party() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIdentification() {
            return identification;
        }

        public void setIdentification(String identification) {
            this.identification = identification;
        }
    }

    public static class Account {
        @JsonProperty("iban")
        private String iban;
        @JsonProperty("currency")
        private String currency;

        public Account() {
        }

        public String getIban() {
            return iban;
        }

        public void setIban(String iban) {
            this.iban = iban;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

    public static class CreditTransferTransaction {
        @JsonProperty("paymentId")
        private String paymentId;
        @JsonProperty("instructedAmount")
        private Amount instructedAmount;
        @JsonProperty("creditor")
        private Party creditor;
        @JsonProperty("creditorAccount")
        private Account creditorAccount;
        @JsonProperty("remittanceInformation")
        private RemittanceInfo remittanceInformation;
        @JsonProperty("requestedExecutionDate")
        private String requestedExecutionDate;

        public CreditTransferTransaction() {
        }

        public String getPaymentId() {
            return paymentId;
        }

        public void setPaymentId(String paymentId) {
            this.paymentId = paymentId;
        }

        public Amount getInstructedAmount() {
            return instructedAmount;
        }

        public void setInstructedAmount(Amount instructedAmount) {
            this.instructedAmount = instructedAmount;
        }

        public Party getCreditor() {
            return creditor;
        }

        public void setCreditor(Party creditor) {
            this.creditor = creditor;
        }

        public Account getCreditorAccount() {
            return creditorAccount;
        }

        public void setCreditorAccount(Account creditorAccount) {
            this.creditorAccount = creditorAccount;
        }

        public RemittanceInfo getRemittanceInformation() {
            return remittanceInformation;
        }

        public void setRemittanceInformation(RemittanceInfo remittanceInformation) {
            this.remittanceInformation = remittanceInformation;
        }

        public String getRequestedExecutionDate() {
            return requestedExecutionDate;
        }

        public void setRequestedExecutionDate(String requestedExecutionDate) {
            this.requestedExecutionDate = requestedExecutionDate;
        }
    }

    public static class Amount {
        @JsonProperty("currency")
        private String currency;
        @JsonProperty("value")
        private BigDecimal value;

        public Amount() {
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }
    }

    public static class RemittanceInfo {
        @JsonProperty("unstructured")
        private String unstructured;
        @JsonProperty("structured")
        private String structured;

        public RemittanceInfo() {
        }

        public String getUnstructured() {
            return unstructured;
        }

        public void setUnstructured(String unstructured) {
            this.unstructured = unstructured;
        }

        public String getStructured() {
            return structured;
        }

        public void setStructured(String structured) {
            this.structured = structured;
        }
    }
}
