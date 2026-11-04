package com.intermidateproject.fees.fees_service.exception;

public class OverpaymentException extends RuntimeException {
    public OverpaymentException(String message) {
        super(message);
    }
}
