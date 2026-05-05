package com.airesume.resumeservice.dto;

import com.airesume.resumeservice.model.Resume;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object representing the standard response for Resume operations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumeResponse {
    private Long resumeId;
    private Long userId;
    private String title;
    private String targetJobTitle;
    private Long templateId;
    private Integer atsScore;
    private String status;
    private String language;
    private boolean isPublic;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Constructs a ResumeResponse DTO from a Resume entity.
     *
     * @param resume The source entity.
     */
    public ResumeResponse(Resume resume) {
        this.resumeId = resume.getResumeId();
        this.userId = resume.getUserId();
        this.title = resume.getTitle();
        this.targetJobTitle = resume.getTargetJobTitle();
        this.templateId = resume.getTemplateId();
        this.atsScore = resume.getAtsScore();
        this.status = resume.getStatus();
        this.language = resume.getLanguage();
        this.isPublic = resume.isPublic();
        this.viewCount = resume.getViewCount();
        this.createdAt = resume.getCreatedAt();
        this.updatedAt = resume.getUpdatedAt();
    }
}
