package com.airesume.templateservice;

import com.airesume.templateservice.dto.TemplateResponseDTO;
import com.airesume.templateservice.model.Category;
import com.airesume.templateservice.model.Template;
import com.airesume.templateservice.model.Tier;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Template Service models and data transfer objects.
 * Verifies entity mapping, response formatting, and enum consistency.
 */
class DtoAndModelTest {

    /**
     * Verifies the Template entity data mapping and builder.
     */
    @Test
    void testTemplateModel() {
        Template template = Template.builder()
                .templateId(1L)
                .name("T")
                .description("D")
                .htmlLayout("H")
                .cssStyles("C")
                .category(Category.MODERN)
                .tier(Tier.FREE)
                .thumbnailUrl("U")
                .usageCount(10L)
                .isActive(true)
                .build();

        assertEquals(1L, template.getTemplateId());
        assertEquals("T", template.getName());
        assertEquals("D", template.getDescription());
        assertEquals("H", template.getHtmlLayout());
        assertEquals("C", template.getCssStyles());
        assertEquals(Category.MODERN, template.getCategory());
        assertEquals(Tier.FREE, template.getTier());
        assertEquals("U", template.getThumbnailUrl());
        assertEquals(10L, template.getUsageCount());
        assertTrue(template.getIsActive());

        Template t2 = new Template();
        t2.setName("T2");
        assertEquals("T2", t2.getName());
        
        assertNotNull(template.toString());
    }

    /**
     * Verifies the Template response DTO fields.
     */
    @Test
    void testTemplateResponseDTO() {
        TemplateResponseDTO dto = TemplateResponseDTO.builder()
                .templateId(1L)
                .name("T")
                .description("D")
                .thumbnailUrl("U")
                .category(Category.MODERN)
                .tier(Tier.FREE)
                .usageCount(10L)
                .isActive(true)
                .build();

        assertEquals(1L, dto.getTemplateId());
        assertEquals("T", dto.getName());
        assertNotNull(dto.toString());
        
        TemplateResponseDTO dto2 = new TemplateResponseDTO();
        dto2.setName("T2");
        assertEquals("T2", dto2.getName());
    }

    /**
     * Verifies that the template category and tier enums have correctly defined constants.
     */
    @Test
    void testEnums() {
        assertNotNull(Category.valueOf("MODERN"));
        assertNotNull(Tier.valueOf("FREE"));
    }
}
