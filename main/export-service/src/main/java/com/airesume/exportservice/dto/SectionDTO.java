package com.airesume.exportservice.dto;

import lombok.Data;

@Data
public class SectionDTO {
    private Long sectionId;
    private String sectionType;
    private String title;
    private String content; // JSON string
    private Integer displayOrder;
    private Boolean isVisible;
}
