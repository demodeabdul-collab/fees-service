package com.intermidateproject.fees.fees_service.controller;

import com.intermidateproject.fees.fees_service.dto.WebhookPaymentPayload;
import com.intermidateproject.fees.fees_service.entity.Payment;
import com.intermidateproject.fees.fees_service.exception.InvalidSignatureException;
import com.intermidateproject.fees.fees_service.exception.InvalidWebhookPayloadException;
import com.intermidateproject.fees.fees_service.service.PaymentService;
import com.intermidateproject.fees.fees_service.util.HmacUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/webhooks")
public class WebhookController {

    private final PaymentService paymentService;
    private final JsonMapper jsonMapper;
    private final String webhookSecret;

    public WebhookController(PaymentService paymentService,
                             JsonMapper jsonMapper,
                             @Value("${webhook.secret}") String webhookSecret) {
        this.paymentService = paymentService;
        this.jsonMapper = jsonMapper;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/payment")
    public ResponseEntity<Payment> receivePaymentWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Signature", required = false) String signature) {

        // 1. Verify the signature against the RAW body, before parsing anything
        if (!HmacUtil.isValidSignature(rawBody, webhookSecret, signature)) {
            throw new InvalidSignatureException("Invalid or missing webhook signature");
        }

        // 2. Only now parse the JSON
        WebhookPaymentPayload payload;
        try {
            payload = jsonMapper.readValue(rawBody, WebhookPaymentPayload.class);
        } catch (JacksonException e) {
            throw new InvalidWebhookPayloadException("Webhook payload is not valid JSON");
        }

        if (payload.getInvoiceId() == null || payload.getAmount() == null
                || payload.getReference() == null || payload.getIdempotencyKey() == null) {
            throw new InvalidWebhookPayloadException(
                    "Webhook payload must include invoiceId, amount, reference and idempotencyKey");
        }

        // 3. Reuse the existing, fully guarded payment logic
        Payment payment = paymentService.createPayment(
                payload.getInvoiceId(),
                payload.getAmount(),
                payload.getReference(),
                payload.getIdempotencyKey(),
                signature);

        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}