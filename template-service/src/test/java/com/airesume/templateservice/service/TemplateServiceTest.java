package com.airesume.templateservice.service;

import com.airesume.templateservice.model.Category;
import com.airesume.templateservice.model.Template;
import com.airesume.templateservice.model.Tier;
import com.airesume.templateservice.repository.TemplateRepository;
import com.airesume.templateservice.service.TemplateServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for TemplateService.
 * Technology: JUnit 5 + Mockito.
 */
@ExtendWith(MockitoExtension.class)
public class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository; // Mocking the repository dependency

    @InjectMocks
    private TemplateServiceImpl templateService; // Injecting mocks into the service implementation

    private Template template;

    @BeforeEach
    void setUp() {
        // Initializing a sample template for reuse in tests
        template = Template.builder()
                .templateId(1L)
                .name("Modern Professional")
                .description("A sleek modern design")
                .category(Category.MODERN)
                .tier(Tier.FREE)
                .isActive(true)
                .usageCount(0L)
                .build();
    }

    @Test
    @DisplayName("Test: Create Template should save correctly")
    void createTemplate_ShouldReturnSavedTemplate() {
        // GIVEN: Repository returns the template when saved
        when(templateRepository.save(any(Template.class))).thenReturn(template);
        
        // WHEN: Calling createTemplate
        Template savedTemplate = templateService.createTemplate(template);
        
        // THEN: Verify the result and repository interaction
        assertNotNull(savedTemplate);
        assertEquals("Modern Professional", savedTemplate.getName());
        verify(templateRepository, times(1)).save(any(Template.class));
    }

    @Test
    @DisplayName("Test: Get Template by ID - Success Scenario")
    void getTemplateById_ShouldReturnTemplate_WhenExists() {
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        
        Template foundTemplate = templateService.getTemplateById(1L);
        
        assertNotNull(foundTemplate);
        assertEquals(1L, foundTemplate.getTemplateId());
    }

    @Test
    @DisplayName("Test: Get Template by ID - Not Found Scenario")
    void getTemplateById_ShouldThrowException_WhenNotExists() {
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> templateService.getTemplateById(99L));
    }

    @Test
    @DisplayName("Test: Retrieve all Active Templates")
    void getAllActiveTemplates_ShouldReturnList() {
        when(templateRepository.findByIsActiveTrue()).thenReturn(Arrays.asList(template));
        
        List<Template> templates = templateService.getAllActiveTemplates();
        
        assertFalse(templates.isEmpty());
        assertEquals(1, templates.size());
    }

    @Test
    @DisplayName("Test: Atomic increment of usage count")
    void incrementUsage_ShouldCallRepository() {
        doNothing().when(templateRepository).incrementUsageCount(1L);
        
        templateService.incrementUsage(1L);
        
        verify(templateRepository, times(1)).incrementUsageCount(1L);
    }

    @Test
    @DisplayName("Test: Template Deactivation (Soft Delete)")
    void deactivateTemplate_ShouldSetIsActiveToFalse() {
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(templateRepository.save(any(Template.class))).thenReturn(template);
        
        templateService.deactivateTemplate(1L);
        
        assertFalse(template.getIsActive());
        verify(templateRepository, times(1)).save(template);
    }
}
