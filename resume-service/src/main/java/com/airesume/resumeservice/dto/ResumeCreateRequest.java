package com.airesume.resumeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for creating a new resume.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeCreateRequest {

    /**
     * ID of the user creating the resume.
     */
    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Title of the resume.
     */
    @NotBlank(message = "Title is required")
    private String title;

    /**
     * Selected template ID.
     */
    private Long templateId;
    
    /**
     * Target job title for ATS and tailoring.
     */
    private String targetJobTitle;
    
    /**
     * Language code (e.g., "en", "es").
     */
    private String language;
}
