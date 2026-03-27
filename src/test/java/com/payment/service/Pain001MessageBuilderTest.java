package com.payment.service;

import com.payment.model.Payment;
import com.payment.model.PaymentRequest;
import com.payment.model.Pain001Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PAIN 001 Message Builder Tests")
class Pain001MessageBuilderTest {

    private Pain001MessageBuilder builder;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        builder = new Pain001MessageBuilder();

        testPayment = Payment.builder()
                .paymentId("TEST001")
                .debtorAccount("DE89370400440532013000")
                .debtorName("John Doe")
                .creditorAccount("FR1420041010050500013M02606")
                .creditorName("Jane Smith")
                .amount(new BigDecimal("1000.50"))
                .currency("EUR")
                .paymentPurpose("Invoice Payment")
                .remittanceInformation("INV-2024-001")
                .priority("NORM")
                .build();
    }

    @Test
    @DisplayName("Should build valid PAIN 001 message from payment")
    void testBuildPain001Message() {
        // Arrange
        // testPayment is already prepared in setUp()

        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);

        // Assert
        assertNotNull(message, "PAIN 001 message should not be null");
        assertNotNull(message.getMessageId(), "Message ID should be generated");
        assertNotNull(message.getCreationDateTime(), "Creation date time should be set");
        assertEquals("pain.001.003.09", message.getVersion());
        assertEquals(1, message.getNumberOfTransactions());
        assertEquals(new BigDecimal("1000.50"), message.getControlSum());
    }

    @Test
    @DisplayName("Should set initiating party correctly")
    void testInitiatingParty() {
        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);

        // Assert
        assertNotNull(message.getInitiatingParty());
        assertEquals("John Doe", message.getInitiatingParty().getName());
        assertEquals("DE89370400440532013000", message.getInitiatingParty().getId());
    }

    @Test
    @DisplayName("Should set payment information correctly")
    void testPaymentInformation() {
        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);
        Pain001Message.PaymentInformation paymentInfo = message.getPaymentInformation();

        // Assert
        assertNotNull(paymentInfo);
        assertEquals("TRF", paymentInfo.getPaymentMethod());
        assertFalse(paymentInfo.isBatchBooking());
        assertEquals(1, paymentInfo.getNumberOfTransactions());
        assertEquals(new BigDecimal("1000.50"), paymentInfo.getControlSum());
    }

    @Test
    @DisplayName("Should set debtor information correctly")
    void testDebtorInformation() {
        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);
        Pain001Message.PaymentInformation paymentInfo = message.getPaymentInformation();

        // Assert
        assertNotNull(paymentInfo.getDebtor());
        assertEquals("John Doe", paymentInfo.getDebtor().getName());
        assertEquals("DE89370400440532013000", paymentInfo.getDebtor().getIdentification());

        assertNotNull(paymentInfo.getDebtorAccount());
        assertEquals("DE89370400440532013000", paymentInfo.getDebtorAccount().getIban());
        assertEquals("EUR", paymentInfo.getDebtorAccount().getCurrency());
    }

    @Test
    @DisplayName("Should set credit transfer transaction correctly")
    void testCreditTransferTransaction() {
        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);
        Pain001Message.CreditTransferTransaction transaction =
                message.getPaymentInformation().getCreditTransferTransaction();

        // Assert
        assertNotNull(transaction);
        assertEquals("TEST001", transaction.getPaymentId());

        // Check amount
        assertNotNull(transaction.getInstructedAmount());
        assertEquals("EUR", transaction.getInstructedAmount().getCurrency());
        assertEquals(new BigDecimal("1000.50"), transaction.getInstructedAmount().getValue());

        // Check creditor
        assertNotNull(transaction.getCreditor());
        assertEquals("Jane Smith", transaction.getCreditor().getName());
        assertEquals("FR1420041010050500013M02606", transaction.getCreditor().getIdentification());

        // Check creditor account
        assertNotNull(transaction.getCreditorAccount());
        assertEquals("FR1420041010050500013M02606", transaction.getCreditorAccount().getIban());
        assertEquals("EUR", transaction.getCreditorAccount().getCurrency());
    }

    @Test
    @DisplayName("Should set remittance information correctly")
    void testRemittanceInformation() {
        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);
        Pain001Message.RemittanceInfo remittanceInfo =
                message.getPaymentInformation().getCreditTransferTransaction().getRemittanceInformation();

        // Assert
        assertNotNull(remittanceInfo);
        assertEquals("INV-2024-001", remittanceInfo.getUnstructured());
    }

    @Test
    @DisplayName("Should use payment purpose when remittance info is not provided")
    void testRemittanceInfoDefaultsToPaymentPurpose() {
        // Arrange
        testPayment.setRemittanceInformation(null);

        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);
        Pain001Message.RemittanceInfo remittanceInfo =
                message.getPaymentInformation().getCreditTransferTransaction().getRemittanceInformation();

        // Assert
        assertEquals("Invoice Payment", remittanceInfo.getUnstructured());
    }

    @Test
    @DisplayName("Should set payment type information with priority")
    void testPaymentTypeInformation() {
        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);
        Pain001Message.PaymentTypeInfo typeInfo =
                message.getPaymentInformation().getPaymentTypeInformation();

        // Assert
        assertNotNull(typeInfo);
        assertEquals("NORM", typeInfo.getInstructionPriority());
        assertEquals("SEPA", typeInfo.getServiceLevel());
    }

    @Test
    @DisplayName("Should handle HIGH priority payment")
    void testHighPriorityPayment() {
        // Arrange
        testPayment.setPriority("HIGH");

        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);

        // Assert
        assertEquals("HIGH", message.getPaymentInformation().getPaymentTypeInformation().getInstructionPriority());
    }

    @Test
    @DisplayName("Should handle null priority and default to NORM")
    void testNullPriorityDefaultsToNorm() {
        // Arrange
        testPayment.setPriority(null);

        // Act
        Pain001Message message = builder.buildPain001Message(testPayment);

        // Assert
        assertEquals("NORM", message.getPaymentInformation().getPaymentTypeInformation().getInstructionPriority());
    }
}
