package com.airesume.sectionservice.dto;

import com.airesume.sectionservice.model.Section;
import com.airesume.sectionservice.model.SectionType;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Resume Section.
 * Used to decouple the API from the internal JPA entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionDTO {

    private Long sectionId;
    private Long resumeId;
    private SectionType sectionType;
    private String title;
    private String content;
    private Integer displayOrder;
    private Boolean isVisible;
    private Boolean aiGenerated;

    /**
     * Converts a Section entity to a SectionDTO.
     */
    public static SectionDTO fromEntity(Section section) {
        if (section == null) return null;
        return SectionDTO.builder()
                .sectionId(section.getSectionId())
                .resumeId(section.getResumeId())
                .sectionType(section.getSectionType())
                .title(section.getTitle())
                .content(section.getContent())
                .displayOrder(section.getDisplayOrder())
                .isVisible(section.getIsVisible())
                .aiGenerated(section.getAiGenerated())
                .build();
    }

    /**
     * Converts this DTO to a Section entity.
     */
    public Section toEntity() {
        return Section.builder()
                .sectionId(this.sectionId)
                .resumeId(this.resumeId)
                .sectionType(this.sectionType)
                .title(this.title)
                .content(this.content)
                .displayOrder(this.displayOrder)
                .isVisible(Objects.requireNonNullElse(this.isVisible, true))
                .aiGenerated(Objects.requireNonNullElse(this.aiGenerated, false))
                .build();
    }
}
