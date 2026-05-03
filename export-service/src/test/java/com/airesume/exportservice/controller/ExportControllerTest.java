package com.airesume.exportservice.controller;

import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.security.CurrentUserService;
import com.airesume.exportservice.service.ExportService;
import com.airesume.exportservice.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExportController.class)
@AutoConfigureMockMvc(addFilters = false) // Skip security filters for simpler controller testing
class ExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExportService exportService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser
    void exportPdf_Success() throws Exception {
        // Arrange
        Long resumeId = 101L;
        ExportJob mockJob = ExportJob.builder()
                .jobId("test-job-uuid")
                .status(ExportStatus.QUEUED)
                .build();

        when(currentUserService.requireUserId()).thenReturn(1L);
        when(exportService.submitExportJob(anyLong(), eq(resumeId), eq(ExportFormat.PDF), any(), any()))
                .thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(post("/pdf/" + resumeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("test-job-uuid"))
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    @Test
    @WithMockUser
    void getJobStatus_Success() throws Exception {
        // Arrange
        String jobId = "test-job-uuid";
        ExportJob mockJob = ExportJob.builder()
                .jobId(jobId)
                .status(ExportStatus.COMPLETED)
                .build();

        when(exportService.getJobStatus(jobId)).thenReturn(mockJob);

        // Act & Assert
        mockMvc.perform(get("/job/" + jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser
    void downloadFile_Success() throws Exception {
        // Arrange
        String jobId = "test-job-uuid";
        byte[] mockContent = "PDF content".getBytes();
        ExportJob mockJob = ExportJob.builder()
                .resumeId(1L)
                .format(ExportFormat.PDF)
                .build();

        when(exportService.getJobStatus(jobId)).thenReturn(mockJob);
        when(exportService.downloadFile(jobId)).thenReturn(mockContent);

        // Act & Assert
        mockMvc.perform(get("/download/" + jobId))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"resume_1.pdf\""))
                .andExpect(content().bytes(mockContent));
    }

    @Test
    @WithMockUser
    void exportDocx_Success() throws Exception {
        Long resumeId = 101L;
        ExportJob mockJob = ExportJob.builder()
                .jobId("docx-job")
                .status(ExportStatus.QUEUED)
                .build();

        when(currentUserService.requireUserId()).thenReturn(1L);
        when(exportService.submitExportJob(anyLong(), eq(resumeId), eq(ExportFormat.DOCX), any(), any()))
                .thenReturn(mockJob);

        mockMvc.perform(post("/docx/" + resumeId)
                .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("docx-job"));
    }

    @Test
    @WithMockUser
    void exportJson_Success() throws Exception {
        Long resumeId = 101L;
        ExportJob mockJob = ExportJob.builder()
                .jobId("json-job")
                .status(ExportStatus.QUEUED)
                .build();

        when(currentUserService.requireUserId()).thenReturn(1L);
        when(exportService.submitExportJob(anyLong(), eq(resumeId), eq(ExportFormat.JSON), any(), any()))
                .thenReturn(mockJob);

        mockMvc.perform(post("/json/" + resumeId)
                .with(csrf()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("json-job"));
    }
}
