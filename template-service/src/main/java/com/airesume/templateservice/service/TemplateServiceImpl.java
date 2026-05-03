package com.airesume.templateservice.service;

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

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "template"}, allEntries = true)
    public Template createTemplate(Template template) {
        template.setIsActive(true);
        template.setUsageCount(0L);
        return templateRepository.save(template);
    }

    public List<Template> getAllActiveTemplates() {
        return templateRepository.findByIsActiveTrue();
    }

    @Override
    public List<Template> getAllTemplates() {
        return templateRepository.findAll();
    }

    @Override
    @Cacheable(value = "templates", key = "'tier_' + #tier")
    public List<Template> getTemplatesByTier(Tier tier) {
        return templateRepository.findByIsActiveTrueAndTier(tier);
    }

    @Override
    @Cacheable(value = "template", key = "#templateId")
    public Template getTemplateById(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));
    }

    @Override
    @Cacheable(value = "templates", key = "'category_' + #category")
    public List<Template> getTemplatesByCategory(Category category) {
        return templateRepository.findByIsActiveTrueAndCategory(category);
    }

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

    @Override
    @Transactional
    @CacheEvict(value = {"templates", "template"}, allEntries = true)
    public void incrementUsage(Long templateId) {
        templateRepository.incrementUsageCount(templateId);
    }
}
