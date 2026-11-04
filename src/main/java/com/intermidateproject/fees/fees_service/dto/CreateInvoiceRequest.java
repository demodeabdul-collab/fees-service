package com.intermidateproject.fees.fees_service.dto;

import java.math.BigDecimal;

public class CreateInvoiceRequest {
    private Long studentId;
    private BigDecimal totalAmount;

    public CreateInvoiceRequest() {}

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
