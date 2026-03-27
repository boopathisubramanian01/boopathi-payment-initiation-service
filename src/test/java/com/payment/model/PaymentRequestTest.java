package com.payment.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PaymentRequest Validation Tests")
class PaymentRequestTest {

    @Test
    @DisplayName("Should create valid payment request")
    void testValidPaymentRequest() {
        // Arrange & Act
        PaymentRequest request = PaymentRequest.builder()
                .paymentId("PAY001")
                .debtorAccount("DE89370400440532013000")
                .debtorName("John Doe")
                .creditorAccount("FR1420041010050500013M02606")
                .creditorName("Jane Smith")
                .amount(new BigDecimal("1000.50"))
                .currency("EUR")
                .paymentPurpose("Invoice")
                .build();

        // Assert
        assertNotNull(request);
        assertEquals("PAY001", request.getPaymentId());
        assertEquals("John Doe", request.getDebtorName());
        assertEquals(new BigDecimal("1000.50"), request.getAmount());
        assertEquals("EUR", request.getCurrency());
        assertEquals("NORM", request.getPriority()); // Default value
    }

    @Test
    @DisplayName("Should accept optional remittance information")
    void testOptionalRemittanceInformation() {
        // Arrange & Act
        PaymentRequest request = PaymentRequest.builder()
                .paymentId("PAY002")
                .debtorAccount("DE89370400440532013000")
                .debtorName("John Doe")
                .creditorAccount("FR1420041010050500013M02606")
                .creditorName("Jane Smith")
                .amount(new BigDecimal("500.00"))
                .currency("EUR")
                .paymentPurpose("Transfer")
                .remittanceInformation("Optional Info")
                .build();

        // Assert
        assertEquals("Optional Info", request.getRemittanceInformation());
    }

    @Test
    @DisplayName("Should accept different priority levels")
    void testDifferentPriorities() {
        // Test HIGH priority
        PaymentRequest highPriority = PaymentRequest.builder()
                .paymentId("PAY003")
                .debtorAccount("DE89370400440532013000")
                .debtorName("John Doe")
                .creditorAccount("FR1420041010050500013M02606")
                .creditorName("Jane Smith")
                .amount(new BigDecimal("1000.00"))
                .currency("EUR")
                .paymentPurpose("Urgent")
                .priority("HIGH")
                .build();

        assertEquals("HIGH", highPriority.getPriority());

        // Test LOW priority
        PaymentRequest lowPriority = PaymentRequest.builder()
                .paymentId("PAY004")
                .debtorAccount("DE89370400440532013000")
                .debtorName("John Doe")
                .creditorAccount("FR1420041010050500013M02606")
                .creditorName("Jane Smith")
                .amount(new BigDecimal("1000.00"))
                .currency("EUR")
                .paymentPurpose("Non-urgent")
                .priority("LOW")
                .build();

        assertEquals("LOW", lowPriority.getPriority());
    }

    @Test
    @DisplayName("Should accept various ISO 4217 currencies")
    void testIso4217Currencies() {
        String[] currencies = {"USD", "GBP", "JPY", "CHF", "CAD", "AUD"};

        for (String currency : currencies) {
            PaymentRequest request = PaymentRequest.builder()
                    .paymentId("PAY-" + currency)
                    .debtorAccount("DE89370400440532013000")
                    .debtorName("John Doe")
                    .creditorAccount("FR1420041010050500013M02606")
                    .creditorName("Jane Smith")
                    .amount(new BigDecimal("1000.00"))
                    .currency(currency)
                    .paymentPurpose("Multi-currency")
                    .build();

            assertEquals(currency, request.getCurrency());
        }
    }

    @Test
    @DisplayName("Should handle small amounts")
    void testSmallAmounts() {
        PaymentRequest request = PaymentRequest.builder()
                .paymentId("PAY005")
                .debtorAccount("DE89370400440532013000")
                .debtorName("John Doe")
                .creditorAccount("FR1420041010050500013M02606")
                .creditorName("Jane Smith")
                .amount(new BigDecimal("0.01"))
                .currency("EUR")
                .paymentPurpose("Small transfer")
                .build();

        assertEquals(new BigDecimal("0.01"), request.getAmount());
    }

    @Test
    @DisplayName("Should handle large amounts")
    void testLargeAmounts() {
        PaymentRequest request = PaymentRequest.builder()
                .paymentId("PAY006")
                .debtorAccount("DE89370400440532013000")
                .debtorName("John Doe")
                .creditorAccount("FR1420041010050500013M02606")
                .creditorName("Jane Smith")
                .amount(new BigDecimal("9999999.99"))
                .currency("EUR")
                .paymentPurpose("Large transfer")
                .build();

        assertEquals(new BigDecimal("9999999.99"), request.getAmount());
    }
}
