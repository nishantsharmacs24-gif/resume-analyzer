package com.resumeanalyzer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "candidate_name")
    private String candidateName;

    @Column(name = "candidate_email")
    private String candidateEmail;

    @Column(name = "candidate_phone")
    private String candidatePhone;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL)
    private List<AnalysisResult> analysisResults;

    // ── Getters ──
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public String getFileType() { return fileType; }
    public Long getFileSize() { return fileSize; }
    public String getExtractedText() { return extractedText; }
    public String getCandidateName() { return candidateName; }
    public String getCandidateEmail() { return candidateEmail; }
    public String getCandidatePhone() { return candidatePhone; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public List<AnalysisResult> getAnalysisResults() { return analysisResults; }

    // ── Setters ──
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
    public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }
    public void setCandidatePhone(String candidatePhone) { this.candidatePhone = candidatePhone; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
    public void setAnalysisResults(List<AnalysisResult> analysisResults) { this.analysisResults = analysisResults; }
}
