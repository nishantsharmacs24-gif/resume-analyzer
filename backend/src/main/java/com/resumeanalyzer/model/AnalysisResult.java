package com.resumeanalyzer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Column(columnDefinition = "TEXT", name = "job_description")
    private String jobDescription;

    @Column(name = "match_score")
    private Double matchScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(columnDefinition = "TEXT", name = "keywords_matched")
    private String keywordsMatched;

    @Column(columnDefinition = "TEXT", name = "keywords_missing")
    private String keywordsMissing;

    @Column(columnDefinition = "TEXT", name = "skills_found")
    private String skillsFound;

    @Column(columnDefinition = "TEXT")
    private String suggestions;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    private Decision decision = Decision.PENDING;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt = LocalDateTime.now();

    public enum Decision {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    // ── Getters ──
    public Long getId() { return id; }
    public Resume getResume() { return resume; }
    public String getJobDescription() { return jobDescription; }
    public Double getMatchScore() { return matchScore; }
    public String getFeedback() { return feedback; }
    public String getKeywordsMatched() { return keywordsMatched; }
    public String getKeywordsMissing() { return keywordsMissing; }
    public String getSkillsFound() { return skillsFound; }
    public String getSuggestions() { return suggestions; }
    public String getSummary() { return summary; }
    public Decision getDecision() { return decision; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }

    // ── Setters ──
    public void setId(Long id) { this.id = id; }
    public void setResume(Resume resume) { this.resume = resume; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
    public void setKeywordsMatched(String keywordsMatched) { this.keywordsMatched = keywordsMatched; }
    public void setKeywordsMissing(String keywordsMissing) { this.keywordsMissing = keywordsMissing; }
    public void setSkillsFound(String skillsFound) { this.skillsFound = skillsFound; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setDecision(Decision decision) { this.decision = decision; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
