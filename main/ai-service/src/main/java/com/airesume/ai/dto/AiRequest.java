package com.airesume.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AiResponse {
    private String content;
    private List<String> bulletPoints;
    private Integer score;
    private Map<String, Object> metadata;
}
