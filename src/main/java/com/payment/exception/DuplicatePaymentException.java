package com.payment.exception;

public class DuplicatePaymentException extends RuntimeException {

    private final String paymentId;

    public DuplicatePaymentException(String paymentId) {
        super("Duplicate payment rejected: paymentId '" + paymentId + "' already exists");
        this.paymentId = paymentId;
    }

    public String getPaymentId() {
        return paymentId;
    }
}
