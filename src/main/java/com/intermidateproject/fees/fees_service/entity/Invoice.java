package com.intermidateproject.fees.fees_service.entity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   @ManyToOne
   @JoinColumn(name = "student_id", nullable = false)
    private Student student;

   @Column(name = "total_amount", nullable = false, precision = 14, scale =2)
   private BigDecimal totalAmount;

   @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
   private LocalDateTime createdAt;

   protected Invoice() {}

    public Invoice(Student student, BigDecimal totalAmount) {
       this.student = student;
        this.totalAmount = totalAmount;
        status = "UNPAID";
   }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {return id;}
    public BigDecimal getTotalAmount() {return totalAmount;}
    public Student getStudent() {return student;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}



}
