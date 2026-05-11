package com.airesume.exportservice.service;

import com.airesume.exportservice.client.ResumeServiceClient;
import com.airesume.exportservice.client.SectionServiceClient;
import com.airesume.exportservice.client.TemplateServiceClient;
import com.airesume.exportservice.dto.ResumeResponseDTO;
import com.airesume.exportservice.dto.SectionDTO;
import com.airesume.exportservice.dto.TemplateDTO;
import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.repository.ExportJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportJobProcessorTest {

    @Mock
    private ExportJobRepository repository;
    @Mock
    private ResumeServiceClient resumeClient;
    @Mock
    private TemplateServiceClient templateClient;
    @Mock
    private SectionServiceClient sectionClient;
    @Mock
    private PdfGeneratorService pdfGenerator;

    @InjectMocks
    private ExportJobProcessor exportJobProcessor;

    private ExportJob job;
    private String tempStoragePath;

    @BeforeEach
    void setUp() throws Exception {
        Path tempDir = Files.createTempDirectory("exports_test");
        tempStoragePath = tempDir.toString();
        ReflectionTestUtils.setField(exportJobProcessor, "storagePath", tempStoragePath);

        job = new ExportJob();
        job.setJobId("job-123");
        job.setResumeId(1L);
        job.setFormat(ExportFormat.PDF);
    }

    @Test
    void testProcessJob_JobNotFound() {
        when(repository.findById("job-123")).thenReturn(Optional.empty());

        exportJobProcessor.processJob("job-123", "Bearer token");

        verify(resumeClient, never()).getResumeById(any());
    }

    @Test
    void testProcessJob_SuccessPDF() throws Exception {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        ResumeResponseDTO resume = new ResumeResponseDTO();
        resume.setTemplateId(2L);
        when(resumeClient.getResumeById(1L)).thenReturn(resume);
        
        when(sectionClient.getSectionsByResume(1L)).thenReturn(List.of(new SectionDTO()));
        
        TemplateDTO template = new TemplateDTO();
        template.setTemplateId(2L);
        when(templateClient.getTemplateById(2L)).thenReturn(template);
        
        when(pdfGenerator.generatePdf(any(), any(), any())).thenReturn(new byte[]{1, 2, 3});

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.COMPLETED, job.getStatus());
        assertNotNull(job.getFileUrl());
        assertTrue(new File(job.getFileUrl()).exists());
        verify(repository, times(2)).save(job); // once for processing, once for complete
    }

    @Test
    void testProcessJob_ResumeClientThrows() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        when(resumeClient.getResumeById(1L)).thenThrow(new RuntimeException("Resume not found"));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertTrue(job.getFailureReason().contains("Resume not found"));
    }

    @Test
    void testProcessJob_SectionClientThrows() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        when(resumeClient.getResumeById(1L)).thenReturn(new ResumeResponseDTO());
        when(sectionClient.getSectionsByResume(1L)).thenThrow(new RuntimeException("Section API failed"));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertTrue(job.getFailureReason().contains("Section API failed"));
    }

    @Test
    void testProcessJob_TemplateClientThrows() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        ResumeResponseDTO resume = new ResumeResponseDTO();
        resume.setTemplateId(2L);
        when(resumeClient.getResumeById(1L)).thenReturn(resume);
        
        when(sectionClient.getSectionsByResume(1L)).thenReturn(List.of());
        
        when(templateClient.getTemplateById(2L)).thenThrow(new RuntimeException("Template missing"));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertTrue(job.getFailureReason().contains("Template missing"));
    }

    @Test
    void testProcessJob_UnsupportedFormat() {
        job.setFormat(ExportFormat.DOCX); // Assume this is not implemented yet
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        ResumeResponseDTO resume = new ResumeResponseDTO();
        when(resumeClient.getResumeById(1L)).thenReturn(resume);
        when(sectionClient.getSectionsByResume(1L)).thenReturn(List.of());

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertTrue(job.getFailureReason().contains("is not supported"));
    }

    @Test
    void testProcessJob_FailureReasonNullMessage() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        when(resumeClient.getResumeById(1L)).thenThrow(new RuntimeException((String) null));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertEquals("Export job failed unexpectedly while preparing the file.", job.getFailureReason());
    }

    @Test
    void testProcessJob_FailureReasonBlankMessage() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        when(resumeClient.getResumeById(1L)).thenThrow(new RuntimeException("   "));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertEquals("Export job failed unexpectedly while preparing the file.", job.getFailureReason());
    }

    @Test
    void testProcessJob_FailureReasonLongMessage() {
        String longMessage = "A".repeat(600);
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        when(resumeClient.getResumeById(1L)).thenThrow(new RuntimeException(longMessage));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertEquals(500, job.getFailureReason().length());
        assertEquals(longMessage.substring(0, 500), job.getFailureReason());
    }

    @Test
    void testProcessJob_SuccessPDF_TemplateNull() throws Exception {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        ResumeResponseDTO resume = new ResumeResponseDTO();
        resume.setTemplateId(2L);
        when(resumeClient.getResumeById(1L)).thenReturn(resume);
        
        when(sectionClient.getSectionsByResume(1L)).thenReturn(List.of(new SectionDTO()));
        
        when(templateClient.getTemplateById(2L)).thenReturn(null);
        
        when(pdfGenerator.generatePdf(any(), any(), any())).thenReturn(new byte[]{1, 2, 3});

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.COMPLETED, job.getStatus());
        assertEquals(2L, job.getTemplateId()); // falls back to resume's templateId
    }

    @Test
    void testProcessJob_AuthorizationHeaderNull() throws Exception {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        ResumeResponseDTO resume = new ResumeResponseDTO();
        resume.setTemplateId(2L);
        when(resumeClient.getResumeById(1L)).thenReturn(resume);
        when(sectionClient.getSectionsByResume(1L)).thenReturn(List.of());
        TemplateDTO template = new TemplateDTO();
        when(templateClient.getTemplateById(2L)).thenReturn(template);
        when(pdfGenerator.generatePdf(any(), any(), any())).thenReturn(new byte[]{1, 2, 3});

        exportJobProcessor.processJob("job-123", null);

        assertEquals(ExportStatus.COMPLETED, job.getStatus());
    }

    @Test
    void testProcessJob_AuthorizationHeaderBlank() throws Exception {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        ResumeResponseDTO resume = new ResumeResponseDTO();
        resume.setTemplateId(2L);
        when(resumeClient.getResumeById(1L)).thenReturn(resume);
        when(sectionClient.getSectionsByResume(1L)).thenReturn(List.of());
        TemplateDTO template = new TemplateDTO();
        when(templateClient.getTemplateById(2L)).thenReturn(template);
        when(pdfGenerator.generatePdf(any(), any(), any())).thenReturn(new byte[]{1, 2, 3});

        exportJobProcessor.processJob("job-123", "   ");

        assertEquals(ExportStatus.COMPLETED, job.getStatus());
    }
}
