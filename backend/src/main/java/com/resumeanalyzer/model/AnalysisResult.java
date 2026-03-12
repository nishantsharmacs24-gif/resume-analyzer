package com.resumeanalyzer.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_results")
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @Column(name = "match_score")
    private Double matchScore;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "keywords_matched", columnDefinition = "TEXT")
    private String keywordsMatched;

    @Column(name = "keywords_missing", columnDefinition = "TEXT")
    private String keywordsMissing;

    @Column(name = "skills_found", columnDefinition = "TEXT")
    private String skillsFound;

    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    private Decision decision;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    public enum Decision {
        PENDING, ACCEPTED, REJECTED
    }

    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }

    public String getJobDescription() { return jobDescription; }
    public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }

    public Double getMatchScore() { return matchScore; }
    public void setMatchScore(Double matchScore) { this.matchScore = matchScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String getKeywordsMatched() { return keywordsMatched; }
    public void setKeywordsMatched(String keywordsMatched) { this.keywordsMatched = keywordsMatched; }

    public String getKeywordsMissing() { return keywordsMissing; }
    public void setKeywordsMissing(String keywordsMissing) { this.keywordsMissing = keywordsMissing; }

    public String getSkillsFound() { return skillsFound; }
    public void setSkillsFound(String skillsFound) { this.skillsFound = skillsFound; }

    public String getSuggestions() { return suggestions; }
    public void setSuggestions(String suggestions) { this.suggestions = suggestions; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public Decision getDecision() { return decision; }
    public void setDecision(Decision decision) { this.decision = decision; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
}
