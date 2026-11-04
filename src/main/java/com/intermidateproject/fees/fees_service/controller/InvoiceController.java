package com.intermidateproject.fees.fees_service.controller;

import com.intermidateproject.fees.fees_service.dto.CreateInvoiceRequest;
import com.intermidateproject.fees.fees_service.entity.Invoice;
import com.intermidateproject.fees.fees_service.entity.Student;
import com.intermidateproject.fees.fees_service.service.InvoiceService;
import com.intermidateproject.fees.fees_service.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final StudentService studentService;

    public InvoiceController(InvoiceService invoiceService, StudentService studentService) {
        this.invoiceService = invoiceService;
        this.studentService = studentService;
    }

    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody CreateInvoiceRequest request){
      Student savedStudent =   studentService.getStudentById(request.getStudentId());
      Invoice savedInvoice = invoiceService.createInvoice(savedStudent, request.getTotalAmount());
      return ResponseEntity.status(HttpStatus.CREATED).body(savedInvoice);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Invoice> getInvoice(@PathVariable Long id){
        Invoice  invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices(){
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable Long id){
        BigDecimal balance = invoiceService.getBalance(id);
        return ResponseEntity.ok(balance);
    }

}
