package com.airesume.exportservice.dto;

import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndEntityTest {

    @Test
    void testResumeResponseDTO() {
        ResumeResponseDTO dto = new ResumeResponseDTO();
        dto.setResumeId(1L);
        dto.setUserId(2L);
        dto.setTemplateId(3L);
        dto.setTitle("Resume");
        dto.setTargetJobTitle("Engineer");
        dto.setSections(List.of(new SectionDTO()));

        assertEquals(1L, dto.getResumeId());
        assertEquals(2L, dto.getUserId());
        assertEquals(3L, dto.getTemplateId());
        assertEquals("Resume", dto.getTitle());
        assertEquals("Engineer", dto.getTargetJobTitle());
        assertNotNull(dto.getSections());
    }

    @Test
    void testTemplateDTO() {
        TemplateDTO dto = new TemplateDTO();
        dto.setTemplateId(1L);
        dto.setName("Template");
        dto.setHtmlLayout("<html>");
        dto.setCssStyles("css");

        assertEquals(1L, dto.getTemplateId());
        assertEquals("Template", dto.getName());
        assertEquals("<html>", dto.getHtmlLayout());
        assertEquals("css", dto.getCssStyles());
    }

    @Test
    void testSectionDTO() {
        SectionDTO dto = new SectionDTO();
        dto.setSectionId(1L);
        dto.setSectionType("TYPE");
        dto.setTitle("TITLE");
        dto.setContent("{}");
        dto.setIsVisible(true);
        dto.setDisplayOrder(3);

        assertEquals(1L, dto.getSectionId());
        assertEquals("TYPE", dto.getSectionType());
        assertEquals("TITLE", dto.getTitle());
        assertEquals("{}", dto.getContent());
        assertTrue(dto.getIsVisible());
        assertEquals(3, dto.getDisplayOrder());
    }

    @Test
    void testExportStatsDTO() {
        ExportStatsDTO dto = ExportStatsDTO.builder()
                .userId(1L)
                .totalExports(10L)
                .todayPdfCount(5L)
                .countByFormat(new HashMap<>())
                .build();

        assertEquals(1L, dto.getUserId());
        assertEquals(10L, dto.getTotalExports());
        assertEquals(5L, dto.getTodayPdfCount());
        assertNotNull(dto.getCountByFormat());
    }

    @Test
    void testExportJob_OnCreate_WithNulls() throws Exception {
        ExportJob job = new ExportJob();
        assertNull(job.getJobId());
        assertNull(job.getStatus());
        assertNull(job.getRequestedAt());

        // Invoke onCreate via reflection
        Method onCreateMethod = ExportJob.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(job);

        assertNotNull(job.getJobId());
        assertEquals(ExportStatus.QUEUED, job.getStatus());
        assertNotNull(job.getRequestedAt());
    }

    @Test
    void testExportJob_OnCreate_WithValues() throws Exception {
        ExportJob job = ExportJob.builder()
                .jobId("existing-id")
                .userId(1L)
                .resumeId(2L)
                .format(ExportFormat.PDF)
                .status(ExportStatus.PROCESSING)
                .fileUrl("url")
                .fileSizeKb(100L)
                .requestedAt(LocalDateTime.MIN)
                .processingStartedAt(LocalDateTime.now())
                .completedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now())
                .templateId(5L)
                .customizations("{}")
                .failureReason("error")
                .build();

        // Verify values
        assertEquals("existing-id", job.getJobId());
        assertEquals(1L, job.getUserId());
        assertEquals(2L, job.getResumeId());
        assertEquals(ExportFormat.PDF, job.getFormat());
        assertEquals(ExportStatus.PROCESSING, job.getStatus());
        assertEquals("url", job.getFileUrl());
        assertEquals(100L, job.getFileSizeKb());
        assertEquals(LocalDateTime.MIN, job.getRequestedAt());
        assertNotNull(job.getProcessingStartedAt());
        assertNotNull(job.getCompletedAt());
        assertNotNull(job.getExpiresAt());
        assertEquals(5L, job.getTemplateId());
        assertEquals("{}", job.getCustomizations());
        assertEquals("error", job.getFailureReason());

        // Invoke onCreate
        Method onCreateMethod = ExportJob.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(job);

        // Should NOT overwrite existing jobId or status, but will overwrite requestedAt
        assertEquals("existing-id", job.getJobId());
        assertEquals(ExportStatus.PROCESSING, job.getStatus());
        assertNotEquals(LocalDateTime.MIN, job.getRequestedAt());
    }
}
