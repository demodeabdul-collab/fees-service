package com.intermidateproject.fees.fees_service.dto;

import com.intermidateproject.fees.fees_service.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ReconciliationReportResponse {
    private List<Payment> payments;
    private BigDecimal totalAmount;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    public ReconciliationReportResponse(List<Payment> payments,BigDecimal totalAmount, LocalDateTime periodStart, LocalDateTime periodEnd){
        this.payments = payments;
        this.totalAmount = totalAmount;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }
}
