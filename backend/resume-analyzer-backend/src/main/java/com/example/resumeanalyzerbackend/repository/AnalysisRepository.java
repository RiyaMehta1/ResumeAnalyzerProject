package com.example.resumeanalyzerbackend.repository;

import com.example.resumeanalyzerbackend.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    Optional<Analysis> findByResumeId(Long resumeId);
}
