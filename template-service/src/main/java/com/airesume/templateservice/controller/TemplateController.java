package com.airesume.templateservice.controller;

import java.util.Objects;
import com.airesume.templateservice.dto.TemplateDTO;
import com.airesume.templateservice.dto.TemplateResponseDTO;
import com.airesume.templateservice.model.Category;
import com.airesume.templateservice.model.Template;
import com.airesume.templateservice.model.Tier;
import com.airesume.templateservice.service.TemplateService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Template Management.
 * Exposes endpoints for creating, retrieving, and updating resume templates.
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Template Controller", description = "Endpoints for managing and retrieving resume templates")
public class TemplateController {

    private final TemplateService templateService;

    /**
     * Creates a new resume template. 
     * Access is restricted to users with the ADMIN role.
     * 
     * @param templateDto the template details including HTML/CSS source
     * @return the created template metadata and content
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new template (Admin only)")
    public ResponseEntity<TemplateDTO> createTemplate(@RequestBody TemplateDTO templateDto) {
        Template template = templateDto.toEntity();
        return new ResponseEntity<>(TemplateDTO.fromEntity(templateService.createTemplate(template)), HttpStatus.CREATED);
    }

    /**
     * Retrieves all active templates available for public use.
     * Lightweight DTOs are returned without full HTML/CSS content.
     * 
     * @return a list of active template summaries
     */
    @GetMapping
    @Operation(summary = "Get all active templates (Public)")
    public ResponseEntity<List<TemplateResponseDTO>> getAllActiveTemplates() {
        List<TemplateResponseDTO> templates = templateService.getAllActiveTemplates()
                .stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(templates);
    }

    /**
     * GET /api/v1/templates/admin
     * Returns a list of all templates including inactive ones. Restricted to ADMIN.
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all templates including inactive (Admin only)")
    public ResponseEntity<List<TemplateDTO>> getAllTemplatesForAdmin() {
        List<TemplateDTO> templates = templateService.getAllTemplates()
                .stream()
                .map(TemplateDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(templates);
    }

    /**
     * GET /api/v1/templates/free
     * Returns only free-tier templates (Public).
     */
    @GetMapping("/free")
    @Operation(summary = "Get free-tier templates only (Public)")
    public ResponseEntity<List<TemplateResponseDTO>> getFreeTemplates() {
        List<TemplateResponseDTO> templates = templateService.getTemplatesByTier(Tier.FREE)
                .stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(templates);
    }

    /**
     * GET /api/v1/templates/premium
     * Returns premium-tier templates. Requires USER or ADMIN role.
     */
    @GetMapping("/premium")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get premium-only templates (Users/Admin)")
    public ResponseEntity<List<TemplateResponseDTO>> getPremiumTemplates() {
        List<TemplateResponseDTO> templates = templateService.getTemplatesByTier(Tier.PREMIUM)
                .stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(templates);
    }

    /**
     * Retrieves full details of a specific template, including its layout and styles.
     * 
     * @param templateId the unique ID of the template
     * @return the complete template DTO
     */
    @GetMapping("/{templateId}")
    @Operation(summary = "Get single template with full HTML/CSS (Public)")
    public ResponseEntity<TemplateDTO> getTemplateById(@PathVariable Long templateId) {
        return ResponseEntity.ok(TemplateDTO.fromEntity(templateService.getTemplateById(templateId)));
    }

    /**
     * GET /api/v1/templates/category/{category}
     * Filters templates by their category (Public).
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "Filter templates by category (Public)")
    public ResponseEntity<List<TemplateResponseDTO>> getTemplatesByCategory(@PathVariable Category category) {
        List<TemplateResponseDTO> templates = templateService.getTemplatesByCategory(category)
                .stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(templates);
    }

    /**
     * Returns a list of templates sorted by their usage count.
     * Useful for displaying 'Trending' or 'Most Used' templates.
     * 
     * @return a list of popular template summaries
     */
    @GetMapping("/popular")
    @Operation(summary = "Get templates sorted by popularity (Public)")
    public ResponseEntity<List<TemplateResponseDTO>> getPopularTemplates() {
        List<TemplateResponseDTO> templates = templateService.getPopularTemplates()
                .stream()
                .map(this::convertToDTO)
                .toList();
        return ResponseEntity.ok(templates);
    }

    /**
     * PUT /api/v1/templates/{templateId}
     * Updates an existing template. Restricted to ADMIN users.
     */
    @PutMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update template details (Admin only)")
    public ResponseEntity<TemplateDTO> updateTemplate(@PathVariable Long templateId, @RequestBody TemplateDTO templateDto) {
        Template template = templateDto.toEntity();
        return ResponseEntity.ok(TemplateDTO.fromEntity(templateService.updateTemplate(templateId, template)));
    }

    /**
     * PUT /api/v1/templates/{templateId}/deactivate
     * Soft-deletes a template. Restricted to ADMIN users.
     */
    @PutMapping("/{templateId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate a template (Admin only)")
    public ResponseEntity<Void> deactivateTemplate(@PathVariable Long templateId) {
        templateService.deactivateTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/v1/templates/{templateId}/increment-usage
     * Increments usage count (Internal use).
     */
    @PutMapping("/{templateId}/increment-usage")
    @Operation(summary = "Increment template usage count (Internal)")
    public ResponseEntity<Void> incrementUsage(@PathVariable Long templateId) {
        templateService.incrementUsage(templateId);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /api/v1/templates/{templateId}
     * Permanently deletes a template. Restricted to ADMIN users.
     */
    @DeleteMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Permanently delete a template (Admin only)")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long templateId) {
        templateService.deleteTemplate(templateId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Helper method to convert Entity to a lightweight DTO.
     */
    private TemplateResponseDTO convertToDTO(Template template) {
        return TemplateResponseDTO.builder()
                .templateId(template.getTemplateId())
                .name(template.getName())
                .description(template.getDescription())
                .thumbnailUrl(template.getThumbnailUrl())
                .category(template.getCategory())
                .tier(template.getTier())
                .usageCount(template.getUsageCount())
                .isActive(Objects.requireNonNullElse(template.getIsActive(), true))
                .build();
    }
}
