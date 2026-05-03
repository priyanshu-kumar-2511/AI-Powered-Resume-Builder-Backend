package com.airesume.exportservice.service;

import com.airesume.exportservice.client.ResumeServiceClient;
import com.airesume.exportservice.client.SectionServiceClient;
import com.airesume.exportservice.client.TemplateServiceClient;
import com.airesume.exportservice.config.ExportAuthContext;
import com.airesume.exportservice.dto.ResumeResponseDTO;
import com.airesume.exportservice.dto.SectionDTO;
import com.airesume.exportservice.dto.TemplateDTO;
import com.airesume.exportservice.entity.ExportJob;
import com.airesume.exportservice.model.ExportFormat;
import com.airesume.exportservice.model.ExportStatus;
import com.airesume.exportservice.repository.ExportJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportJobProcessor {

    private final ExportJobRepository repository;
    private final ResumeServiceClient resumeClient;
    private final TemplateServiceClient templateClient;
    private final SectionServiceClient sectionClient;
    private final PdfGeneratorService pdfGenerator;

    @Value("${app.export.storage-path:./exports}")
    private String storagePath;

    @Async
    public void processJob(String jobId, String authorizationHeader) {
        ExportJob job = repository.findById(jobId).orElse(null);
        if (job == null) {
            log.error("[EXPORT] Job {} disappeared before processing started.", jobId);
            return;
        }

        log.info("[EXPORT] ▶ Starting job={} format={} resumeId={} threadName={}",
                job.getJobId(), job.getFormat(), job.getResumeId(), Thread.currentThread().getName());
        log.info("[EXPORT] JWT header present: {}", authorizationHeader != null && !authorizationHeader.isBlank());

        try {
            ExportAuthContext.setAuthorization(authorizationHeader);

            job.setStatus(ExportStatus.PROCESSING);
            job.setProcessingStartedAt(LocalDateTime.now());
            job.setFailureReason(null);
            repository.save(job);
            log.info("[EXPORT] Job {} set to PROCESSING", jobId);

            // ── Step 1: Fetch Resume via Feign ──────────────────────────────────
            log.info("[EXPORT] Fetching resume resumeId={} from RESUME-SERVICE ...", job.getResumeId());
            ResumeResponseDTO resume;
            try {
                resume = resumeClient.getResumeById(job.getResumeId());
                log.info("[EXPORT] ✓ Fetched resume: templateId={} title={}", resume.getTemplateId(), resume.getTitle());
            } catch (Exception feignEx) {
                log.error("[EXPORT] ✗ Feign call to RESUME-SERVICE failed: {}", feignEx.getMessage(), feignEx);
                throw feignEx;
            }

            // ── Step 2: Fetch Sections via Feign ────────────────────────────────
            log.info("[EXPORT] Fetching sections for resumeId={} from SECTION-SERVICE ...", job.getResumeId());
            List<SectionDTO> sections;
            try {
                sections = sectionClient.getSectionsByResume(job.getResumeId());
                log.info("[EXPORT] ✓ Fetched {} sections", sections.size());
            } catch (Exception feignEx) {
                log.error("[EXPORT] ✗ Feign call to SECTION-SERVICE failed: {}", feignEx.getMessage(), feignEx);
                throw feignEx;
            }
            resume.setSections(sections);

            // ── Step 3: Generate File ────────────────────────────────────────────
            TemplateDTO template = null;
            byte[] content;
            if (job.getFormat() == ExportFormat.PDF) {
                log.info("[EXPORT] Fetching template templateId={} from TEMPLATE-SERVICE ...", resume.getTemplateId());
                try {
                    template = templateClient.getTemplateById(resume.getTemplateId());
                    log.info("[EXPORT] ✓ Fetched template: {}", template != null ? template.getTemplateId() : "null");
                } catch (Exception feignEx) {
                    log.error("[EXPORT] ✗ Feign call to TEMPLATE-SERVICE failed: {}", feignEx.getMessage(), feignEx);
                    throw feignEx;
                }
                log.info("[EXPORT] Generating PDF ...");
                content = pdfGenerator.generatePdf(resume, template, job.getCustomizations());
                log.info("[EXPORT] ✓ PDF generated: {} bytes", content.length);
            } else {
                throw new UnsupportedOperationException("Format " + job.getFormat() + " is not supported.");
            }

            // ── Step 4: Save File ────────────────────────────────────────────────
            String fileName = job.getJobId() + "." + job.getFormat().name().toLowerCase();
            Path filePath = Paths.get(storagePath, fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, content);
            log.info("[EXPORT] ✓ File saved to: {}", filePath.toAbsolutePath());

            job.setStatus(ExportStatus.COMPLETED);
            job.setFailureReason(null);
            job.setFileUrl(filePath.toAbsolutePath().toString());
            job.setFileSizeKb((long) Math.max(1, Math.ceil(content.length / 1024.0)));
            job.setCompletedAt(LocalDateTime.now());
            job.setTemplateId(template != null ? template.getTemplateId() : resume.getTemplateId());
            repository.save(job);

            log.info("[EXPORT] ✅ Completed export job: {}", job.getJobId());
        } catch (Exception e) {
            log.error("[EXPORT] ❌ FAILED export job {}: {} — {}", job.getJobId(), e.getClass().getSimpleName(), e.getMessage(), e);
            job.setStatus(ExportStatus.FAILED);
            job.setFailureReason(buildFailureReason(e));
            job.setCompletedAt(LocalDateTime.now());
            repository.save(job);
        } finally {
            ExportAuthContext.clear();
        }
    }

    private String buildFailureReason(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            return "Export job failed unexpectedly while preparing the file.";
        }
        return message.length() > 500 ? message.substring(0, 500) : message;
    }
}
