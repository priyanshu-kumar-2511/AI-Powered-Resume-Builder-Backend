package com.airesume.resumeservice.service;

import com.airesume.resumeservice.dto.AtsUpdateDTO;
import com.airesume.resumeservice.dto.ResumeCreateRequest;
import com.airesume.resumeservice.dto.ResumeResponse;
import com.airesume.resumeservice.dto.ResumeUpdateRequest;

import java.util.List;

/**
 * Service interface for Resume management operations.
 * Defines the contract for all resume lifecycle operations including
 * CRUD, publishing, duplication, and internal metrics updates.
 */
public interface ResumeService {
    
    /**
     * Creates a new resume for a user based on a template.
     * Enforces any tier-based creation limits if applicable.
     *
     * @param request The resume creation payload.
     * @return The created resume details.
     */
    ResumeResponse createResume(ResumeCreateRequest request);

    /**
     * Retrieves a specific resume by its unique identifier.
     *
     * @param resumeId The ID of the resume.
     * @return The resume details.
     */
    ResumeResponse getResumeById(Long resumeId);

    /**
     * Retrieves all resumes owned by a specific user.
     *
     * @param userId The user's ID.
     * @return A list of resumes belonging to the user.
     */
    List<ResumeResponse> getResumesByUser(Long userId);

    /**
     * Retrieves all resumes that utilize a specific template.
     * Typically used for admin analytics.
     *
     * @param templateId The template ID.
     * @return A list of resumes using the template.
     */
    List<ResumeResponse> getResumesByTemplate(Long templateId);

    /**
     * Retrieves all resumes that have been published to the public gallery.
     *
     * @return A list of public resumes.
     */
    List<ResumeResponse> getPublicResumes();

    /**
     * Updates editable fields of a specific resume (e.g., title, target job).
     *
     * @param resumeId The ID of the resume to update.
     * @param request  The updated data.
     * @return The updated resume details.
     */
    ResumeResponse updateResume(Long resumeId, ResumeUpdateRequest request);

    /**
     * Updates the ATS score of a resume.
     * This is intended to be called internally by the ai-service.
     *
     * @param resumeId The ID of the resume.
     * @param request  The new ATS score payload.
     * @return The updated resume details.
     */
    ResumeResponse updateAtsScore(Long resumeId, AtsUpdateDTO request);

    /**
     * Creates a complete copy of an existing resume to serve as a starting point
     * for a new variation.
     *
     * @param resumeId The ID of the original resume.
     * @return The newly duplicated resume details.
     */
    ResumeResponse duplicateResume(Long resumeId);

    /**
     * Publishes a resume to the public gallery, making it visible to guests.
     * Sets the status to COMPLETE.
     *
     * @param resumeId The ID of the resume to publish.
     * @return The published resume details.
     */
    ResumeResponse publishResume(Long resumeId);

    /**
     * Removes a resume from the public gallery, making it private.
     *
     * @param resumeId The ID of the resume to unpublish.
     * @return The updated resume details.
     */
    ResumeResponse unpublishResume(Long resumeId);

    /**
     * Increments the view count of a resume by 1.
     * Typically used when a public resume is viewed by a guest.
     *
     * @param resumeId The ID of the resume.
     */
    void incrementViewCount(Long resumeId);

    /**
     * Permanently deletes a resume.
     *
     * @param resumeId The ID of the resume to delete.
     */
    void deleteResume(Long resumeId);

    /**
     * Retrieves all resumes across the platform.
     * Intended for administrative access only.
     *
     * @return A comprehensive list of all resumes.
     */
    List<ResumeResponse> getAllResumes();

    /**
     * Administratively forces the deletion of any resume.
     *
     * @param resumeId The ID of the resume to delete.
     */
    void forceDeleteResume(Long resumeId);

    /**
     * Counts the total number of resumes created by a specific user.
     *
     * @param userId The ID of the user.
     * @return The total count of resumes.
     */
    Long countUserResumes(Long userId);
}
