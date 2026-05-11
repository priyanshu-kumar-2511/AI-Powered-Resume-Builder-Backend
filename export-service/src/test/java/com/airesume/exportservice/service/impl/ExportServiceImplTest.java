package com.airesume.exportservice.service.impl;

import com.airesume.exportservice.dto.ExportMessage;
import com.airesume.exportservice.dto.ExportStatsDTO;
import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.repository.ExportJobRepository;
import com.airesume.exportservice.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    private ExportJobRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ExportServiceImpl exportService;

    private ExportJob job;
    private Path tempFile;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(exportService, "freeLimitDaily", 10);

        job = new ExportJob();
        job.setJobId("job-123");
        job.setUserId(1L);
        job.setStatus(ExportStatus.COMPLETED);
        
        tempFile = Files.createTempFile("test-export", ".pdf");
        Files.write(tempFile, new byte[]{1, 2, 3});
        job.setFileUrl(tempFile.toAbsolutePath().toString());
    }

    @Test
    void testSubmitExportJob_Success() {
        when(currentUserService.isPremium()).thenReturn(false);
        when(repository.countPdfExportsByUserIdSince(any(), any())).thenReturn(5L);
        when(repository.save(any())).thenReturn(job);

        ExportJob result = exportService.submitExportJob(1L, 1L, ExportFormat.PDF, "{}", "Bearer token");

        assertNotNull(result);
        verify(rabbitTemplate).convertAndSend(eq("x.airesume"), eq("pdf.export"), any(ExportMessage.class));
    }

    @Test
    void testSubmitExportJob_FreeLimitReached() {
        when(currentUserService.isPremium()).thenReturn(false);
        when(repository.countPdfExportsByUserIdSince(any(), any())).thenReturn(10L);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exportService.submitExportJob(1L, 1L, ExportFormat.PDF, "{}", "Bearer token"));

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getStatusCode());
    }

    @Test
    void testSubmitExportJob_PremiumRequiredForNonPdf() {
        when(currentUserService.isPremium()).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exportService.submitExportJob(1L, 1L, ExportFormat.DOCX, "{}", "Bearer token"));

        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void testGetJobStatus_Success() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        ExportJob result = exportService.getJobStatus("job-123");
        assertEquals("job-123", result.getJobId());
    }

    @Test
    void testGetJobStatus_NotFound() {
        when(repository.findById("job-123")).thenReturn(Optional.empty());
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exportService.getJobStatus("job-123"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void testGetExportsByUser() {
        when(repository.findByUserId(1L)).thenReturn(List.of(job));
        List<ExportJob> result = exportService.getExportsByUser(1L);
        assertEquals(1, result.size());
    }

    @Test
    void testDownloadFile_Success() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        byte[] bytes = exportService.downloadFile("job-123");
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
    }

    @Test
    void testDownloadFile_NotReady() {
        job.setStatus(ExportStatus.PROCESSING);
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exportService.downloadFile("job-123"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void testDownloadFile_IOException() {
        job.setFileUrl("path/that/does/not/exist.pdf");
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exportService.downloadFile("job-123"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
    }

    @Test
    void testDeleteExport() {
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        exportService.deleteExport("job-123");
        
        assertFalse(new File(job.getFileUrl()).exists());
        verify(repository).delete(job);
    }

    @Test
    void testCleanupExpiredExports() {
        when(repository.findByExpiresAtBefore(any())).thenReturn(List.of(job));
        when(repository.findById("job-123")).thenReturn(Optional.of(job));

        exportService.cleanupExpiredExports();

        verify(repository).delete(job);
    }

    @Test
    void testGetUserStats() {
        when(repository.countByUserId(1L)).thenReturn(20L);
        when(repository.countPdfExportsByUserIdSince(any(), any())).thenReturn(5L);
        
        // Mocking the group by return
        Object[] arr = new Object[]{"PDF", 20L};
        when(repository.countByFormatForUser(1L)).thenReturn(java.util.Collections.singletonList(arr));

        ExportStatsDTO stats = exportService.getUserStats(1L);
        
        assertEquals(20L, stats.getTotalExports());
        assertEquals(5L, stats.getTodayPdfCount());
        assertEquals(20L, stats.getCountByFormat().get("PDF"));
    }

    @Test
    void testGetAdminStats() {
        Object[] arr = new Object[]{"PDF", 100L};
        when(repository.countByFormatGlobal()).thenReturn(java.util.Collections.singletonList(arr));

        Map<String, Long> stats = exportService.getAdminStats();
        
        assertEquals(100L, stats.get("PDF"));
    }

    @Test
    void testSubmitExportJob_PremiumUser() {
        when(currentUserService.isPremium()).thenReturn(true);
        when(repository.save(any())).thenReturn(job);

        ExportJob result = exportService.submitExportJob(1L, 1L, ExportFormat.DOCX, "{}", "Bearer token");

        assertNotNull(result);
        verify(rabbitTemplate).convertAndSend(eq("x.airesume"), eq("pdf.export"), any(ExportMessage.class));
    }

    @Test
    void testSubmitExportJob_PremiumUser_Pdf() {
        when(currentUserService.isPremium()).thenReturn(true);
        when(repository.save(any())).thenReturn(job);

        ExportJob result = exportService.submitExportJob(1L, 1L, ExportFormat.PDF, "{}", "Bearer token");

        assertNotNull(result);
        verify(rabbitTemplate).convertAndSend(eq("x.airesume"), eq("pdf.export"), any(ExportMessage.class));
    }

    @Test
    void testDownloadFile_FileUrlNull() {
        job.setFileUrl(null);
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () ->
                exportService.downloadFile("job-123"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
    }

    @Test
    void testDeleteExport_FileUrlNull() {
        job.setFileUrl(null);
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        exportService.deleteExport("job-123");
        
        verify(repository).delete(job);
    }

    @Test
    void testDeleteExport_IOException() throws Exception {
        // Create a non-empty directory. Attempting to delete this with Files.deleteIfExists will fail and throw DirectoryNotEmptyException (which extends IOException)
        Path tempDir = Files.createTempDirectory("non-empty-dir");
        Files.createTempFile(tempDir, "temp", ".txt");
        job.setFileUrl(tempDir.toAbsolutePath().toString());
        when(repository.findById("job-123")).thenReturn(Optional.of(job));
        
        assertDoesNotThrow(() -> exportService.deleteExport("job-123"));
        verify(repository).delete(job);
    }
}
