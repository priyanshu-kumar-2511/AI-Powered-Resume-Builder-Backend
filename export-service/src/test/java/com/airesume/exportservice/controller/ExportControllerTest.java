package com.airesume.exportservice.controller;

import com.airesume.exportservice.dto.ExportStatsDTO;
import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.security.CurrentUserService;
import com.airesume.exportservice.service.ExportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the Export Controller.
 * Verifies the submission and retrieval of resume export jobs
 * across different formats (PDF, DOCX, JSON).
 */
@WebMvcTest(ExportController.class)
@AutoConfigureMockMvc(addFilters = false) // Disabling security filters for controller logic testing
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExportService exportService;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private com.airesume.exportservice.service.JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExportJob job;

    @BeforeEach
    void setUp() {
        job = new ExportJob();
        job.setJobId("job-123");
        job.setResumeId(1L);
        job.setUserId(1L);
        job.setFormat(ExportFormat.PDF);
        job.setStatus(ExportStatus.COMPLETED);

        when(currentUserService.requireUserId()).thenReturn(1L);
    }

    /**
     * Verifies that submitting a PDF export request returns 202 Accepted.
     */
    @Test
    void testExportPdf() throws Exception {
        when(exportService.submitExportJob(eq(1L), eq(1L), eq(ExportFormat.PDF), any(), any())).thenReturn(job);

        mockMvc.perform(post("/pdf/1")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"red\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-123"));
    }

    /**
     * Verifies that submitting a DOCX export request returns 202 Accepted.
     */
    @Test
    void testExportDocx() throws Exception {
        when(exportService.submitExportJob(eq(1L), eq(1L), eq(ExportFormat.DOCX), any(), any())).thenReturn(job);

        mockMvc.perform(post("/docx/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-123"));
    }

    /**
     * Verifies that submitting a JSON export request returns 202 Accepted.
     */
    @Test
    void testExportJson() throws Exception {
        when(exportService.submitExportJob(eq(1L), eq(1L), eq(ExportFormat.JSON), any(), any())).thenReturn(job);

        mockMvc.perform(post("/json/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-123"));
    }

    /**
     * Tests the retrieval of a background export job's current status.
     */
    @Test
    void testGetJobStatus() throws Exception {
        when(exportService.getJobStatus("job-123")).thenReturn(job);

        mockMvc.perform(get("/job/job-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value("job-123"));
    }

    /**
     * Verifies retrieval of all export jobs associated with the current user.
     */
    @Test
    void testGetExportsByUser() throws Exception {
        when(exportService.getExportsByUser(1L)).thenReturn(List.of(job));

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].jobId").value("job-123"));
    }

    /**
     * Verifies that generated export files can be downloaded with correct headers.
     */
    @Test
    void testDownloadFile() throws Exception {
        when(exportService.getJobStatus("job-123")).thenReturn(job);
        when(exportService.downloadFile("job-123")).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/download/job-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume_1.pdf\""))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    /**
     * Verifies that an export job and its associated file can be deleted.
     */
    @Test
    void testDeleteExport() throws Exception {
        mockMvc.perform(delete("/job-123"))
                .andExpect(status().isNoContent());

        verify(exportService).deleteExport("job-123");
    }

    /**
     * Verifies retrieval of personal export statistics for a user.
     */
    @Test
    void testGetStats() throws Exception {
        ExportStatsDTO stats = ExportStatsDTO.builder().userId(1L).totalExports(10L).build();
        when(exportService.getUserStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/stats/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExports").value(10));
    }

    /**
     * Verifies that administrators can manually trigger the cleanup of expired export files.
     */
    @Test
    void testCleanup() throws Exception {
        mockMvc.perform(delete("/internal/cleanup-expired"))
                .andExpect(status().isOk());

        verify(exportService).cleanupExpiredExports();
    }

    /**
     * Verifies that administrators can retrieve global export statistics across all users.
     */
    @Test
    void testGetAdminStats() throws Exception {
        when(exportService.getAdminStats()).thenReturn(Map.of("PDF", 100L));

        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PDF").value(100));
    }

    /**
     * Verifies the daily PDF export count for a specific user.
     */
    @Test
    void testGetDailyCount() throws Exception {
        when(exportService.getDailyPdfCount(1L)).thenReturn(5L);

        mockMvc.perform(get("/admin/user/1/daily-count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    /**
     * Verifies that users can retrieve their own export jobs even if IDs are mismatched (legacy check).
     */
    @Test
    void testGetExportsByUser_DifferentUserId() throws Exception {
        // Current user is 1, but requesting stats/exports for user 2
        when(currentUserService.requireUserId()).thenReturn(1L);
        when(exportService.getExportsByUser(2L)).thenReturn(List.of());

        mockMvc.perform(get("/user/2"))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that non-PDF files (e.g., DOCX) are served with the correct octet-stream MIME type.
     */
    @Test
    void testDownloadFile_NonPdf() throws Exception {
        job.setFormat(ExportFormat.DOCX); // non-PDF
        when(exportService.getJobStatus("job-123")).thenReturn(job);
        when(exportService.downloadFile("job-123")).thenReturn(new byte[]{4, 5, 6});

        mockMvc.perform(get("/download/job-123"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume_1.docx\""))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(new byte[]{4, 5, 6}));
    }
}
