package com.airesume.templateservice.service;

import java.util.List;

import com.airesume.templateservice.model.Category;
import com.airesume.templateservice.model.Template;
import com.airesume.templateservice.model.Tier;

/**
 * Service interface defining business logic for resume templates.
 */
public interface TemplateService {
    
    /**
     * Create a new resume template (Admin only).
     */
    Template createTemplate(Template template);

    /**
     * Retrieve all active templates.
     */
    List<Template> getAllActiveTemplates();

    /**
     * Retrieve all templates, including inactive ones (Admin only).
     */
    List<Template> getAllTemplates();

    /**
     * Retrieve templates filtered by access tier.
     */
    List<Template> getTemplatesByTier(Tier tier);

    /**
     * Retrieve a specific template by its ID.
     */
    Template getTemplateById(Long templateId);

    /**
     * Retrieve templates filtered by style category.
     */
    List<Template> getTemplatesByCategory(Category category);

    /**
     * Retrieve active templates sorted by usage popularity.
     */
    List<Template> getPopularTemplates();

    /**
     * Update an existing template's metadata or layout.
     */
    Template updateTemplate(Long templateId, Template template);

    /**
     * Soft-delete/deactivate a template.
     */
    void deactivateTemplate(Long templateId);

    /**
     * Increment the usage count when a resume is created with this template.
     */
    void incrementUsage(Long templateId);

    /**
     * Permanently delete a template from the system (Admin only).
     */
    void deleteTemplate(Long templateId);
}
