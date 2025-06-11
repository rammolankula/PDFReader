package com.vestigas.controller;

import com.vestigas.model.Invoice;
import com.vestigas.service.PdfInvoiceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/integrations")
public class InvoiceUploadController {

    private final PdfInvoiceService pdfInvoiceService;

    public InvoiceUploadController(PdfInvoiceService pdfInvoiceService) {
        this.pdfInvoiceService = pdfInvoiceService;
    }

    @PostMapping(
        value = "/upload-pdf",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE // 👈 return response as XML
    )
    public ResponseEntity<Invoice> uploadPdfInvoice(@RequestParam("file") MultipartFile file) {
        try {
            Invoice invoice = pdfInvoiceService.extractAndParse(file);
            return ResponseEntity.ok(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
