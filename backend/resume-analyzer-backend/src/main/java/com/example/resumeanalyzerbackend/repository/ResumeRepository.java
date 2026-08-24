package com.example.resumeanalyzerbackend.repository;

import com.example.resumeanalyzerbackend.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
}
