package com.airesume.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for PDF template extraction results.
 * Contains the generated HTML/CSS and a visual thumbnail URI of the template.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateExtractionResponse {
    private String thumbnailUrl;
    private String htmlLayout;
    private String cssStyles;
}
