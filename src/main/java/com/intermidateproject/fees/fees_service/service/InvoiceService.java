package com.intermidateproject.fees.fees_service.service;

import com.intermidateproject.fees.fees_service.entity.*;
import com.intermidateproject.fees.fees_service.exception.ResourceNotFoundException;
import com.intermidateproject.fees.fees_service.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, PaymentRepository paymentRepository) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
    }

    public Invoice createInvoice(Student student, BigDecimal totalAmount){
              Invoice invoice = new Invoice(student, totalAmount);

              return invoiceRepository.save(invoice);
    }
    public Invoice getInvoiceByIdForUpdate(Long Id) {
        return invoiceRepository.findByIdForUpdate(Id).orElseThrow(()->new ResourceNotFoundException("Invoice not found with id: " +Id));

    }

    public Invoice getInvoiceById(Long id){
        return invoiceRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Invoice not found with ID: " +id));
    }
    public List<Invoice> getAllInvoices(){return invoiceRepository.findAll();}

    public BigDecimal getBalance (Long invoiceId){
        Invoice invoice = getInvoiceById(invoiceId);
        List<Payment> allPayments = paymentRepository.findAll();

        BigDecimal sumOfPayments = BigDecimal.ZERO;
        for(Payment payment: allPayments){
            if(payment.getInvoice().getId().equals(invoiceId) && payment.getStatus().equals("CONFIRMED")){
                sumOfPayments = sumOfPayments.add(payment.getAmount());
            }
        }
        return invoice.getTotalAmount().subtract(sumOfPayments);
    }
}
