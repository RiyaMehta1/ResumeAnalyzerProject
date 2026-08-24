package com.example.service;

import com.example.dto.AnalysisResultDTO;
import com.example.entity.Resume;
import com.example.repository.ResumeRepository;
import com.example.util.PDFExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private PDFExtractor pdfExtractor;

    @Autowired
    private GroqService groqService;

    @Value("${app.upload-dir}")
    private String uploadDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Resume uploadResume(Long userId, MultipartFile file) throws Exception {
        // Create upload directory if it doesn't exist
        Files.createDirectories(Paths.get(uploadDir));

        // Generate file name
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String filePath = uploadDir + File.separator + fileName;

        // Save file
        file.transferTo(new File(filePath));

        // Extract text from PDF
        String extractedText = pdfExtractor.extractTextFromPDF(filePath);

        // Save resume to database
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setFileName(file.getOriginalFilename());
        resume.setFilePath(filePath);
        resume.setExtractedText(extractedText);

        return resumeRepository.save(resume);
    }

    public AnalysisResultDTO analyzeResume(Long resumeId, String jobDescription) throws Exception {
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new Exception("Resume not found"));

        // Get analysis from Groq
        String analysisJson = groqService.analyzeResume(resume.getExtractedText(), jobDescription);

        // Parse the response - handle potential markdown code blocks
        String jsonString = extractJsonFromResponse(analysisJson);
        JsonNode analysisNode = objectMapper.readTree(jsonString);

        // Build result DTO
        AnalysisResultDTO result = new AnalysisResultDTO();
        result.setResumeId(resumeId);
        result.setAtsScore(analysisNode.get("atsScore").asInt());
        result.setMatchedSkills(jsonArrayToList(analysisNode.get("matchedSkills")));
        result.setMissingSkills(jsonArrayToList(analysisNode.get("missingSkills")));
        result.setSuggestions(jsonArrayToList(analysisNode.get("suggestions")));
        result.setRawAnalysis(analysisJson);

        return result;
    }

    private String extractJsonFromResponse(String response) {
        // Remove markdown code blocks if present
        if (response.contains("```json")) {
            response = response.replace("```json", "").replace("```", "").trim();
        } else if (response.contains("```")) {
            response = response.replace("```", "").trim();
        }
        return response;
    }

    private List<String> jsonArrayToList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            arrayNode.forEach(node -> list.add(node.asText()));
        }
        return list;
    }
}
