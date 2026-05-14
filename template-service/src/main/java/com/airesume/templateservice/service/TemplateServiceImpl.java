package com.airesume.templateservice.service;

import com.airesume.templateservice.exception.TemplateNotFoundException;
import com.airesume.templateservice.model.Category;
import com.airesume.templateservice.model.Template;
import com.airesume.templateservice.model.Tier;
import com.airesume.templateservice.repository.TemplateRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of TemplateService.
 * Handles database interactions and business rules for resume templates.
 */
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;

    /**
     * Creates a new resume template.
     * Evicts template caches to ensure new data is visible immediately.
     */
    @Override
    @Transactional
    @CacheEvict(value = {"templates", "template"}, allEntries = true)
    public Template createTemplate(Template template) {
        template.setIsActive(true);
        template.setUsageCount(0L);
        return templateRepository.save(template);
    }

    /**
     * Retrieves all templates that are currently active and available for use.
     * 
     * @return a list of active templates
     */
    @Override
    public List<Template> getAllActiveTemplates() {
        return templateRepository.findByIsActiveTrue();
    }

    /**
     * Retrieves all templates in the system, including inactive ones.
     * 
     * @return a list of all templates
     */
    @Override
    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    /**
     * Filters active templates based on their tier (e.g., FREE, PREMIUM).
     */
    @Override
    @Cacheable(value = "templates", key = "'tier_' + #tier")
    public List<Template> getTemplatesByTier(Tier tier) {
        return templateRepository.findByIsActiveTrueAndTier(tier);
    }

    /**
     * Retrieves a single template by its unique ID.
     * 
     * @param templateId the ID of the template to fetch
     * @return the requested template entity
     * @throws RuntimeException if the template is not found
     */
    @Override
    @Cacheable(value = "template", key = "#templateId")
    public Template getTemplateById(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + templateId));
    }

    /**
     * Groups active templates by their category (e.g., MODERN, CLASSIC).
     */
    @Override
    @Cacheable(value = "templates", key = "'category_' + #category")
    public List<Template> getTemplatesByCategory(Category category) {
        return templateRepository.findByIsActiveTrueAndCategory(category);
    }

    /**
     * Returns a list of active templates ordered by their usage count.
     * 
     * @return a list of trending/popular templates
     */
    @Override
    @Cacheable(value = "templates", key = "'popular'")
    public List<Template> getPopularTemplates() {
        return templateRepository.findByIsActiveTrueOrderByUsageCountDesc();
    }

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "template"}, allEntries = true)
    public Template updateTemplate(Long templateId, Template templateDetails) {
        Template template = getTemplateById(templateId);
        template.setName(templateDetails.getName());
        template.setDescription(templateDetails.getDescription());
        template.setHtmlLayout(templateDetails.getHtmlLayout());
        template.setCssStyles(templateDetails.getCssStyles());
        template.setCategory(templateDetails.getCategory());
        template.setTier(templateDetails.getTier());
        template.setThumbnailUrl(templateDetails.getThumbnailUrl());
        if (templateDetails.getIsActive() != null) {
            template.setIsActive(templateDetails.getIsActive());
        }
        return templateRepository.save(template);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "template"}, allEntries = true)
    public void deactivateTemplate(Long templateId) {
        Template template = getTemplateById(templateId);
        template.setIsActive(false);
        templateRepository.save(template);
    }

    /**
     * Increments the usage count of a template for popularity tracking.
     */
    @Override
    @Transactional
    @CacheEvict(value = {"templates", "template"}, allEntries = true)
    public void incrementUsage(Long templateId) {
        templateRepository.incrementUsageCount(templateId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "template"}, allEntries = true)
    public void deleteTemplate(Long templateId) {
        if (!templateRepository.existsById(templateId)) {
            throw new TemplateNotFoundException("Template not found with ID: " + templateId);
        }
        templateRepository.deleteById(templateId);
    }
}
