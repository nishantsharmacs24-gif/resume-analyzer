package com.resumeanalyzer.repository;

import com.resumeanalyzer.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // Get all resumes uploaded by a specific user
    List<Resume> findByUserId(Long userId);

    // Get resumes ordered by upload date (newest first)
    List<Resume> findByUserIdOrderByUploadedAtDesc(Long userId);
}
