package com.airesume.resumeservice.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResumeTest {

    @Test
    void testResumeBuilderAndAccessors() {
        LocalDateTime now = LocalDateTime.now();
        Resume resume = Resume.builder()
                .resumeId(1L)
                .userId(100L)
                .title("Software Engineer")
                .targetJobTitle("Backend Developer")
                .templateId(5L)
                .atsScore(80)
                .status("DRAFT")
                .language("en")
                .isPublic(true)
                .viewCount(10)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Verify getters
        assertEquals(1L, resume.getResumeId());
        assertEquals(100L, resume.getUserId());
        assertEquals("Software Engineer", resume.getTitle());
        assertEquals("Backend Developer", resume.getTargetJobTitle());
        assertEquals(5L, resume.getTemplateId());
        assertEquals(80, resume.getAtsScore());
        assertEquals("DRAFT", resume.getStatus());
        assertEquals("en", resume.getLanguage());
        assertTrue(resume.isPublic());
        assertEquals(10, resume.getViewCount());
        assertEquals(now, resume.getCreatedAt());
        assertEquals(now, resume.getUpdatedAt());

        // Verify setters
        resume.setResumeId(2L);
        resume.setUserId(200L);
        resume.setTitle("Product Manager");
        resume.setTargetJobTitle("Senior PM");
        resume.setTemplateId(6L);
        resume.setAtsScore(90);
        resume.setStatus("COMPLETE");
        resume.setLanguage("fr");
        resume.setPublic(false);
        resume.setViewCount(20);
        
        LocalDateTime tomorrow = now.plusDays(1);
        resume.setCreatedAt(tomorrow);
        resume.setUpdatedAt(tomorrow);

        assertEquals(2L, resume.getResumeId());
        assertEquals(200L, resume.getUserId());
        assertEquals("Product Manager", resume.getTitle());
        assertEquals("Senior PM", resume.getTargetJobTitle());
        assertEquals(6L, resume.getTemplateId());
        assertEquals(90, resume.getAtsScore());
        assertEquals("COMPLETE", resume.getStatus());
        assertEquals("fr", resume.getLanguage());
        assertFalse(resume.isPublic());
        assertEquals(20, resume.getViewCount());
        assertEquals(tomorrow, resume.getCreatedAt());
        assertEquals(tomorrow, resume.getUpdatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Resume resume = new Resume();
        assertNull(resume.getResumeId());
        assertEquals(0, resume.getAtsScore());
        assertEquals("DRAFT", resume.getStatus());
        assertEquals("en", resume.getLanguage());
        assertFalse(resume.isPublic());
        assertEquals(0, resume.getViewCount());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Resume resume = new Resume(
                1L, 100L, "Title", "Job", 2L, 90, "DRAFT", "es", true, 5, null, now, now
        );

        assertEquals(1L, resume.getResumeId());
        assertEquals(100L, resume.getUserId());
        assertEquals("Title", resume.getTitle());
        assertEquals("Job", resume.getTargetJobTitle());
        assertEquals(2L, resume.getTemplateId());
        assertEquals(90, resume.getAtsScore());
        assertEquals("DRAFT", resume.getStatus());
        assertEquals("es", resume.getLanguage());
        assertTrue(resume.isPublic());
        assertEquals(5, resume.getViewCount());
        assertEquals(now, resume.getCreatedAt());
        assertEquals(now, resume.getUpdatedAt());
    }

    @Test
    void testLifecycleCallbacks() {
        Resume resume = new Resume();
        assertNull(resume.getCreatedAt());
        assertNull(resume.getUpdatedAt());

        // Invoke onCreate
        resume.onCreate();
        assertNotNull(resume.getCreatedAt());
        assertNotNull(resume.getUpdatedAt());
        assertEquals(resume.getCreatedAt(), resume.getUpdatedAt());

        // Invoke onUpdate
        LocalDateTime initialUpdate = resume.getUpdatedAt();
        resume.onUpdate();
        assertNotNull(resume.getUpdatedAt());
        // Verify update time changes/is set
    }
}
