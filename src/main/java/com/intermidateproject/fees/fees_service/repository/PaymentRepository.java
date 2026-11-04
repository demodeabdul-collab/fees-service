package com.intermidateproject.fees.fees_service.repository;

import com.intermidateproject.fees.fees_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    boolean existsByReference(String reference);
    List<Payment> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}

