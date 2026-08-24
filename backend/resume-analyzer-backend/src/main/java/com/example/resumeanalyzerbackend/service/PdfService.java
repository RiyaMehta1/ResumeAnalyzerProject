package com.example.resumeanalyzerbackend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class PdfService {

    public String extractText(MultipartFile file) {
        try {
            File tempFile = File.createTempFile("resume-", ".pdf");
            file.transferTo(tempFile);

            try (PDDocument document = Loader.loadPDF(tempFile)) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setSortByPosition(true);
                String text = stripper.getText(document);
                tempFile.delete();
                return text;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }
}
