package com.balu.application_tracking_system.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ResumeService {

    // Extract text from uploaded PDF
    public String extractTextFromPdf(MultipartFile file) throws IOException {

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // Calculate match score
    public int calculateMatchScore(String resumeText, String jobSkills) {

        if (jobSkills == null || jobSkills.isBlank()) {
            return 0;
        }

        String[] skills = jobSkills.split(", ");
        int matched = 0;

        for (String skill : skills) {
            if (resumeText.toLowerCase().contains(skill.trim().toLowerCase())) {
                matched++;
            }
        }
        return (matched * 100) / skills.length;
    }
}
