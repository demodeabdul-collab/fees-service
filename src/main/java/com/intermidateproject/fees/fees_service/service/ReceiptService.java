package com.intermidateproject.fees.fees_service.service;

import com.intermidateproject.fees.fees_service.entity.Payment;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;


import java.io.ByteArrayOutputStream;

@Service
public class ReceiptService {
    private final TemplateEngine templateEngine;
    public ReceiptService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
    public byte[] generateReceiptPdf(Payment payment) {
        Context context = new Context();
        context.setVariable("studentName", payment.getInvoice().getStudent().getName());
        context.setVariable("amount", payment.getAmount());
        context.setVariable("reference", payment.getReference());
        context.setVariable("paymentDate", payment.getCreatedAt());

        String htmlContent = templateEngine.process("receipt", context);

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(htmlContent);
        renderer.layout();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }
}
