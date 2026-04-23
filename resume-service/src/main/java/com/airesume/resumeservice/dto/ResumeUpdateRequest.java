package com.airesume.resumeservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating an existing resume.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeUpdateRequest {
    /**
     * Updated title of the resume.
     */
    private String title;
    
    /**
     * Updated target job title for ATS and tailoring.
     */
    private String targetJobTitle;
    
    /**
     * Updated language code.
     */
    private String language;
    
    /**
     * Updated status (e.g., "DRAFT" or "COMPLETE").
     */
    private String status;
}
