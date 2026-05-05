package com.airesume.exportservice.scheduler;

import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.repository.ExportJobRepository;
import com.airesume.exportservice.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExportCleanupScheduler {

    private final ExportService exportService;
    private final ExportJobRepository exportJobRepository;

    /**
     * How many seconds a PROCESSING job is allowed to run before the watchdog
     * declares it stuck. Default 180s (3 min) to accommodate DOCX generation.
     *
     * NOTE: This timeout starts from processingStartedAt (when the @Async thread
     * begins work), NOT from requestedAt (when the job was first created).
     * This means queue wait time is excluded from the countdown.
     */
    @Value("${app.export.job.processing-timeout-seconds:180}")
    private long processingTimeoutSeconds;

    /**
     * How many seconds a QUEUED job may sit without being picked up before it is
     * considered stuck. A job should be picked up within a few seconds of creation.
     */
    private static final long QUEUED_TIMEOUT_SECONDS = 60;

    // Runs every day at midnight to delete expired file records
    @Scheduled(cron = "0 0 0 * * *")
    public void cleanupExpiredExports() {
        log.info("[WATCHDOG] Starting scheduled cleanup of expired export files...");
        exportService.cleanupExpiredExports();
        log.info("[WATCHDOG] Finished scheduled cleanup.");
    }

    /**
     * Runs every 15 seconds. Marks genuinely stuck jobs as FAILED.
     *
     * A job is stuck if:
     *  - Status = QUEUED  and requestedAt      > QUEUED_TIMEOUT_SECONDS ago   (never picked up)
     *  - Status = PROCESSING and processingStartedAt > processingTimeoutSeconds ago (running too long)
     */
    @Scheduled(fixedDelayString = "${app.export.job.watchdog-interval-ms:15000}")
    public void failStuckJobs() {
        List<ExportJob> stuckJobs = new ArrayList<>();

        // --- Jobs stuck in QUEUED (never picked up by @Async thread) ---
        LocalDateTime queuedCutoff = LocalDateTime.now().minusSeconds(QUEUED_TIMEOUT_SECONDS);
        List<ExportJob> stuckQueued =
                exportJobRepository.findByStatusAndRequestedAtBefore(ExportStatus.QUEUED, queuedCutoff);
        stuckJobs.addAll(stuckQueued);

        // --- Jobs stuck in PROCESSING (generation is taking too long) ---
        // FIX: use processingStartedAt, NOT requestedAt.
        // processingStartedAt is set the moment the @Async thread transitions the job
        // to PROCESSING. Jobs where processingStartedAt is null haven't truly started
        // yet (race condition between save and @Async pickup) — skip those.
        LocalDateTime processingCutoff = LocalDateTime.now().minusSeconds(processingTimeoutSeconds);
        List<ExportJob> stuckProcessing =
                exportJobRepository.findByStatusAndProcessingStartedAtBefore(ExportStatus.PROCESSING, processingCutoff);
        stuckJobs.addAll(stuckProcessing);

        if (stuckJobs.isEmpty()) {
            return;
        }

        log.warn("[WATCHDOG] Marking {} stuck export job(s) as FAILED (queueTimeout={}s, processingTimeout={}s).",
                stuckJobs.size(), QUEUED_TIMEOUT_SECONDS, processingTimeoutSeconds);

        for (ExportJob job : stuckJobs) {
            log.warn("[WATCHDOG] Failing job={} format={} status={} requestedAt={} processingStartedAt={}",
                    job.getJobId(), job.getFormat(), job.getStatus(),
                    job.getRequestedAt(), job.getProcessingStartedAt());
            job.setStatus(ExportStatus.FAILED);
            if (job.getFailureReason() == null || job.getFailureReason().isBlank()) {
                job.setFailureReason("Export timed out while generating the file. Please try again.");
            }
            if (job.getCompletedAt() == null) {
                job.setCompletedAt(LocalDateTime.now());
            }
        }

        exportJobRepository.saveAll(stuckJobs);
    }
}
