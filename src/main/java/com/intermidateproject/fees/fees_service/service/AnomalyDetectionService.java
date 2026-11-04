package com.intermidateproject.fees.fees_service.service;

import com.intermidateproject.fees.fees_service.anomaly.Anomaly;
import com.intermidateproject.fees.fees_service.anomaly.AnomalyType;
import com.intermidateproject.fees.fees_service.entity.Invoice;
import com.intermidateproject.fees.fees_service.entity.Payment;
import com.intermidateproject.fees.fees_service.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnomalyDetectionService {

    private static final Duration DUPLICATE_WINDOW = Duration.ofMinutes(5);

    private final InvoiceService invoiceService;
    private final PaymentRepository paymentRepository;

    public AnomalyDetectionService(InvoiceService invoiceService,
                                   PaymentRepository paymentRepository) {
        this.invoiceService = invoiceService;
        this.paymentRepository = paymentRepository;
    }

    public List<Anomaly> detectAnomalies() {
        List<Anomaly> anomalies = new ArrayList<>();
        anomalies.addAll(detectInvoiceAnomalies());
        anomalies.addAll(detectSuspectedDuplicates());
        return anomalies;
    }

    private List<Anomaly> detectInvoiceAnomalies() {
        List<Anomaly> anomalies = new ArrayList<>();

        for (Invoice invoice : invoiceService.getAllInvoices()) {
            BigDecimal total = invoice.getTotalAmount();
            BigDecimal balance = invoiceService.getBalance(invoice.getId());

            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                anomalies.add(new Anomaly(
                        AnomalyType.NEGATIVE_BALANCE,
                        invoice.getId(),
                        "Invoice is overpaid: balance is " + balance + " of " + total));
                continue;
            }

            String expectedStatus = expectedStatus(total, balance);
            if (!expectedStatus.equals(invoice.getStatus())) {
                anomalies.add(new Anomaly(
                        AnomalyType.STATUS_MISMATCH,
                        invoice.getId(),
                        "Invoice status is " + invoice.getStatus()
                                + " but balance is " + balance + " of " + total
                                + " (expected " + expectedStatus + ")"));
            }
        }
        return anomalies;
    }

    private List<Anomaly> detectSuspectedDuplicates() {
        List<Anomaly> anomalies = new ArrayList<>();

        List<Payment> confirmed = new ArrayList<>();
        for (Payment payment : paymentRepository.findAll()) {
            if ("CONFIRMED".equals(payment.getStatus())) {
                confirmed.add(payment);
            }
        }

        for (int i = 0; i < confirmed.size(); i++) {
            for (int j = i + 1; j < confirmed.size(); j++) {
                Payment a = confirmed.get(i);
                Payment b = confirmed.get(j);

                boolean sameInvoice = a.getInvoice().getId().equals(b.getInvoice().getId());
                boolean sameAmount = a.getAmount().compareTo(b.getAmount()) == 0;
                Duration gap = Duration.between(a.getCreatedAt(), b.getCreatedAt()).abs();
                boolean withinWindow = gap.compareTo(DUPLICATE_WINDOW) <= 0;

                if (sameInvoice && sameAmount && withinWindow) {
                    anomalies.add(new Anomaly(
                            AnomalyType.SUSPECTED_DUPLICATE,
                            a.getInvoice().getId(),
                            "Payments " + a.getReference() + " and " + b.getReference()
                                    + " on the same invoice have the same amount "
                                    + a.getAmount() + " within 5 minutes"));
                }
            }
        }
        return anomalies;
    }

    private String expectedStatus(BigDecimal total, BigDecimal balance) {
        if (balance.compareTo(total) == 0) {
            return "UNPAID";
        }
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            return "PAID";
        }
        return "PARTIALLY_PAID";
    }
}