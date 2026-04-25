package com.airesume.sectionservice.service;

import com.airesume.sectionservice.model.Section;
import com.airesume.sectionservice.model.SectionType;

import java.util.List;

/**
 * Service interface for managing Resume Sections.
 * Contains business logic for CRUD operations, visibility toggling, and reordering.
 * 
 * @author ResumeAI Team
 * @version 1.0
 */
public interface SectionService {
    /**
     * Adds a new section to a resume.
     * @param section the section to add
     * @return the saved section
     */
    Section addSection(Section section);

    /**
     * Retrieves all sections for a resume ordered by displayOrder.
     * @param resumeId the ID of the resume
     * @return list of ordered sections
     */
    List<Section> getSectionsByResume(Long resumeId);

    /**
     * Retrieves a single section by its ID.
     * @param sectionId the ID of the section
     * @return the requested section
     */
    Section getSectionById(Long sectionId);

    /**
     * Retrieves all sections of a specific type for a resume.
     * @param resumeId the ID of the resume
     * @param type the type of sections
     * @return list of sections matching the type
     */
    List<Section> getSectionsByType(Long resumeId, SectionType type);

    /**
     * Retrieves all AI-generated sections for a resume.
     * @param resumeId the ID of the resume
     * @return list of AI-generated sections
     */
    List<Section> getAiGeneratedSections(Long resumeId);

    /**
     * Updates an existing section.
     * @param sectionId the ID of the section to update
     * @param sectionDetails the updated details
     * @return the updated section
     */
    Section updateSection(Long sectionId, Section sectionDetails);

    /**
     * Toggles the visibility of a section.
     * @param sectionId the ID of the section
     * @return the updated section
     */
    Section toggleVisibility(Long sectionId);

    /**
     * Reorders sections based on the provided list of IDs.
     * @param resumeId the ID of the resume
     * @param sectionIds the ordered list of section IDs
     */
    void reorderSections(Long resumeId, List<Long> sectionIds);

    /**
     * Performs a bulk update of multiple sections.
     * @param sections the list of sections to update
     * @return the updated sections
     */
    List<Section> bulkUpdate(List<Section> sections);

    /**
     * Deletes a section by its ID.
     * @param sectionId the ID of the section
     */
    void deleteSection(Long sectionId);

    /**
     * Deletes all sections for a specific resume.
     * @param resumeId the ID of the resume
     */
    void deleteAllSectionsByResume(Long resumeId);

    /**
     * Counts the total number of sections for a resume.
     * @param resumeId the ID of the resume
     * @return the total count
     */
    long countSections(Long resumeId);
}
