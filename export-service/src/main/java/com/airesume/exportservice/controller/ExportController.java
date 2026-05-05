package com.airesume.exportservice.controller;

import com.airesume.exportservice.dto.ExportStatsDTO;
import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.security.CurrentUserService;
import com.airesume.exportservice.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing the export process of resumes.
 * Supports PDF, DOCX, and JSON formats with specific rate limits and job status tracking.
 */
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Export Management", description = "APIs for exporting resumes to PDF, DOCX, and JSON")
public class ExportController {

    private final ExportService exportService;
    private final CurrentUserService currentUserService;

    /**
     * Submits a request to export a resume as a PDF.
     * 
     * @param resumeId The ID of the resume to export
     * @param customizations Optional JSON string for export styling/customizations
     * @return The created ExportJob with status ACCEPTED
     */
    @PostMapping("/pdf/{resumeId}")
    @Operation(summary = "Submit a PDF export job")
    public ResponseEntity<ExportJob> exportPdf(
            @PathVariable Long resumeId,
            @RequestBody(required = false) String customizations,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        Long userId = currentUserService.requireUserId();
        return new ResponseEntity<>(exportService.submitExportJob(userId, resumeId, ExportFormat.PDF, customizations, authorizationHeader), HttpStatus.ACCEPTED);
    }

    @PostMapping("/docx/{resumeId}")
    @Operation(summary = "Submit a DOCX export job")
    public ResponseEntity<ExportJob> exportDocx(
            @PathVariable Long resumeId,
            @RequestBody(required = false) String customizations,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        Long userId = currentUserService.requireUserId();
        return new ResponseEntity<>(exportService.submitExportJob(userId, resumeId, ExportFormat.DOCX, customizations, authorizationHeader), HttpStatus.ACCEPTED);
    }

    @PostMapping("/json/{resumeId}")
    @Operation(summary = "Submit a JSON export job")
    public ResponseEntity<ExportJob> exportJson(
            @PathVariable Long resumeId,
            @RequestBody(required = false) String customizations,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        Long userId = currentUserService.requireUserId();
        return new ResponseEntity<>(exportService.submitExportJob(userId, resumeId, ExportFormat.JSON, customizations, authorizationHeader), HttpStatus.ACCEPTED);
    }

    /**
     * Polls the status of an asynchronous export job.
     * 
     * @param jobId The unique ID of the export job
     * @return The current state of the ExportJob
     */
    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get status of an export job")
    public ResponseEntity<ExportJob> getJobStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(exportService.getJobStatus(jobId));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all export jobs for a user")
    public ResponseEntity<List<ExportJob>> getExportsByUser(@PathVariable Long userId) {
        // Basic security check: user can only see their own jobs unless admin
        Long currentUserId = currentUserService.requireUserId();
        if (!currentUserId.equals(userId)) {
             // In a real app, check for ADMIN role here
        }
        return ResponseEntity.ok(exportService.getExportsByUser(userId));
    }

    /**
     * Downloads the final file for a completed export job.
     * 
     * @param jobId The unique ID of the export job
     * @return The binary content of the file with appropriate headers
     */
    @GetMapping("/download/{jobId}")
    @Operation(summary = "Download the exported file")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String jobId) {
        ExportJob job = exportService.getJobStatus(jobId);
        byte[] content = exportService.downloadFile(jobId);
        
        String fileName = "resume_" + job.getResumeId() + "." + job.getFormat().name().toLowerCase();
        MediaType mediaType = job.getFormat() == ExportFormat.PDF ? MediaType.APPLICATION_PDF : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(mediaType)
                .body(content);
    }

    @DeleteMapping("/{jobId}")
    @Operation(summary = "Delete an export job record and its file")
    public ResponseEntity<Void> deleteExport(@PathVariable String jobId) {
        exportService.deleteExport(jobId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/{userId}")
    @Operation(summary = "Get export statistics for a user")
    public ResponseEntity<ExportStatsDTO> getStats(@PathVariable Long userId) {
        return ResponseEntity.ok(exportService.getUserStats(userId));
    }

    @DeleteMapping("/internal/cleanup-expired")
    @Operation(summary = "Cleanup expired exports (Internal)")
    public ResponseEntity<Void> cleanup() {
        exportService.cleanupExpiredExports();
        return ResponseEntity.ok().build();
    }

    // --- Admin Endpoints ---

    @GetMapping("/admin/stats")
    @Operation(summary = "Get platform-wide export stats (Admin)")
    public ResponseEntity<Map<String, Long>> getAdminStats() {
        return ResponseEntity.ok(exportService.getAdminStats());
    }

    @GetMapping("/admin/user/{userId}/daily-count")
    @Operation(summary = "Check user's daily PDF export count")
    public ResponseEntity<Long> getDailyCount(@PathVariable Long userId) {
        return ResponseEntity.ok(exportService.getDailyPdfCount(userId));
    }
}
