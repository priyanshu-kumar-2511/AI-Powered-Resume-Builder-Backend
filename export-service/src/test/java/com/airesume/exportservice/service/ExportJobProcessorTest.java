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

/**
 * Comprehensive unit tests for the Export Job Processor.
 * Verifies the full lifecycle of background document generation, including
 * inter-service data fetching, PDF rendering, and robust error handling
 * for service timeouts and API failures.
 */
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

    /**
     * Verifies that processing stops gracefully if the job ID is not found in the repository.
     */
    @Test
    void testProcessJob_JobNotFound() {
        when(repository.findById("job-123")).thenReturn(Optional.empty());

        exportJobProcessor.processJob("job-123", "Bearer token");

        verify(resumeClient, never()).getResumeById(any());
    }

    /**
     * Tests a successful PDF export workflow, ensuring all dependencies are called correctly.
     */
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

    /**
     * Verifies that failures in the Resume service correctly mark the export job as FAILED.
     */
    @Test
    void testProcessJob_ResumeClientThrows() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        when(resumeClient.getResumeById(1L)).thenThrow(new RuntimeException("Resume not found"));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertTrue(job.getFailureReason().contains("Resume not found"));
    }

    /**
     * Verifies that failures in the Section service correctly mark the export job as FAILED.
     */
    @Test
    void testProcessJob_SectionClientThrows() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        when(resumeClient.getResumeById(1L)).thenReturn(new ResumeResponseDTO());
        when(sectionClient.getSectionsByResume(1L)).thenThrow(new RuntimeException("Section API failed"));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertTrue(job.getFailureReason().contains("Section API failed"));
    }

    /**
     * Verifies that failures in the Template service correctly mark the export job as FAILED.
     */
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

    /**
     * Verifies that attempting to export in an unsupported format results in a FAILED job status.
     */
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

    /**
     * Verifies that a generic error message is provided when a processing exception has no message.
     */
    @Test
    void testProcessJob_FailureReasonNullMessage() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        when(resumeClient.getResumeById(1L)).thenThrow(new RuntimeException((String) null));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertEquals("Export job failed unexpectedly while preparing the file.", job.getFailureReason());
    }

    /**
     * Verifies that a generic error message is provided when a processing exception has a blank message.
     */
    @Test
    void testProcessJob_FailureReasonBlankMessage() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        when(resumeClient.getResumeById(1L)).thenThrow(new RuntimeException("   "));

        exportJobProcessor.processJob("job-123", "Bearer token");

        assertEquals(ExportStatus.FAILED, job.getStatus());
        assertEquals("Export job failed unexpectedly while preparing the file.", job.getFailureReason());
    }

    /**
     * Verifies that exceptionally long error messages are truncated to fit database column constraints.
     */
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

    /**
     * Verifies that the processor falls back to the resume's template ID if the Template service returns null.
     */
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

    /**
     * Verifies that the processor handles null authorization headers without crashing.
     */
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

    /**
     * Verifies that the processor handles blank authorization headers without crashing.
     */
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
