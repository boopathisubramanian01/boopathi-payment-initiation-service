package com.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InitiatingParty {
        @JsonProperty("name")
        private String name;
        @JsonProperty("id")
        private String id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentTypeInfo {
        @JsonProperty("instructionPriority")
        private String instructionPriority; // NORM, HIGH, LOW
        @JsonProperty("serviceLevel")
        private String serviceLevel; // SEPA
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Party {
        @JsonProperty("name")
        private String name;
        @JsonProperty("identification")
        private String identification;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Account {
        @JsonProperty("iban")
        private String iban;
        @JsonProperty("currency")
        private String currency;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
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
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        @JsonProperty("currency")
        private String currency;
        @JsonProperty("value")
        private BigDecimal value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RemittanceInfo {
        @JsonProperty("unstructured")
        private String unstructured;
        @JsonProperty("structured")
        private String structured;
    }
}
