package com.resumeai.template.service;

import com.resumeai.template.model.Category;
import com.resumeai.template.model.Template;
import com.resumeai.template.model.Tier;

import java.util.List;

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
}
