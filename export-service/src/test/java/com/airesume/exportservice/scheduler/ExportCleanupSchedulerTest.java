package com.airesume.exportservice.scheduler;

import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.repository.ExportJobRepository;
import com.airesume.exportservice.service.ExportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportCleanupSchedulerTest {

    @Mock
    private ExportService exportService;

    @Mock
    private ExportJobRepository exportJobRepository;

    @InjectMocks
    private ExportCleanupScheduler exportCleanupScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exportCleanupScheduler, "processingTimeoutSeconds", 180L);
    }

    @Test
    void testCleanupExpiredExports() {
        exportCleanupScheduler.cleanupExpiredExports();
        verify(exportService).cleanupExpiredExports();
    }

    @Test
    void testFailStuckJobs_NoStuckJobs() {
        when(exportJobRepository.findByStatusAndRequestedAtBefore(eq(ExportStatus.QUEUED), any())).thenReturn(List.of());
        when(exportJobRepository.findByStatusAndProcessingStartedAtBefore(eq(ExportStatus.PROCESSING), any())).thenReturn(List.of());

        exportCleanupScheduler.failStuckJobs();

        verify(exportJobRepository, never()).saveAll(any());
    }

    @Test
    void testFailStuckJobs_HasStuckJobs() {
        ExportJob queuedJob = new ExportJob();
        queuedJob.setJobId("q-123");
        queuedJob.setStatus(ExportStatus.QUEUED);
        queuedJob.setRequestedAt(LocalDateTime.now().minusSeconds(100));

        ExportJob processingJob = new ExportJob();
        processingJob.setJobId("p-123");
        processingJob.setStatus(ExportStatus.PROCESSING);
        processingJob.setProcessingStartedAt(LocalDateTime.now().minusSeconds(200));

        when(exportJobRepository.findByStatusAndRequestedAtBefore(eq(ExportStatus.QUEUED), any()))
                .thenReturn(List.of(queuedJob));
        when(exportJobRepository.findByStatusAndProcessingStartedAtBefore(eq(ExportStatus.PROCESSING), any()))
                .thenReturn(List.of(processingJob));

        exportCleanupScheduler.failStuckJobs();

        verify(exportJobRepository).saveAll(argThat(jobs -> {
            List<ExportJob> savedJobs = (List<ExportJob>) jobs;
            return savedJobs.size() == 2 &&
                   savedJobs.get(0).getStatus() == ExportStatus.FAILED &&
                   savedJobs.get(1).getStatus() == ExportStatus.FAILED;
        }));
    }
    @Test
    void testFailStuckJobs_HasStuckJobs_WithExistingFailureReasonAndCompletedAt() {
        ExportJob queuedJob = new ExportJob();
        queuedJob.setJobId("q-123");
        queuedJob.setStatus(ExportStatus.QUEUED);
        queuedJob.setRequestedAt(LocalDateTime.now().minusSeconds(100));
        queuedJob.setFailureReason("Pre-existing error");
        queuedJob.setCompletedAt(LocalDateTime.now().minusSeconds(10));

        when(exportJobRepository.findByStatusAndRequestedAtBefore(eq(ExportStatus.QUEUED), any()))
                .thenReturn(List.of(queuedJob));
        when(exportJobRepository.findByStatusAndProcessingStartedAtBefore(eq(ExportStatus.PROCESSING), any()))
                .thenReturn(List.of());

        exportCleanupScheduler.failStuckJobs();

        verify(exportJobRepository).saveAll(argThat(jobs -> {
            List<ExportJob> savedJobs = (List<ExportJob>) jobs;
            return savedJobs.size() == 1 &&
                   savedJobs.get(0).getStatus() == ExportStatus.FAILED &&
                   "Pre-existing error".equals(savedJobs.get(0).getFailureReason()) &&
                   savedJobs.get(0).getCompletedAt() != null;
        }));
    }
    @Test
    void testFailStuckJobs_HasStuckJobs_WithBlankFailureReason() {
        ExportJob queuedJob = new ExportJob();
        queuedJob.setJobId("q-123");
        queuedJob.setStatus(ExportStatus.QUEUED);
        queuedJob.setRequestedAt(LocalDateTime.now().minusSeconds(100));
        queuedJob.setFailureReason("   "); // blank string

        when(exportJobRepository.findByStatusAndRequestedAtBefore(eq(ExportStatus.QUEUED), any()))
                .thenReturn(List.of(queuedJob));
        when(exportJobRepository.findByStatusAndProcessingStartedAtBefore(eq(ExportStatus.PROCESSING), any()))
                .thenReturn(List.of());

        exportCleanupScheduler.failStuckJobs();

        verify(exportJobRepository).saveAll(argThat(jobs -> {
            List<ExportJob> savedJobs = (List<ExportJob>) jobs;
            return savedJobs.size() == 1 &&
                   savedJobs.get(0).getStatus() == ExportStatus.FAILED &&
                   savedJobs.get(0).getFailureReason().contains("timed out");
        }));
    }
}
