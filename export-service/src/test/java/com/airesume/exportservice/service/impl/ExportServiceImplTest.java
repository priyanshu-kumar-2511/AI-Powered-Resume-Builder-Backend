package com.airesume.exportservice.service.impl;

import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.repository.ExportJobRepository;
import com.airesume.exportservice.security.CurrentUserService;
import com.airesume.exportservice.service.ExportJobProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    @Mock
    private ExportJobRepository repository;

    @Mock
    private ExportJobProcessor exportJobProcessor;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ExportServiceImpl exportService;

    private final Long userId = 1L;
    private final Long resumeId = 101L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exportService, "freeLimitDaily", 10);
        ReflectionTestUtils.setField(exportService, "storagePath", "./exports");
    }

    @Test
    void submitExportJob_Success_FreeUserWithinLimit() {
        when(currentUserService.isPremium()).thenReturn(false);
        when(repository.countPdfExportsByUserIdSince(any(), any())).thenReturn(5L);

        ExportJob mockJob = ExportJob.builder()
                .jobId("test-uuid")
                .userId(userId)
                .resumeId(resumeId)
                .format(ExportFormat.PDF)
                .status(ExportStatus.QUEUED)
                .build();

        when(repository.save(any(ExportJob.class))).thenReturn(mockJob);

        ExportJob result = exportService.submitExportJob(userId, resumeId, ExportFormat.PDF, null, "Bearer token");

        assertNotNull(result);
        assertEquals(ExportFormat.PDF, result.getFormat());
        verify(repository, times(1)).countPdfExportsByUserIdSince(any(), any());
        verify(exportJobProcessor).processJob("test-uuid", "Bearer token");
    }

    @Test
    void submitExportJob_Fails_FreeUserExceedsLimit() {
        when(currentUserService.isPremium()).thenReturn(false);
        when(repository.countPdfExportsByUserIdSince(any(), any())).thenReturn(10L);

        assertThrows(ResponseStatusException.class, () ->
                exportService.submitExportJob(userId, resumeId, ExportFormat.PDF, null, "Bearer token")
        );
    }

    @Test
    void submitExportJob_Success_PremiumUserBypassLimit() {
        when(currentUserService.isPremium()).thenReturn(true);

        ExportJob mockJob = ExportJob.builder()
                .jobId("premium-job")
                .userId(userId)
                .resumeId(resumeId)
                .format(ExportFormat.DOCX)
                .status(ExportStatus.QUEUED)
                .build();

        when(repository.save(any(ExportJob.class))).thenReturn(mockJob);

        ExportJob result = exportService.submitExportJob(userId, resumeId, ExportFormat.DOCX, null, "Bearer token");

        assertNotNull(result);
        verify(repository, never()).countPdfExportsByUserIdSince(any(), any());
        verify(exportJobProcessor).processJob("premium-job", "Bearer token");
    }

    @Test
    void submitExportJob_Fails_FreeUserRequestDocx() {
        when(currentUserService.isPremium()).thenReturn(false);

        assertThrows(ResponseStatusException.class, () ->
                exportService.submitExportJob(userId, resumeId, ExportFormat.DOCX, null, "Bearer token")
        );
    }

    @Test
    void getJobStatus_Success() {
        String jobId = "test-job";
        ExportJob mockJob = ExportJob.builder().jobId(jobId).status(ExportStatus.COMPLETED).build();
        when(repository.findById(jobId)).thenReturn(Optional.of(mockJob));

        ExportJob result = exportService.getJobStatus(jobId);

        assertEquals(ExportStatus.COMPLETED, result.getStatus());
    }

    @Test
    void getJobStatus_NotFound() {
        when(repository.findById("invalid")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> exportService.getJobStatus("invalid"));
    }
}
