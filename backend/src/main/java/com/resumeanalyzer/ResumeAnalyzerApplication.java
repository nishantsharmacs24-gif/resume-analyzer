package com.resumeanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
// This tells Spring Boot to start and scan all files in this package
@SpringBootApplication
public class ResumeAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResumeAnalyzerApplication.class, args);
        System.out.println("✅ Resume Analyzer Backend is running on http://localhost:8080");
    }
}
