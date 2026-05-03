package com.airesume.exportservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class ResumeResponseDTO {
    private Long resumeId;
    private Long userId;
    private String title;
    private String targetJobTitle;
    private Long templateId;
    private Integer atsScore;
    private String language;
    private List<SectionDTO> sections;
}
