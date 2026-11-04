package com.intermidateproject.fees.fees_service.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table (name = "payments")
public class Payment {

    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name ="invoice_id", nullable = false)
    private Invoice invoice;

    @Column(name = "amount", nullable = false, precision = 14, scale =2)
    private BigDecimal amount;

    @Column(name = "reference", unique = true, nullable = false)
    private String reference;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Payment() {}

    public Payment(Invoice invoice, BigDecimal amount, String reference) {
        this.invoice = invoice;
        this.amount = amount;
        this.reference = reference;
        status = "CONFIRMED";
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {return id;}
    public Invoice getInvoice() {return invoice;}
    public BigDecimal getAmount() {return amount;}
    public String getReference() {return reference;}
    public String getStatus() {return status;}
    public LocalDateTime getCreatedAt() {return createdAt;}

    public void setStatus(String status) {
        this.status = status;
    }

}
