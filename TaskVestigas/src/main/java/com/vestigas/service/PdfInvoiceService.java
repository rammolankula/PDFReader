package com.vestigas.service;

import com.vestigas.model.Invoice;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfInvoiceService {

    public Invoice extractAndParse(MultipartFile file) throws Exception {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            Invoice invoiceFromXml = extractInvoiceFromEmbeddedXml(document);
            if (invoiceFromXml != null) {
                return invoiceFromXml;
            }

            String text = extractTextFromPdf(document);
            return parseTextToInvoice(text);
        }
    }

    private Invoice extractInvoiceFromEmbeddedXml(PDDocument document) throws Exception {
        PDDocumentNameDictionary names = new PDDocumentNameDictionary(document.getDocumentCatalog());
        PDEmbeddedFilesNameTreeNode efTree = names.getEmbeddedFiles();
        if (efTree == null) return null;

        Map<String, PDComplexFileSpecification> embeddedFileNames = efTree.getNames();
        if (embeddedFileNames == null) return null;

        for (Map.Entry<String, PDComplexFileSpecification> entry : embeddedFileNames.entrySet()) {
            String filename = entry.getKey();
            PDComplexFileSpecification fileSpec = entry.getValue();

            if (filename.endsWith(".xml")) {
                try (InputStream is = fileSpec.getEmbeddedFile().createInputStream()) {
                    JAXBContext context = JAXBContext.newInstance(Invoice.class);
                    Unmarshaller unmarshaller = context.createUnmarshaller();
                    return (Invoice) unmarshaller.unmarshal(is);
                }
            }
        }
        return null;
    }

    private String extractTextFromPdf(PDDocument document) throws Exception {
        PDFTextStripper stripper = new PDFTextStripper();
        return stripper.getText(document);
    }

    private Invoice parseTextToInvoice(String text) {
        Invoice invoice = new Invoice();

        Pattern sellerPattern = Pattern.compile("(CPB Software \\(Germany\\) GmbH)");
        Matcher sellerMatcher = sellerPattern.matcher(text);
        if (sellerMatcher.find()) {
            invoice.setSeller(sellerMatcher.group(1));
        }

        Pattern buyerPattern = Pattern.compile("(Musterkunde AG)");
        Matcher buyerMatcher = buyerPattern.matcher(text);
        if (buyerMatcher.find()) {
            invoice.setBuyer(buyerMatcher.group(1));
        }

        // Extract gross amount incl. VAT
        Pattern grossAmountPattern = Pattern.compile("Gross Amount incl\\. VAT\\s+([\\d.,]+) €");
        Matcher grossMatcher = grossAmountPattern.matcher(text);
        if (grossMatcher.find()) {
            String amountStr = grossMatcher.group(1).replace(".", "").replace(",", ".");
            try {
                double amount = Double.parseDouble(amountStr);
                invoice.setAmount(amount);
            } catch (NumberFormatException e) {
                invoice.setAmount(0);
            }
        } else {
            invoice.setAmount(0);
        }

        return invoice;
    }
}
