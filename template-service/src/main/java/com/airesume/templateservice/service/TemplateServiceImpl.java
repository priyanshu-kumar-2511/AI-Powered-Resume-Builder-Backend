package com.airesume.templateservice.service;

import com.airesume.templateservice.model.Category;
import com.airesume.templateservice.model.Template;
import com.airesume.templateservice.model.Tier;
import com.airesume.templateservice.repository.TemplateRepository;

import lombok.RequiredArgsConstructor;
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
    public Template createTemplate(Template template) {
        template.setIsActive(true);
        template.setUsageCount(0L);
        return templateRepository.save(template);
    }

    @Override
    public List<Template> getAllActiveTemplates() {
        return templateRepository.findByIsActiveTrue();
    }

    @Override
    public List<Template> getTemplatesByTier(Tier tier) {
        return templateRepository.findByIsActiveTrueAndTier(tier);
    }

    @Override
    public Template getTemplateById(Long templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with ID: " + templateId));
    }

    @Override
    public List<Template> getTemplatesByCategory(Category category) {
        return templateRepository.findByIsActiveTrueAndCategory(category);
    }

    @Override
    public List<Template> getPopularTemplates() {
        return templateRepository.findByIsActiveTrueOrderByUsageCountDesc();
    }

    @Override
    @Transactional
    public Template updateTemplate(Long templateId, Template templateDetails) {
        Template template = getTemplateById(templateId);
        template.setName(templateDetails.getName());
        template.setDescription(templateDetails.getDescription());
        template.setHtmlLayout(templateDetails.getHtmlLayout());
        template.setCssStyles(templateDetails.getCssStyles());
        template.setCategory(templateDetails.getCategory());
        template.setTier(templateDetails.getTier());
        template.setThumbnailUrl(templateDetails.getThumbnailUrl());
        return templateRepository.save(template);
    }

    @Override
    @Transactional
    public void deactivateTemplate(Long templateId) {
        Template template = getTemplateById(templateId);
        template.setIsActive(false);
        templateRepository.save(template);
    }

    @Override
    @Transactional
    public void incrementUsage(Long templateId) {
        templateRepository.incrementUsageCount(templateId);
    }
}
