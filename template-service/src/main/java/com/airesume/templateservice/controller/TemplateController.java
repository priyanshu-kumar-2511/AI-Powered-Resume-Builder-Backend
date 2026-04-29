package com.airesume.templateservice.controller;

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
@Tag(name = "Template Management", description = "APIs for managing and retrieving resume templates")
public class TemplateController {

    private final TemplateService templateService;

    /**
     * POST /api/v1/templates
     * Creates a new template. Restricted to ADMIN users.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new template (Admin only)")
    public ResponseEntity<Template> createTemplate(@RequestBody Template template) {
        return new ResponseEntity<>(templateService.createTemplate(template), HttpStatus.CREATED);
    }

    /**
     * GET /api/v1/templates
     * Returns a list of all active templates (Public).
     */
    @GetMapping
    @Operation(summary = "Get all active templates (Public)")
    public ResponseEntity<List<TemplateResponseDTO>> getAllTemplates() {
        List<TemplateResponseDTO> templates = templateService.getAllActiveTemplates()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
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
                .collect(Collectors.toList());
        return ResponseEntity.ok(templates);
    }

    /**
     * GET /api/v1/templates/{templateId}
     * Returns full details (including HTML/CSS) of a single template (Public).
     */
    @GetMapping("/{templateId}")
    @Operation(summary = "Get single template with full HTML/CSS (Public)")
    public ResponseEntity<Template> getTemplateById(@PathVariable Long templateId) {
        return ResponseEntity.ok(templateService.getTemplateById(templateId));
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
                .collect(Collectors.toList());
        return ResponseEntity.ok(templates);
    }

    /**
     * GET /api/v1/templates/popular
     * Returns templates sorted by usage count (Public).
     */
    @GetMapping("/popular")
    @Operation(summary = "Get templates sorted by popularity (Public)")
    public ResponseEntity<List<TemplateResponseDTO>> getPopularTemplates() {
        List<TemplateResponseDTO> templates = templateService.getPopularTemplates()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(templates);
    }

    /**
     * PUT /api/v1/templates/{templateId}
     * Updates an existing template. Restricted to ADMIN users.
     */
    @PutMapping("/{templateId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update template details (Admin only)")
    public ResponseEntity<Template> updateTemplate(@PathVariable Long templateId, @RequestBody Template template) {
        return ResponseEntity.ok(templateService.updateTemplate(templateId, template));
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
                .build();
    }
}
