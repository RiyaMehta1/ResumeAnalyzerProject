package com.example.controller;

import com.example.dto.AnalysisResultDTO;
import com.example.entity.Resume;
import com.example.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
@CrossOrigin(origins = "*")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<Resume> uploadResume(
        @RequestParam("userId") Long userId,
        @RequestParam("file") MultipartFile file) {
        try {
            Resume resume = resumeService.uploadResume(userId, file);
            return ResponseEntity.ok(resume);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResultDTO> analyzeResume(
        @RequestParam("resumeId") Long resumeId,
        @RequestParam("jobDescription") String jobDescription,
        @RequestParam("file") MultipartFile file) {
        try {
            AnalysisResultDTO result = resumeService.analyzeResume(resumeId, jobDescription);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
