package com.payment.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
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
    @Builder.Default
    private String priority = "NORM"; // NORM, HIGH, LOW

    @JsonProperty("debtorRoutingNumber")
    private String debtorRoutingNumber;

    @JsonProperty("creditorRoutingNumber")
    private String creditorRoutingNumber;

}
