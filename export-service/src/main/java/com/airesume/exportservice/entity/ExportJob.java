package com.airesume.exportservice.entity;

import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "export_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExportJob {

    @Id
    private String jobId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long resumeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportFormat format;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportStatus status;

    private String fileUrl;

    private Long fileSizeKb;

    private LocalDateTime requestedAt;

    /**
     * Set when the @Async thread transitions the job to PROCESSING.
     * The watchdog uses this (not requestedAt) to detect truly stuck jobs,
     * so short queue delays don't count toward the generation timeout.
     */
    private LocalDateTime processingStartedAt;

    private LocalDateTime completedAt;

    private LocalDateTime expiresAt;

    private Long templateId;

    @Column(columnDefinition = "TEXT")
    private String customizations;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @PrePersist
    protected void onCreate() {
        if (jobId == null) {
            jobId = UUID.randomUUID().toString();
        }
        requestedAt = LocalDateTime.now();
        if (status == null) {
            status = ExportStatus.QUEUED;
        }
    }
}
