package com.example.resumeanalyzerbackend.service;

import com.example.resumeanalyzerbackend.dto.AnalysisResponse;
import com.example.resumeanalyzerbackend.entity.Analysis;
import com.example.resumeanalyzerbackend.entity.Resume;
import com.example.resumeanalyzerbackend.repository.AnalysisRepository;
import com.example.resumeanalyzerbackend.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final AnalysisRepository analysisRepository;
    private final PdfService pdfService;
    private final GeminiService geminiService;

    public Resume upload(Long userId, MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null || filename.isBlank()) {
                filename = "resume_" + System.currentTimeMillis() + ".pdf";
            }

            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            Resume resume = Resume.builder()
                    .userId(userId)
                    .filename(filename)
                    .cloudPath(filePath.toString())
                    .uploadedAt(LocalDateTime.now())
                    .build();

            return resumeRepository.save(resume);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload resume", e);
        }
    }

    public AnalysisResponse analyze(Long resumeId, String jobDescription, MultipartFile file) {
        String resumeText = pdfService.extractText(file);

        AnalysisResponse response = geminiService.analyzeResume(resumeText, jobDescription);

        Integer atsScore = response.getAtsScore() != null ? response.getAtsScore() : 0;
        List<String> matchedSkills = response.getMatchedSkills() != null ? response.getMatchedSkills() : java.util.Collections.emptyList();
        List<String> missingSkills = response.getMissingSkills() != null ? response.getMissingSkills() : java.util.Collections.emptyList();
        List<String> suggestions = response.getSuggestions() != null ? response.getSuggestions() : java.util.Collections.emptyList();

        Analysis analysis = Analysis.builder()
                .resumeId(resumeId)
                .atsScore(atsScore)
                .matchedSkills(String.join(", ", matchedSkills))
                .missingSkills(String.join(", ", missingSkills))
                .suggestions(String.join(", ", suggestions))
                .createdAt(LocalDateTime.now())
                .build();

        analysisRepository.save(analysis);

        return AnalysisResponse.builder()
                .atsScore(atsScore)
                .matchedSkills(matchedSkills)
                .missingSkills(missingSkills)
                .suggestions(suggestions)
                .build();
    }
}