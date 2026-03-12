package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.AnalysisResult;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.service.ResumeService;
import com.resumeanalyzer.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired private ResumeService resumeService;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            Resume resume = resumeService.saveResume(file, userId);

            // Return only safe fields — no circular references
            Map<String, Object> response = new HashMap<>();
            response.put("id", resume.getId());
            response.put("fileName", resume.getFileName());
            response.put("fileType", resume.getFileType());
            response.put("fileSize", resume.getFileSize());
            response.put("uploadedAt", resume.getUploadedAt());
            response.put("hasText", resume.getExtractedText() != null && !resume.getExtractedText().isEmpty());
            response.put("textLength", resume.getExtractedText() != null ? resume.getExtractedText().length() : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(
            @RequestBody Map<String, Object> request,
            @RequestHeader("Authorization") String token) {
        try {
            Long resumeId = Long.valueOf(request.get("resumeId").toString());
            String jobDescription = (String) request.get("jobDescription");

            AnalysisResult result = resumeService.analyzeResume(resumeId, jobDescription);

            // Return only safe fields — no circular references
            Map<String, Object> response = new HashMap<>();
            response.put("id", result.getId());
            response.put("matchScore", result.getMatchScore());
            response.put("feedback", result.getFeedback());
            response.put("keywordsMatched", result.getKeywordsMatched());
            response.put("keywordsMissing", result.getKeywordsMissing());
            response.put("skillsFound", result.getSkillsFound());
            response.put("suggestions", result.getSuggestions());
            response.put("summary", result.getSummary());
            response.put("decision", result.getDecision());
            response.put("analyzedAt", result.getAnalyzedAt());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getResumes(@RequestHeader("Authorization") String token) {
        try {
            Long userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            var resumes = resumeService.getResumesByUser(userId);

            var response = resumes.stream().map(r -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", r.getId());
                map.put("fileName", r.getFileName());
                map.put("fileType", r.getFileType());
                map.put("uploadedAt", r.getUploadedAt());
                return map;
            }).toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadResume(@PathVariable Long id) {
        try {
            byte[] data = resumeService.downloadResume(id);
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=resume.pdf")
                .body(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
