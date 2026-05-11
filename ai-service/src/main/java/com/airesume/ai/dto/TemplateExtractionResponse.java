package com.airesume.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateExtractionResponse {
    private String thumbnailUrl;
    private String htmlLayout;
    private String cssStyles;
}
