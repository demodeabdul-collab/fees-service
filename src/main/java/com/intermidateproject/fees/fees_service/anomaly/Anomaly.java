package com.intermidateproject.fees.fees_service.anomaly;

public class Anomaly {
    private  final AnomalyType type;
    private final Long invoiceId;
    private final String details;

    public Anomaly(AnomalyType type, Long invoiceId, String details) {
        this.type = type;
        this.invoiceId = invoiceId;
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public AnomalyType getType() {
        return type;
    }
}
