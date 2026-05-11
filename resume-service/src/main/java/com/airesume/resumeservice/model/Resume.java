package com.airesume.resumeservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a Resume in the system.
 * This acts as the root container for a user's resume, holding metadata like
 * the ATS score, title, template choice, and visibility status.
 * Actual sections (like experience, education) are managed by Section-Service.
 */
@Entity
@Table(name = "resumes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    /**
     * Unique identifier for the resume.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resumeId;

    /**
     * The ID of the user who owns this resume.
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * A user-friendly title for the resume (e.g., "Software Engineer - Google").
     */
    @Column(nullable = false)
    private String title;

    /**
     * The target job title this resume is tailored for. Used for ATS scoring.
     */
    private String targetJobTitle;

    /**
     * The ID of the visual template selected for rendering this resume.
     */
    private Long templateId;

    /**
     * The overall Applicant Tracking System (ATS) compatibility score (0-100).
     */
    @Builder.Default
    private Integer atsScore = 0;

    /**
     * Status of the resume: DRAFT or COMPLETE.
     */
    @Column(nullable = false)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT or COMPLETE

    /**
     * Language of the resume, default is English ("en").
     */
    @Builder.Default
    private String language = "en";

    /**
     * Flag indicating if the resume is published to the public gallery.
     */
    @Builder.Default
    private boolean isPublic = false;

    /**
     * The number of times this resume has been viewed.
     */
    @Builder.Default
    private Integer viewCount = 0;

    @Column(columnDefinition = "TEXT")
    private String customizations;

    /**
     * Timestamp of when the resume was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp of when the resume was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Automatically sets creation and update timestamps before inserting into the database.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * Automatically updates the update timestamp before modifying the database record.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
