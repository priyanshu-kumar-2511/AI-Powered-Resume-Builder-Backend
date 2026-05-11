package com.airesume.resumeservice.controller;

import com.airesume.resumeservice.dto.AtsUpdateDTO;
import com.airesume.resumeservice.dto.ResumeCreateRequest;
import com.airesume.resumeservice.dto.ResumeResponse;
import com.airesume.resumeservice.dto.ResumeUpdateRequest;
import com.airesume.resumeservice.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller exposing API endpoints for Resume management.
 * Contains endpoints for Free/Premium users and Administrative tasks.
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Resume Controller", description = "Endpoints for creating, updating, and managing user resumes")
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * 1. Creates a new resume from a template.
     */
    @PostMapping
    @Operation(summary = "Create a new resume from a template")
    public ResponseEntity<ResumeResponse> createResume(@Valid @RequestBody ResumeCreateRequest request) {
        return new ResponseEntity<>(resumeService.createResume(request), HttpStatus.CREATED);
    }

    /**
     * 2. Retrieves a specific resume by ID.
     */
    @GetMapping("/{resumeId}")
    @Operation(summary = "Get detailed information of a specific resume")
    public ResponseEntity<ResumeResponse> getResumeById(@PathVariable Long resumeId) {
        return ResponseEntity.ok(resumeService.getResumeById(resumeId));
    }

    /**
     * 3. Retrieves all resumes associated with a specific user.
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all resumes for a specific user")
    public ResponseEntity<List<ResumeResponse>> getResumesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(resumeService.getResumesByUser(userId));
    }

    /**
     * 4. Retrieves resumes based on a template ID (Admin analytics).
     */
    @GetMapping("/template/{templateId}")
    @Operation(summary = "Get resumes created from a specific template")
    public ResponseEntity<List<ResumeResponse>> getResumesByTemplate(@PathVariable Long templateId) {
        return ResponseEntity.ok(resumeService.getResumesByTemplate(templateId));
    }

    /**
     * 5. Retrieves all public gallery resumes.
     */
    @GetMapping("/public")
    @Operation(summary = "Get all resumes published to public gallery")
    public ResponseEntity<List<ResumeResponse>> getPublicResumes() {
        return ResponseEntity.ok(resumeService.getPublicResumes());
    }

    /**
     * 6. Updates editable metadata of a resume.
     */
    @PutMapping("/{resumeId}")
    @Operation(summary = "Update metadata of an existing resume")
    public ResponseEntity<ResumeResponse> updateResume(
            @PathVariable Long resumeId,
            @RequestBody ResumeUpdateRequest request) {
        return ResponseEntity.ok(resumeService.updateResume(resumeId, request));
    }

    /**
     * 7. Updates the ATS score asynchronously.
     * Intended to be called by ai-service.
     */
    @PutMapping("/{resumeId}/ats-score")
    @Operation(summary = "Update ATS score of a resume (Internal)")
    public ResponseEntity<ResumeResponse> updateAtsScore(
            @PathVariable Long resumeId,
            @Valid @RequestBody AtsUpdateDTO request) {
        return ResponseEntity.ok(resumeService.updateAtsScore(resumeId, request));
    }

    /**
     * 8. Duplicates an existing resume to create a new variant.
     */
    @PostMapping("/{resumeId}/duplicate")
    @Operation(summary = "Duplicate an existing resume")
    public ResponseEntity<ResumeResponse> duplicateResume(@PathVariable Long resumeId) {
        return new ResponseEntity<>(resumeService.duplicateResume(resumeId), HttpStatus.CREATED);
    }

    /**
     * 9. Publishes a resume to the public gallery.
     */
    @PutMapping("/{resumeId}/publish")
    @Operation(summary = "Publish a resume to the public gallery")
    public ResponseEntity<ResumeResponse> publishResume(@PathVariable Long resumeId) {
        return ResponseEntity.ok(resumeService.publishResume(resumeId));
    }

    /**
     * 10. Removes a resume from the public gallery.
     */
    @PutMapping("/{resumeId}/unpublish")
    @Operation(summary = "Remove a resume from the public gallery")
    public ResponseEntity<ResumeResponse> unpublishResume(@PathVariable Long resumeId) {
        return ResponseEntity.ok(resumeService.unpublishResume(resumeId));
    }

    /**
     * 11. Increments the public view count of a resume.
     */
    @PutMapping("/{resumeId}/view-count")
    @Operation(summary = "Increment public view count of a resume")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Long resumeId) {
        resumeService.incrementViewCount(resumeId);
        return ResponseEntity.ok().build();
    }

    /**
     * 12. Deletes a resume completely.
     */
    @DeleteMapping("/{resumeId}")
    @Operation(summary = "Delete a specific resume permanently")
    public ResponseEntity<Void> deleteResume(@PathVariable Long resumeId) {
        resumeService.deleteResume(resumeId);
        return ResponseEntity.noContent().build();
    }

    // --- Admin Endpoints ---

    /**
     * 13. Retrieves all resumes globally (Admin panel).
     */
    @GetMapping("/admin/all")
    @Operation(summary = "Get all resumes globally (Admin only)")
    public ResponseEntity<List<ResumeResponse>> getAllResumes() {
        return ResponseEntity.ok(resumeService.getAllResumes());
    }

    /**
     * 14. Admin operation to forcefully delete any resume.
     */
    @DeleteMapping("/admin/{resumeId}")
    @Operation(summary = "Forcefully delete any resume (Admin only)")
    public ResponseEntity<Void> forceDeleteResume(@PathVariable Long resumeId) {
        resumeService.forceDeleteResume(resumeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 15. Admin operation to count total resumes per user.
     */
    @GetMapping("/admin/count/{userId}")
    @Operation(summary = "Get count of resumes for a user (Admin only)")
    public ResponseEntity<Long> countUserResumes(@PathVariable Long userId) {
        return ResponseEntity.ok(resumeService.countUserResumes(userId));
    }
}
