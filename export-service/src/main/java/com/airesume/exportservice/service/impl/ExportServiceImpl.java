package com.airesume.exportservice.service.impl;

import com.airesume.exportservice.dto.ExportStatsDTO;
import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.repository.ExportJobRepository;
import com.airesume.exportservice.security.CurrentUserService;
import com.airesume.exportservice.service.ExportService;
import com.airesume.exportservice.dto.ExportMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ExportService that handles document generation requests.
 * Orchestrates asynchronous processing via RabbitMQ and manages file storage.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportServiceImpl implements ExportService {

    private final ExportJobRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final CurrentUserService currentUserService;

    @Value("${app.export.storage-path:./exports}")
    private String storagePath;

    @Value("${app.export.pdf.free-limit-daily:10}")
    private int freeLimitDaily;

    /**
     * Submits a new export job (PDF/DOCX).
     * 1. Validates user quota (Free vs Premium).
     * 2. Persists a QUEUED job record.
     * 3. Publishes a message to RabbitMQ for async processing.
     */
    @Override
    public ExportJob submitExportJob(Long userId, Long resumeId, ExportFormat format, String customizations, String authorizationHeader) {
        // 1. Validate Tier & Quota
        validateQuota(userId, format);

        // 2. Create Job Record
        ExportJob job = ExportJob.builder()
                .userId(userId)
                .resumeId(resumeId)
                .format(format)
                .status(ExportStatus.QUEUED)
                .customizations(customizations)
                .failureReason(null)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        job = repository.save(job);

        // 3. Process Async via RabbitMQ Queue
        log.info("[RABBITMQ] Publishing export task for jobId: {} to queue.", job.getJobId());
        try {
            ExportMessage message = ExportMessage.builder()
                    .jobId(job.getJobId())
                    .authorizationHeader(authorizationHeader)
                    .build();
            rabbitTemplate.convertAndSend("x.airesume", "pdf.export", message);
            log.info("[RABBITMQ] Successfully published export task for jobId: {}", job.getJobId());
        } catch (Exception e) {
            log.error("[RABBITMQ] Failed to publish export task to queue for jobId: {}. Error: {}", 
                    job.getJobId(), e.getMessage(), e);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Failed to queue export task: " + e.getMessage());
        }

        return job;
    }

    /**
     * Retrieves the current status of an export job (QUEUED, PROCESSING, COMPLETED, FAILED).
     */
    @Override
    public ExportJob getJobStatus(String jobId) {
        return repository.findById(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }

    @Override
    public List<ExportJob> getExportsByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public byte[] downloadFile(String jobId) {
        ExportJob job = getJobStatus(jobId);
        if (job.getStatus() != ExportStatus.COMPLETED || job.getFileUrl() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is not ready or failed");
        }

        try {
            return Files.readAllBytes(Paths.get(job.getFileUrl()));
        } catch (IOException e) {
            log.error("Failed to read file for job {}: {}", jobId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file");
        }
    }

    @Override
    public void deleteExport(String jobId) {
        ExportJob job = getJobStatus(jobId);
        if (job.getFileUrl() != null) {
            try {
                Files.deleteIfExists(Paths.get(job.getFileUrl()));
            } catch (IOException e) {
                log.warn("Failed to delete file for job {}: {}", jobId, e.getMessage());
            }
        }
        repository.delete(job);
    }

    /**
     * Periodically cleans up files and database records for expired export jobs.
     */
    @Override
    public void cleanupExpiredExports() {
        List<ExportJob> expired = repository.findByExpiresAtBefore(LocalDateTime.now());
        for (ExportJob job : expired) {
            deleteExport(job.getJobId());
        }
        log.info("Cleaned up {} expired export jobs", expired.size());
    }

    @Override
    public ExportStatsDTO getUserStats(Long userId) {
        long total = repository.countByUserId(userId);
        long todayPdf = getDailyPdfCount(userId);
        
        Map<String, Long> formatStats = repository.countByFormatForUser(userId).stream()
                .collect(Collectors.toMap(
                        arr -> String.valueOf(arr[0]),
                        arr -> (Long) arr[1]
                ));

        return ExportStatsDTO.builder()
                .userId(userId)
                .totalExports(total)
                .todayPdfCount(todayPdf)
                .countByFormat(formatStats)
                .build();
    }

    @Override
    public Map<String, Long> getAdminStats() {
        return repository.countByFormatGlobal().stream()
                .collect(Collectors.toMap(
                        arr -> String.valueOf(arr[0]),
                        arr -> (Long) arr[1]
                ));
    }

    @Override
    public long getDailyPdfCount(Long userId) {
        return repository.countPdfExportsByUserIdSince(userId, LocalDate.now().atStartOfDay());
    }

    /**
     * Enforces usage limits based on user tier.
     * Free users: PDF only, daily limit applies.
     * Premium users: Unlimited access to all formats.
     */
    private void validateQuota(Long userId, ExportFormat format) {
        boolean isPremium = currentUserService.isPremium();

        if (!isPremium && format != ExportFormat.PDF) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Premium plan required for " + format + " export.");
        }

        if (format == ExportFormat.PDF && !isPremium) {
            long count = repository.countPdfExportsByUserIdSince(userId, LocalDate.now().atStartOfDay());
            if (count >= freeLimitDaily) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily PDF export limit reached for Free plan.");
            }
        }
    }
}
