package com.resumeanalyzer.controller;

import com.resumeanalyzer.model.AnalysisResult;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "http://localhost:3000")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    // ─────────────────────────────────────
    // POST /api/resume/upload
    // Upload a PDF or DOCX resume file
    // ─────────────────────────────────────
    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {

        // Validate file type
        String originalName = file.getOriginalFilename();
        if (originalName == null ||
            (!originalName.endsWith(".pdf") && !originalName.endsWith(".docx"))) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Only PDF and DOCX files are allowed!"));
        }

        try {
            Resume saved = resumeService.saveResume(file, userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Resume uploaded successfully!",
                "resumeId", saved.getId(),
                "fileName", saved.getFileName()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────
    // POST /api/resume/analyze
    // Analyze resume against job description
    // ─────────────────────────────────────
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeResume(@RequestBody Map<String, Object> request) {
        try {
            Long resumeId = Long.valueOf(request.get("resumeId").toString());
            String jobDescription = (String) request.get("jobDescription");

            if (jobDescription == null || jobDescription.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Job description cannot be empty!"));
            }

            AnalysisResult result = resumeService.analyzeResume(resumeId, jobDescription);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Analysis failed: " + e.getMessage()));
        }
    }

    // ─────────────────────────────────────
    // GET /api/resume/user/{userId}
    // Get all resumes uploaded by a user
    // ─────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Resume>> getUserResumes(@PathVariable Long userId) {
        List<Resume> resumes = resumeService.getResumesByUser(userId);
        return ResponseEntity.ok(resumes);
    }

    // ─────────────────────────────────────
    // GET /api/resume/{id}
    // Get a single resume by ID
    // ─────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getResume(@PathVariable Long id) {
        return resumeService.getResumeById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────
    // GET /api/resume/download/{id}
    // Download original resume file
    // ─────────────────────────────────────
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadResume(@PathVariable Long id) throws IOException {
        byte[] fileBytes = resumeService.downloadResume(id);

        return resumeService.getResumeById(id).map(resume -> {
            String contentType = resume.getFileType().equals("PDF")
                ? "application/pdf"
                : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + resume.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(fileBytes);
        }).orElse(ResponseEntity.notFound().build());
    }
}
