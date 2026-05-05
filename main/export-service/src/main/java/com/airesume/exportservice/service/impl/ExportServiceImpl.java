package com.airesume.exportservice.service.impl;

import com.airesume.exportservice.dto.ExportStatsDTO;
import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.repository.ExportJobRepository;
import com.airesume.exportservice.security.CurrentUserService;
import com.airesume.exportservice.service.ExportService;
import com.airesume.exportservice.service.ExportJobProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportServiceImpl implements ExportService {

    private final ExportJobRepository repository;
    private final ExportJobProcessor exportJobProcessor;
    private final CurrentUserService currentUserService;

    @Value("${app.export.storage-path:./exports}")
    private String storagePath;

    @Value("${app.export.pdf.free-limit-daily:10}")
    private int freeLimitDaily;

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

        // 3. Process Async
        exportJobProcessor.processJob(job.getJobId(), authorizationHeader);

        return job;
    }

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

    private void validateQuota(Long userId, ExportFormat format) {
        boolean isPremium = currentUserService.isPremium();

        if (format == ExportFormat.PDF && !isPremium) {
            long count = repository.countPdfExportsByUserIdSince(userId, LocalDate.now().atStartOfDay());
            if (count >= freeLimitDaily) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily PDF export limit reached for Free plan.");
            }
        }
    }
}
