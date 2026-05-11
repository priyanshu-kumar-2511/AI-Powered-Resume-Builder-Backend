package com.airesume.sectionservice.controller;

import com.airesume.sectionservice.model.Section;
import com.airesume.sectionservice.model.SectionType;
import com.airesume.sectionservice.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Resume Sections.
 * Provides endpoints for creating, retrieving, updating, deleting, and reordering sections.
 * 
 * @author ResumeAI Team
 * @version 1.0
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Section Controller", description = "Endpoints for managing resume sections and content details")
public class SectionController {

    private final SectionService sectionService;

    /**
     * Creates a new section for a resume.
     * 
     * @param section the section data to create
     * @return the created section with generated ID
     */
    @PostMapping
    @Operation(summary = "Create a new resume section")
    public ResponseEntity<Section> addSection(@RequestBody Section section) {
        return new ResponseEntity<>(sectionService.addSection(section), HttpStatus.CREATED);
    }

    /**
     * Retrieves all sections belonging to a specific resume, ordered by displayOrder.
     * 
     * @param resumeId the ID of the resume
     * @return a list of ordered sections
     */
    @GetMapping("/resume/{resumeId}")
    @Operation(summary = "Get all sections for a resume")
    public ResponseEntity<List<Section>> getSectionsByResume(@PathVariable Long resumeId) {
        return ResponseEntity.ok(sectionService.getSectionsByResume(resumeId));
    }

    /**
     * Retrieves a specific section by its ID.
     * 
     * @param sectionId the ID of the section
     * @return the requested section
     */
    @GetMapping("/{sectionId}")
    @Operation(summary = "Get a single section by ID")
    public ResponseEntity<Section> getSectionById(@PathVariable Long sectionId) {
        return ResponseEntity.ok(sectionService.getSectionById(sectionId));
    }

    /**
     * Retrieves all sections of a specific type (e.g., EXPERIENCE, EDUCATION) for a given resume.
     * 
     * @param resumeId the ID of the resume
     * @param type the type of sections to fetch
     * @return a list of sections matching the type
     */
    @GetMapping("/resume/{resumeId}/type/{type}")
    @Operation(summary = "Get resume sections filtered by type")
    public ResponseEntity<List<Section>> getSectionsByType(@PathVariable Long resumeId, @PathVariable SectionType type) {
        return ResponseEntity.ok(sectionService.getSectionsByType(resumeId, type));
    }

    /**
     * Retrieves all sections that were generated or optimized by AI for a given resume.
     * 
     * @param resumeId the ID of the resume
     * @return a list of AI-generated sections
     */
    @GetMapping("/resume/{resumeId}/ai-generated")
    @Operation(summary = "Get AI-generated/optimized sections for a resume")
    public ResponseEntity<List<Section>> getAiGeneratedSections(@PathVariable Long resumeId) {
        return ResponseEntity.ok(sectionService.getAiGeneratedSections(resumeId));
    }

    /**
     * Updates an existing section's content, title, or metadata.
     * 
     * @param sectionId the ID of the section to update
     * @param section the updated section data
     * @return the updated section
     */
    @PutMapping("/{sectionId}")
    @Operation(summary = "Update an existing section")
    public ResponseEntity<Section> updateSection(@PathVariable Long sectionId, @RequestBody Section section) {
        return ResponseEntity.ok(sectionService.updateSection(sectionId, section));
    }

    /**
     * Toggles the visibility status of a specific section (e.g., to hide it from the final PDF).
     * 
     * @param sectionId the ID of the section to toggle
     * @return the updated section with new visibility status
     */
    @PutMapping("/{sectionId}/toggle-visibility")
    @Operation(summary = "Toggle visibility of a section")
    public ResponseEntity<Section> toggleVisibility(@PathVariable Long sectionId) {
        return ResponseEntity.ok(sectionService.toggleVisibility(sectionId));
    }

    /**
     * Reorders multiple sections based on an ordered list of section IDs.
     * This is used by the frontend drag-and-drop builder.
     * 
     * @param resumeId the ID of the resume
     * @param sectionIds the ordered list of section IDs
     * @return no content on success
     */
    @PutMapping("/resume/{resumeId}/reorder")
    @Operation(summary = "Reorder list of sections for a resume")
    public ResponseEntity<Void> reorderSections(@PathVariable Long resumeId, @RequestBody List<Long> sectionIds) {
        sectionService.reorderSections(resumeId, sectionIds);
        return ResponseEntity.noContent().build();
    }

    /**
     * Performs a bulk update on multiple sections at once.
     * 
     * @param sections the list of sections with updated data
     * @return the list of updated sections
     */
    @PutMapping("/bulk-update")
    @Operation(summary = "Bulk update multiple sections")
    public ResponseEntity<List<Section>> bulkUpdate(@RequestBody List<Section> sections) {
        return ResponseEntity.ok(sectionService.bulkUpdate(sections));
    }

    /**
     * Deletes a specific section.
     * 
     * @param sectionId the ID of the section to delete
     * @return no content on success
     */
    @DeleteMapping("/{sectionId}")
    @Operation(summary = "Delete a specific section")
    public ResponseEntity<Void> deleteSection(@PathVariable Long sectionId) {
        sectionService.deleteSection(sectionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes all sections belonging to a specific resume.
     * Usually called internally when a resume is deleted.
     * 
     * @param resumeId the ID of the resume
     * @return no content on success
     */
    @DeleteMapping("/resume/{resumeId}/all")
    @Operation(summary = "Delete all sections for a resume")
    public ResponseEntity<Void> deleteAllSectionsByResume(@PathVariable Long resumeId) {
        sectionService.deleteAllSectionsByResume(resumeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Counts the total number of sections in a given resume.
     * 
     * @param resumeId the ID of the resume
     * @return the count of sections
     */
    @GetMapping("/resume/{resumeId}/count")
    @Operation(summary = "Count total sections in a resume")
    public ResponseEntity<Long> countSections(@PathVariable Long resumeId) {
        return ResponseEntity.ok(sectionService.countSections(resumeId));
    }
}
