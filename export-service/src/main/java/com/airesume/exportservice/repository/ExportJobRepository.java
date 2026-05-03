package com.airesume.exportservice.repository;

import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExportJobRepository extends JpaRepository<ExportJob, String> {
    List<ExportJob> findByUserId(Long userId);
    List<ExportJob> findByResumeId(Long resumeId);
    List<ExportJob> findByStatus(ExportStatus status);
    List<ExportJob> findByStatusAndRequestedAtBefore(ExportStatus status, LocalDateTime cutoff);
    List<ExportJob> findByStatusAndProcessingStartedAtBefore(ExportStatus status, LocalDateTime cutoff);
    
    @Query("SELECT COUNT(e) FROM ExportJob e WHERE e.userId = :userId AND e.format = com.airesume.exportservice.model.ExportFormat.PDF AND e.requestedAt >= :start")
    long countPdfExportsByUserIdSince(Long userId, LocalDateTime start);

    long countByUserId(Long userId);
    
    @Query("SELECT e.format, COUNT(e) FROM ExportJob e GROUP BY e.format")
    List<Object[]> countByFormatGlobal();

    @Query("SELECT e.format, COUNT(e) FROM ExportJob e WHERE e.userId = :userId GROUP BY e.format")
    List<Object[]> countByFormatForUser(Long userId);

    List<ExportJob> findByExpiresAtBefore(LocalDateTime now);
}
