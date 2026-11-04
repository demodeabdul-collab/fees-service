package com.intermidateproject.fees.fees_service.controller;

import com.intermidateproject.fees.fees_service.dto.CreatePaymentRequest;
import com.intermidateproject.fees.fees_service.dto.ReconciliationReportResponse;
import com.intermidateproject.fees.fees_service.entity.Payment;
import com.intermidateproject.fees.fees_service.service.PaymentService;
import com.intermidateproject.fees.fees_service.service.ReceiptService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    public PaymentController(PaymentService paymentService, ReceiptService receiptService) {
        this.paymentService = paymentService;
        this.receiptService = receiptService;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody CreatePaymentRequest request) {
        Payment savedPayment = paymentService.createPayment(request.getInvoiceId(),request.getAmount(),request.getReference(),request.getIdempotencyKey(), request.getRequestHash());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPayment);
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id){
        Payment payment = paymentService.getPaymentById(id);
        byte[] pdfBytes = receiptService.generateReceiptPdf(payment);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt-" + id + ".pdf")
                .body(pdfBytes);
    }

    @GetMapping("/reconciliation")
    public ResponseEntity<ReconciliationReportResponse> getReconciliationReport(
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        ReconciliationReportResponse report = paymentService.getReconciliationReport(start, end);
        return ResponseEntity.ok(report);
    }

}
