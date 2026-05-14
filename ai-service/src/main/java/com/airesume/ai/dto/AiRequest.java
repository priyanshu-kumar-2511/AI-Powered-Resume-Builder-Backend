package com.airesume.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for AI Generation Requests.
 * Encapsulates the context needed for generating resume content, 
 * optimizing bullets, or translating sections.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {
    private String userId;
    private Long resumeId;
    private String targetJobTitle;
    private String jobDescription;
    private String existingContent;
    private List<String> existingBullets;
    private String language;
    private String targetLanguage;
    private String sectionType;
    private String tone; // e.g., "Professional", "Confident", "Academic"
}
