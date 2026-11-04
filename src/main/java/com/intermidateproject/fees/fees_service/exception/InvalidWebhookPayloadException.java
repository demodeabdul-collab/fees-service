package com.intermidateproject.fees.fees_service.exception;

public class InvalidWebhookPayloadException extends RuntimeException {
    public InvalidWebhookPayloadException(String message) {
        super(message);
    }
}
