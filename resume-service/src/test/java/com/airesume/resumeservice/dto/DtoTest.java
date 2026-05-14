package com.airesume.resumeservice.dto;

import com.airesume.resumeservice.model.Resume;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Resume service Data Transfer Objects.
 * Verifies property mapping for resume creation, update requests, 
 * and ATS score synchronization models.
 */
class DtoTest {

    /**
     * Verifies that the ResumeResponse DTO correctly maps all fields from a Resume entity.
     */
    @Test
    void resumeResponse_ShouldMapFieldsFromResume() {
        Resume resume = Resume.builder()
                .resumeId(1L)
                .userId(100L)
                .title("Software Engineer")
                .targetJobTitle("Backend Developer")
                .templateId(10L)
                .atsScore(85)
                .status("COMPLETE")
                .language("en")
                .isPublic(true)
                .viewCount(5)
                .build();

        ResumeResponse response = new ResumeResponse(resume);

        assertEquals(1L, response.getResumeId());
        assertEquals(100L, response.getUserId());
        assertEquals("Software Engineer", response.getTitle());
        assertEquals("Backend Developer", response.getTargetJobTitle());
        assertEquals(10L, response.getTemplateId());
        assertEquals(85, response.getAtsScore());
        assertEquals("COMPLETE", response.getStatus());
        assertEquals("en", response.getLanguage());
        assertTrue(response.isPublic());
        assertEquals(5, response.getViewCount());
    }

    /**
     * Verifies the data integrity of a resume creation request.
     */
    @Test
    void resumeCreateRequest_ShouldSetGetFields() {
        ResumeCreateRequest req = new ResumeCreateRequest();
        req.setUserId(1L);
        req.setTitle("My Resume");
        req.setTemplateId(5L);
        req.setTargetJobTitle("Engineer");
        req.setLanguage("fr");

        assertEquals(1L, req.getUserId());
        assertEquals("My Resume", req.getTitle());
        assertEquals(5L, req.getTemplateId());
        assertEquals("Engineer", req.getTargetJobTitle());
        assertEquals("fr", req.getLanguage());
    }

    /**
     * Verifies the data integrity of a resume update request.
     */
    @Test
    void resumeUpdateRequest_ShouldSetGetFields() {
        ResumeUpdateRequest req = new ResumeUpdateRequest();
        req.setTitle("Updated");
        req.setTargetJobTitle("Lead");
        req.setLanguage("de");
        req.setStatus("COMPLETE");

        assertEquals("Updated", req.getTitle());
        assertEquals("Lead", req.getTargetJobTitle());
        assertEquals("de", req.getLanguage());
        assertEquals("COMPLETE", req.getStatus());
    }

    /**
     * Verifies that the ATS score update DTO correctly captures and returns score data.
     */
    @Test
    void atsUpdateDTO_ShouldSetGetFields() {
        AtsUpdateDTO dto = new AtsUpdateDTO();
        dto.setAtsScore(90);

        assertEquals(90, dto.getAtsScore());
    }

    /**
     * Verifies that the ResumeResponse DTO can be instantiated correctly even if only partial metadata is available.
     */
    @Test
    void resumeResponse_ShouldHandleTimestamps() {
        LocalDateTime now = LocalDateTime.now();
        Resume resume = Resume.builder()
                .resumeId(2L)
                .userId(50L)
                .title("Test")
                .build();

        ResumeResponse response = new ResumeResponse(resume);
        assertNotNull(response);
        assertEquals(2L, response.getResumeId());
    }
}
