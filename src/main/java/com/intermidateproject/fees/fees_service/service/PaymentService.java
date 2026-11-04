package com.intermidateproject.fees.fees_service.service;

import com.intermidateproject.fees.fees_service.dto.ReconciliationReportResponse;
import com.intermidateproject.fees.fees_service.entity.IdempotencyKey;
import com.intermidateproject.fees.fees_service.entity.Invoice;
import com.intermidateproject.fees.fees_service.entity.Payment;
import com.intermidateproject.fees.fees_service.exception.DuplicatePaymentReferenceException;
import com.intermidateproject.fees.fees_service.exception.IdempotencyConflictException;
import com.intermidateproject.fees.fees_service.exception.OverpaymentException;
import com.intermidateproject.fees.fees_service.exception.ResourceNotFoundException;
import com.intermidateproject.fees.fees_service.repository.IdempotencyKeyRepository;
import com.intermidateproject.fees.fees_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceService invoiceService, IdempotencyKeyRepository idempotencyKeyRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceService = invoiceService;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Transactional
    public Payment createPayment(Long invoiceId, BigDecimal amount, String reference, String idempotencyKey, String requestHash) {

        Invoice invoice = invoiceService.getInvoiceByIdForUpdate(invoiceId);

        // Step 1: check if this idempotency key already exists
        Optional<IdempotencyKey> existing = idempotencyKeyRepository.findById(idempotencyKey);

        if (existing.isPresent()) {
            // key was already used before — for now (simple first version),
            // we'll just throw, rather than returning a stored response.
            // We'll upgrade this to "return the stored result" in a later refinement.
            throw new IdempotencyConflictException("This idempotency key has already been used: " + idempotencyKey);
        }



        // Step 3b: compute current balance
        BigDecimal balance = invoiceService.getBalance(invoiceId);

        // Step 3c: overpayment guard
        if (amount.compareTo(balance) > 0) {
            throw new OverpaymentException(
                    "Payment of " + amount + " exceeds remaining balance of " + balance + " for invoice " + invoiceId
            );
        }

        // Step 3d: duplicate reference guard
        if (paymentRepository.existsByReference(reference)) {
            throw new DuplicatePaymentReferenceException(
                    "A payment with reference '" + reference + "' already exists"
            );
        }

        // Step 3e: construct and save the payment
        Payment payment = new Payment(invoice, amount, reference);
        Payment savedPayment = paymentRepository.save(payment);
       balance = balance.subtract(savedPayment.getAmount());

       if (balance.compareTo(BigDecimal.ZERO) == 0) {
           invoice.setStatus("PAID");

       }else{
           invoice.setStatus("PARTIALLY_PAID");
       }

        // Step 3f: save an idempotency key record for this outcome
        IdempotencyKey keyRecord = new IdempotencyKey(idempotencyKey, requestHash);
        idempotencyKeyRepository.save(keyRecord);

        // Step 4: return the saved payment
        return savedPayment;
    }

    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    public ReconciliationReportResponse getReconciliationReport(LocalDateTime start, LocalDateTime end) {
        List<Payment> payments = paymentRepository.findByCreatedAtBetween(start, end);

        BigDecimal total = BigDecimal.ZERO;
        for (Payment payment : payments) {
            if (payment.getStatus().equals("CONFIRMED")) {
                total = total.add(payment.getAmount());
            }
        }

        return new ReconciliationReportResponse(payments, total, start, end);
    }

}
