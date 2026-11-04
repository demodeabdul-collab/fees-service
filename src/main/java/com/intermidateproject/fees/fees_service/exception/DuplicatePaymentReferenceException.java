package com.intermidateproject.fees.fees_service.exception;

public class DuplicatePaymentReferenceException extends RuntimeException {
    public DuplicatePaymentReferenceException(String message) {
        super(message);
    }
}
