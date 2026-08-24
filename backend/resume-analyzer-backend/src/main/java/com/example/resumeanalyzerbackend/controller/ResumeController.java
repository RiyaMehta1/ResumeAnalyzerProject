package com.example.resumeanalyzerbackend.controller;

import com.example.resumeanalyzerbackend.dto.AnalysisResponse;
import com.example.resumeanalyzerbackend.entity.Resume;
import com.example.resumeanalyzerbackend.repository.ResumeRepository;
import com.example.resumeanalyzerbackend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeRepository resumeRepository;

    @PostMapping("/upload")
    public ResponseEntity<Resume> upload(
            @RequestParam("userId") Long userId,
            @RequestParam("file") MultipartFile file
    ) {

        return ResponseEntity.ok(
                resumeService.upload(userId, file)
        );
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyze(
            @RequestParam("resumeId") Long resumeId,
            @RequestParam("jobDescription") String jobDescription,
            @RequestParam("file") MultipartFile file
    ) {

        return ResponseEntity.ok(
                resumeService.analyze(
                        resumeId,
                        jobDescription,
                        file
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resume> getResume(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                resumeRepository.findById(id)
                        .orElseThrow(
                                () -> new RuntimeException("Resume not found")
                        )
        );
    }
}