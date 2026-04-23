package com.airesume.templateservice.dto;

import com.airesume.templateservice.model.Category;
import com.airesume.templateservice.model.Tier;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight Data Transfer Object for Template listings.
 * Excludes heavy fields like HTML and CSS layouts to optimize bandwidth.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponseDTO {
    private Long templateId;
    private String name;
    private String description;
    private String thumbnailUrl;
    private Category category;
    private Tier tier;
    private Long usageCount;
}
