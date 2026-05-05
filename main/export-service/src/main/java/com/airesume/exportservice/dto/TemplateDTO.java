package com.airesume.exportservice.dto;

import lombok.Data;

@Data
public class TemplateDTO {
    private Long templateId;
    private String name;
    private String htmlLayout;
    private String cssStyles;
}
