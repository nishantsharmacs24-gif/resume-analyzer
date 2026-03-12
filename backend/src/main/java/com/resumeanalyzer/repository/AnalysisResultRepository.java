package com.resumeanalyzer.repository;

import com.resumeanalyzer.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    // Get latest analysis for a resume
    Optional<AnalysisResult> findTopByResumeIdOrderByAnalyzedAtDesc(Long resumeId);

    // Get all results ordered by score (for interviewer ranking)
    List<AnalysisResult> findAllByOrderByMatchScoreDesc();

    // Custom query: get top N candidates above a score threshold
    @Query("SELECT a FROM AnalysisResult a WHERE a.matchScore >= :minScore ORDER BY a.matchScore DESC")
    List<AnalysisResult> findTopCandidates(Double minScore);

    // Get results by recruiter decision
    List<AnalysisResult> findByDecision(AnalysisResult.Decision decision);
}
