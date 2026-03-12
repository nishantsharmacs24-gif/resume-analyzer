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

    @Value("${file.upload-dir:/tmp/uploads}")
    private String uploadDir;

    public Resume saveResume(MultipartFile file, Long userId) {
        try {
            String originalName = file.getOriginalFilename();
            String fileType = originalName.endsWith(".pdf") ? "PDF" : "DOCX";

            // Extract text directly from memory stream
            byte[] fileBytes = file.getBytes();
            String extractedText = fileType.equals("PDF")
                ? extractTextFromPDFBytes(fileBytes)
                : extractTextFromDOCXBytes(fileBytes);

            // Save file to disk (best effort)
            String uniqueFileName = System.currentTimeMillis() + "_" + originalName;
            String filePath = "/tmp/uploads/" + uniqueFileName;
            try {
                Path uploadPath = Paths.get("/tmp/uploads");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Files.write(Paths.get(filePath), fileBytes);
            } catch (Exception e) {
                filePath = originalName; // fallback
            }

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

            Resume resume = new Resume();
            resume.setUser(user);
            resume.setFileName(uniqueFileName);
            resume.setFilePath(filePath);
            resume.setFileType(fileType);
            resume.setFileSize(file.getSize());
            resume.setExtractedText(extractedText);

            return resumeRepository.save(resume);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    // Extract text from PDF bytes directly in memory
    public String extractTextFromPDFBytes(byte[] bytes) {
        try (PDDocument document = PDDocument.load(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        } catch (IOException e) {
            throw new RuntimeException("PDF text extraction failed: " + e.getMessage());
        }
    }

    // Extract text from DOCX bytes directly in memory
    public String extractTextFromDOCXBytes(byte[] bytes) {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            StringBuilder text = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                text.append(paragraph.getText()).append("\n");
            }
            return text.toString().trim();
        } catch (IOException e) {
            throw new RuntimeException("DOCX text extraction failed: " + e.getMessage());
        }
    }

    public String extractTextFromPDF(String filePath) {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).trim();
        } catch (IOException e) {
            throw new RuntimeException("PDF text extraction failed: " + e.getMessage());
        }
    }

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

    public AnalysisResult analyzeResume(Long resumeId, String jobDescription) {
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new RuntimeException("Resume not found"));

        String resumeText = resume.getExtractedText();

        if (resumeText == null || resumeText.isEmpty()) {
            throw new RuntimeException("No text found in resume. Please re-upload.");
        }

        return mlService.analyzeResume(resume, resumeText, jobDescription);
    }

    public List<Resume> getResumesByUser(Long userId) {
        return resumeRepository.findByUserIdOrderByUploadedAtDesc(userId);
    }

    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }

    public List<AnalysisResult> getAllResultsRanked() {
        return analysisResultRepository.findAllByOrderByMatchScoreDesc();
    }

    public AnalysisResult updateDecision(Long analysisId, String decision) {
        AnalysisResult result = analysisResultRepository.findById(analysisId)
            .orElseThrow(() -> new RuntimeException("Analysis result not found"));
        result.setDecision(AnalysisResult.Decision.valueOf(decision.toUpperCase()));
        return analysisResultRepository.save(result);
    }

    public byte[] downloadResume(Long resumeId) throws IOException {
        Resume resume = resumeRepository.findById(resumeId)
            .orElseThrow(() -> new RuntimeException("Resume not found"));
        Path path = Paths.get(resume.getFilePath());
        return Files.readAllBytes(path);
    }
}
