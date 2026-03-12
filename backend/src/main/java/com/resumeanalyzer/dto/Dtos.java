package com.resumeanalyzer.dto;

// DTO = Data Transfer Object
// These are simple classes used to send/receive data in API requests
// They are NOT database entities

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

// ──────────────────────────────────────────
// AUTH DTOs
// ──────────────────────────────────────────

class RegisterRequest {
    public String name;
    public String email;
    public String password;
    public String role; // "CANDIDATE" or "INTERVIEWER"
    public String phone;
}

class LoginRequest {
    public String email;
    public String password;
}

class AuthResponse {
    public String token;
    public String role;
    public Long userId;
    public String name;
    public String message;

    public AuthResponse(String token, String role, Long userId, String name) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.name = name;
        this.message = "Login successful";
    }
}

// ──────────────────────────────────────────
// RESUME DTOs
// ──────────────────────────────────────────

class AnalysisRequest {
    public Long resumeId;
    public String jobDescription;
}

class AnalysisResponse {
    public Long id;
    public Double matchScore;
    public String feedback;
    public String keywordsMatched;
    public String keywordsMissing;
    public String skillsFound;
    public String suggestions;
    public String summary;
}

// ──────────────────────────────────────────
// INTERVIEWER DTOs
// ──────────────────────────────────────────

class DecisionRequest {
    public Long analysisId;
    public String decision; // "ACCEPTED" or "REJECTED"
}
