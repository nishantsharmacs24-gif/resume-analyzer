package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.AnalysisResult;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@RestController
@RequestMapping("/api/interviewer")
@CrossOrigin(origins = "http://localhost:3000")
public class InterviewerController {

    @Autowired
    private ResumeService resumeService;

    // ─────────────────────────────────────────────
    // POST /api/interviewer/bulk-upload
    // Upload multiple resumes + a job description
    // ─────────────────────────────────────────────
    @PostMapping("/bulk-upload")
    public ResponseEntity<?> bulkUpload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("jobDescription") String jobDescription,
            @RequestParam("userId") Long userId) {

        List<Map<String, Object>> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // Upload each file
                Resume resume = resumeService.saveResume(file, userId);

                // Immediately analyze against job description
                AnalysisResult result = resumeService.analyzeResume(
                    resume.getId(), jobDescription
                );

                results.add(Map.of(
                    "fileName", file.getOriginalFilename(),
                    "resumeId", resume.getId(),
                    "matchScore", result.getMatchScore(),
                    "analysisId", result.getId(),
                    "skillsFound", result.getSkillsFound() != null ? result.getSkillsFound() : "",
                    "keywordsMatched", result.getKeywordsMatched() != null ? result.getKeywordsMatched() : ""
                ));

            } catch (Exception e) {
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
            }
        }

        // Sort results by match score (highest first)
        results.sort((a, b) -> {
            Double scoreA = (Double) a.get("matchScore");
            Double scoreB = (Double) b.get("matchScore");
            return Double.compare(scoreB, scoreA);
        });

        return ResponseEntity.ok(Map.of(
            "success", true,
            "totalUploaded", results.size(),
            "errors", errors,
            "rankedCandidates", results
        ));
    }

    // ─────────────────────────────────────────────
    // GET /api/interviewer/dashboard
    // Get all analyzed resumes ranked by score
    // ─────────────────────────────────────────────
    @GetMapping("/dashboard")
    public ResponseEntity<List<AnalysisResult>> getDashboard() {
        List<AnalysisResult> ranked = resumeService.getAllResultsRanked();
        return ResponseEntity.ok(ranked);
    }

    // ─────────────────────────────────────────────
    // PUT /api/interviewer/decision
    // Accept or reject a candidate
    // ─────────────────────────────────────────────
    @PutMapping("/decision")
    public ResponseEntity<?> updateDecision(@RequestBody Map<String, Object> request) {
        try {
            Long analysisId = Long.valueOf(request.get("analysisId").toString());
            String decision = (String) request.get("decision"); // ACCEPTED or REJECTED

            AnalysisResult updated = resumeService.updateDecision(analysisId, decision);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Decision updated to: " + decision,
                "analysisId", analysisId
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/interviewer/top-candidates?minScore=70
    // Filter candidates above a minimum score
    // ─────────────────────────────────────────────
    @GetMapping("/top-candidates")
    public ResponseEntity<?> getTopCandidates(
            @RequestParam(defaultValue = "60") Double minScore) {
        List<AnalysisResult> results = resumeService.getAllResultsRanked()
            .stream()
            .filter(r -> r.getMatchScore() != null && r.getMatchScore() >= minScore)
            .toList();

        return ResponseEntity.ok(results);
    }
}
