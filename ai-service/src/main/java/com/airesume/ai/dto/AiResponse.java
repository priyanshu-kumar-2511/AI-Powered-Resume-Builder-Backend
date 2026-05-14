package com.airesume.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Data Transfer Object for AI Generation Responses.
 * Returns the generated text, optimized bullet points, or 
 * calculated ATS scores along with relevant metadata.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {
    private String content;
    private List<String> bulletPoints;
    private Integer score;
    private Map<String, Object> metadata;
}
