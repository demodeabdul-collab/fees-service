package com.intermidateproject.fees.fees_service.service;

import com.intermidateproject.fees.fees_service.entity.*;
import com.intermidateproject.fees.fees_service.exception.*;
import com.intermidateproject.fees.fees_service.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceService invoiceService;

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPayment_throwsOverpaymentException_whenAmountExceedsBalance() {
        // Arrange
        Student student = new Student("Jane Doe", "jane@example.com");
        Invoice invoice = new Invoice(student, new BigDecimal("500.00"));

        when(invoiceService.getInvoiceByIdForUpdate(1L)).thenReturn(invoice);
        when(invoiceService.getBalance(1L)).thenReturn(new BigDecimal("300.00"));
        when(idempotencyKeyRepository.findById(anyString())).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(OverpaymentException.class, () -> {
            paymentService.createPayment(1L, new BigDecimal("1000.00"), "REF999", "key-test-1", "hash-test-1");
        });
    }

    @Test
    void createPayment_throwsIdempotencyConflictException_whenKeyAlreadyUsed() {
        // Arrange
        Student student = new Student("Jane Doe", "jane@example.com");
        Invoice invoice = new Invoice(student, new BigDecimal("500.00"));
        IdempotencyKey existingKey = new IdempotencyKey("key-test-1", "hash-test-1");

        when(invoiceService.getInvoiceByIdForUpdate(1L)).thenReturn(invoice);
        when(idempotencyKeyRepository.findById("key-test-1")).thenReturn(Optional.of(existingKey));

        // Act + Assert
        assertThrows(IdempotencyConflictException.class, () -> {
            paymentService.createPayment(1L, new BigDecimal("100.00"), "REF888", "key-test-1", "hash-test-1");
        });
    }

    @Test
    void createPayment_throwsDuplicatePaymentReference_whenPaymentIsDuplicate() {
             Student student = new Student("Jane Doe", "jane@example.com");
             Invoice invoice = new Invoice(student, new BigDecimal("500.00"));

             when(invoiceService.getInvoiceByIdForUpdate(1L)).thenReturn(invoice);
             when(invoiceService.getBalance(1L)).thenReturn(new BigDecimal("300.00"));
             when(idempotencyKeyRepository.findById("key-test-1")).thenReturn(Optional.empty());
             when(paymentRepository.existsByReference("REF888")).thenReturn(true);

             assertThrows(DuplicatePaymentReferenceException.class, () -> {
                 paymentService.createPayment(1L, new BigDecimal("200"), "REF888", "key-test-1", "hash-test-1");
             });


    }
}