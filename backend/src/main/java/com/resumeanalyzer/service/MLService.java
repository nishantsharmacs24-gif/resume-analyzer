package com.resumeanalyzer.service;

import com.resumeanalyzer.model.AnalysisResult;
import com.resumeanalyzer.model.Resume;
import com.resumeanalyzer.repository.AnalysisResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

// This service is the BRIDGE between Java and Python
// It sends resume text to Python, gets back ML analysis results

@Service
public class MLService {

    @Value("${ml.service.url}")
    private String mlServiceUrl; // http://localhost:5000

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    // Send resume + job description to Python ML service for analysis
    public AnalysisResult analyzeResume(Resume resume, String resumeText, String jobDescription) {

        RestTemplate restTemplate = new RestTemplate();

        // Build JSON request body to send to Python
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("resume_text", resumeText);
        requestBody.put("job_description", jobDescription);

        // Set headers: we're sending JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> httpEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Call Python Flask API: POST http://localhost:5000/analyze
            ResponseEntity<Map> response = restTemplate.postForEntity(
                mlServiceUrl + "/analyze",
                httpEntity,
                Map.class
            );

            Map<String, Object> data = response.getBody();

            // Map Python response fields to Java AnalysisResult object
            AnalysisResult result = new AnalysisResult();
            result.setResume(resume);
            result.setJobDescription(jobDescription);
            result.setMatchScore(toDouble(data.get("match_score")));
            result.setFeedback((String) data.get("feedback"));
            result.setKeywordsMatched((String) data.get("keywords_matched"));
            result.setKeywordsMissing((String) data.get("keywords_missing"));
            result.setSkillsFound((String) data.get("skills_found"));
            result.setSuggestions((String) data.get("suggestions"));
            result.setSummary((String) data.get("summary"));
            result.setDecision(AnalysisResult.Decision.PENDING);

            // Save to database and return
            return analysisResultRepository.save(result);

        } catch (Exception e) {
            // If Python service is down, return basic result with error message
            AnalysisResult fallback = new AnalysisResult();
            fallback.setResume(resume);
            fallback.setJobDescription(jobDescription);
            fallback.setMatchScore(0.0);
            fallback.setFeedback("ML service is currently unavailable. Please try again later. Error: " + e.getMessage());
            return analysisResultRepository.save(fallback);
        }
    }

    // Helper: safely convert Number to Double
    private Double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
}
