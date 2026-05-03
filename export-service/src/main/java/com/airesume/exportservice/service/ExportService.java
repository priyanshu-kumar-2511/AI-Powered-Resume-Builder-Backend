package com.airesume.exportservice.service;

import com.airesume.exportservice.dto.ExportStatsDTO;
import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import java.util.List;
import java.util.Map;

public interface ExportService {
    ExportJob submitExportJob(Long userId, Long resumeId, ExportFormat format, String customizations, String authorizationHeader);
    ExportJob getJobStatus(String jobId);
    List<ExportJob> getExportsByUser(Long userId);
    byte[] downloadFile(String jobId);
    void deleteExport(String jobId);
    void cleanupExpiredExports();
    
    // Stats & Admin
    ExportStatsDTO getUserStats(Long userId);
    Map<String, Long> getAdminStats();
    long getDailyPdfCount(Long userId);
}
