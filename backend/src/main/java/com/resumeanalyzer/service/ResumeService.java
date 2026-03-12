package com.resumeanalyzer.service;

import com.resumeanalyzer.model.AnalysisResult;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.model.User;
import com.resumeanalyzer.repository.AnalysisResultRepository;
import com.resumeanalyzer.repository.ResumeRepository;
import com.resumeanalyzer.repository.UserRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
public class ResumeService {

    @Autowired private ResumeRepository resumeRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AnalysisResultRepository analysisResultRepository;
    @Autowired private MLService mlService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // ─────────────────────────────────────────────────
    // UPLOAD AND SAVE RESUME
    // ─────────────────────────────────────────────────
    public Resume saveResume(MultipartFile file, Long userId) {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Create unique filename to avoid conflicts
            String originalName = file.getOriginalFilename();
            String uniqueFileName = System.currentTimeMillis() + "_" + originalName;
            Path filePath = uploadPath.resolve(uniqueFileName);

            // Save file bytes to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Determine file type
            String fileType = originalName.endsWith(".pdf") ? "PDF" : "DOCX";

            // Extract text from file
            String extractedText = fileType.equals("PDF")
                ? extractTextFromPDF(filePath.toString())
                : extractTextFromDOCX(filePath.toString());

            // Get the User from DB
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

            // Create Resume entity and save to DB
            Resume resume = new Resume();
            resume.setUser(user);
            resume.setFileName(uniqueFileName);
            resume.setFilePath(filePath.toString());
            resume.setFileType(fileType);
            resume.setFileSize(file.getSize());
            resume.setExtractedText(extractedText);

            return resumeRepository.save(resume);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    // EXTRACT TEXT FROM PDF FILE
    // ─────────────────────────────────────────────────
    public String extractTextFromPDF(String filePath) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return text.trim();
        } catch (IOException e) {
            throw new RuntimeException("PDF text extraction failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    // EXTRACT TEXT FROM DOCX FILE
    // ─────────────────────────────────────────────────
    public String extractTextFromDOCX(String filePath) {
        try (XWPFDocument document = new XWPFDocument(new FileInputStream(filePath))) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            return text.toString().trim();
        } catch (IOException e) {
            throw new RuntimeException("DOCX text extraction failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────
    // ANALYZE RESUME AGAINST JOB DESCRIPTION
    // ─────────────────────────────────────────────────
    public AnalysisResult analyzeResume(Long resumeId, String jobDescription) {
        // Find resume in DB
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new RuntimeException("Resume not found with id: " + resumeId));

        String resumeText = resume.getExtractedText();

        if (resumeText == null || resumeText.isEmpty()) {
            throw new RuntimeException("No text found in resume. Please re-upload.");
        }

        // Send to Python ML service
        return mlService.analyzeResume(resume, resumeText, jobDescription);
    }

    // ─────────────────────────────────────────────────
    // GET ALL RESUMES FOR A USER
    // ─────────────────────────────────────────────────
    public List<Resume> getResumesByUser(Long userId) {
        return resumeRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    // ─────────────────────────────────────────────────
    // GET RESUME BY ID
    // ─────────────────────────────────────────────────
    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }

    // ─────────────────────────────────────────────────
    // GET ALL ANALYSIS RESULTS (for Interviewer)
    // ─────────────────────────────────────────────────
    public List<AnalysisResult> getAllResultsRanked() {
        return analysisResultRepository.findAllByOrderByMatchScoreDesc();
    }

    // ─────────────────────────────────────────────────
    // UPDATE RECRUITER DECISION
    // ─────────────────────────────────────────────────
    public AnalysisResult updateDecision(Long analysisId, String decision) {
        AnalysisResult result = analysisResultRepository.findById(analysisId)
            .orElseThrow(() -> new RuntimeException("Analysis result not found"));

        result.setDecision(AnalysisResult.Decision.valueOf(decision.toUpperCase()));
        return analysisResultRepository.save(result);
    }

    // ─────────────────────────────────────────────────
    // DOWNLOAD RESUME FILE
    // ─────────────────────────────────────────────────
    public byte[] downloadResume(Long resumeId) throws IOException {
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new RuntimeException("Resume not found"));

        Path path = Paths.get(resume.getFilePath());
        return Files.readAllBytes(path);
    }
}
