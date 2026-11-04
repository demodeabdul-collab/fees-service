package com.intermidateproject.fees.fees_service.dto;

import java.math.BigDecimal;

public class WebhookPaymentPayload {
    private Long invoiceId;
    private BigDecimal amount;
    private String reference;
    private String idempotencyKey;

    public WebhookPaymentPayload() {}

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}