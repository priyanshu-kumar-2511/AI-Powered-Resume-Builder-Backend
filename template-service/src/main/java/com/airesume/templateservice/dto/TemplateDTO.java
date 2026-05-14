package com.airesume.templateservice.dto;

import com.airesume.templateservice.model.Category;
import com.airesume.templateservice.model.Template;
import com.airesume.templateservice.model.Tier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Full Data Transfer Object for Template.
 * Includes HTML and CSS layouts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDTO {
    private Long templateId;
    private String name;
    private String description;
    private String thumbnailUrl;
    private String htmlLayout;
    private String cssStyles;
    private Category category;
    private Tier tier;
    private Boolean isActive;
    private Long usageCount;

    public static TemplateDTO fromEntity(Template template) {
        if (template == null) return null;
        return TemplateDTO.builder()
                .templateId(template.getTemplateId())
                .name(template.getName())
                .description(template.getDescription())
                .thumbnailUrl(template.getThumbnailUrl())
                .htmlLayout(template.getHtmlLayout())
                .cssStyles(template.getCssStyles())
                .category(template.getCategory())
                .tier(template.getTier())
                .isActive(template.getIsActive())
                .usageCount(template.getUsageCount())
                .build();
    }

    public Template toEntity() {
        return Template.builder()
                .templateId(this.templateId)
                .name(this.name)
                .description(this.description)
                .thumbnailUrl(this.thumbnailUrl)
                .htmlLayout(this.htmlLayout)
                .cssStyles(this.cssStyles)
                .category(this.category)
                .tier(this.tier)
                .isActive(Objects.requireNonNullElse(this.isActive, true))
                .usageCount(Objects.requireNonNullElse(this.usageCount, 0L))
                .build();
    }
}
