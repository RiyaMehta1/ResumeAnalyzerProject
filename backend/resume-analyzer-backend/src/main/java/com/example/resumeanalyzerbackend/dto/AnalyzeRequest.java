package com.example.resumeanalyzerbackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AnalyzeRequest {
    @NotBlank
    private String jobDescription;
}