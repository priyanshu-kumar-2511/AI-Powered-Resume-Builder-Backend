package com.airesume.resumeservice.repository;

import com.airesume.resumeservice.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Resume entity.
 * Provides standard CRUD operations and custom query methods for accessing resume data.
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    /**
     * Retrieves all resumes belonging to a specific user.
     *
     * @param userId The ID of the user.
     * @return List of resumes owned by the user.
     */
    List<Resume> findByUserId(Long userId);

    /**
     * Retrieves all resumes that use a specific template.
     * Used primarily for admin analytics to determine template popularity.
     *
     * @param templateId The ID of the template.
     * @return List of resumes using the specified template.
     */
    List<Resume> findByTemplateId(Long templateId);

    /**
     * Retrieves all resumes based on their public visibility status.
     *
     * @param isPublic True to fetch published resumes, false otherwise.
     * @return List of public (or private) resumes.
     */
    List<Resume> findByIsPublic(boolean isPublic);

    /**
     * Counts the total number of resumes created by a specific user.
     * Used for enforcing tier limits and quotas.
     *
     * @param userId The ID of the user.
     * @return Total resume count for the user.
     */
    Long countByUserId(Long userId);
    
    /**
     * Finds a specific resume ensuring it belongs to the given user.
     * Useful for authorization checks.
     *
     * @param resumeId The ID of the resume.
     * @param userId The ID of the user.
     * @return An Optional containing the resume if found and owned by the user.
     */
    Optional<Resume> findByResumeIdAndUserId(Long resumeId, Long userId);
}
