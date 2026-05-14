package com.airesume.sectionservice.dto;

import com.airesume.sectionservice.model.Section;
import com.airesume.sectionservice.model.SectionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Section domain models and types.
 * Verifies entity data binding, builder patterns, and enum consistency.
 */
public class DtoTest {

    /**
     * Verifies the Section entity data mapping and builder functionality.
     */
    @Test
    void testSectionModel() {
        Section section = new Section();
        section.setSectionId(1L);
        section.setResumeId(10L);
        section.setSectionType(SectionType.EXPERIENCE);
        section.setTitle("Work Experience");
        section.setContent("{\"company\":\"Google\"}");
        section.setDisplayOrder(1);
        section.setIsVisible(true);
        section.setAiGenerated(false);
        
        LocalDateTime now = LocalDateTime.now();
        section.setCreatedAt(now);
        section.setUpdatedAt(now);

        assertEquals(1L, section.getSectionId());
        assertEquals(10L, section.getResumeId());
        assertEquals(SectionType.EXPERIENCE, section.getSectionType());
        assertEquals("Work Experience", section.getTitle());
        assertEquals("{\"company\":\"Google\"}", section.getContent());
        assertEquals(1, section.getDisplayOrder());
        assertTrue(section.getIsVisible());
        assertFalse(section.getAiGenerated());
        assertEquals(now, section.getCreatedAt());
        assertEquals(now, section.getUpdatedAt());

        // Test Builder
        Section section2 = Section.builder()
                .sectionId(2L)
                .resumeId(20L)
                .sectionType(SectionType.EDUCATION)
                .title("Education")
                .content("{}")
                .displayOrder(2)
                .isVisible(false)
                .aiGenerated(true)
                .build();

        assertEquals(2L, section2.getSectionId());
        assertEquals("Education", section2.getTitle());
        assertTrue(section2.getAiGenerated());
    }

    /**
     * Verifies the SectionType enum constants and string mapping.
     */
    @Test
    void testSectionTypeEnum() {
        assertEquals("VOLUNTEER", SectionType.VOLUNTEER.name());
        assertEquals("SUMMARY", SectionType.SUMMARY.name());
        assertEquals("EXPERIENCE", SectionType.EXPERIENCE.name());
        assertEquals("EDUCATION", SectionType.EDUCATION.name());
        assertEquals("SKILLS", SectionType.SKILLS.name());
        assertEquals("PROJECTS", SectionType.PROJECTS.name());
        assertEquals("CERTIFICATIONS", SectionType.CERTIFICATIONS.name());
        assertEquals("LANGUAGES", SectionType.LANGUAGES.name());
        assertEquals("CUSTOM", SectionType.CUSTOM.name());

        SectionType[] types = SectionType.values();
        assertTrue(types.length > 0);
    }
}
