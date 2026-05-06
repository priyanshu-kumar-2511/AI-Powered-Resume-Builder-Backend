package com.airesume.resumeservice.service;

import com.airesume.resumeservice.client.SectionServiceClient;
import com.airesume.resumeservice.client.dto.SectionPayload;
import com.airesume.resumeservice.dto.AtsUpdateDTO;
import com.airesume.resumeservice.dto.ResumeCreateRequest;
import com.airesume.resumeservice.dto.ResumeResponse;
import com.airesume.resumeservice.dto.ResumeUpdateRequest;
import com.airesume.resumeservice.model.Resume;
import com.airesume.resumeservice.repository.ResumeRepository;
import com.airesume.resumeservice.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Core implementation of the ResumeService.
 * Handles the business logic for creating, fetching, and updating resumes.
 * Uses caching (@Cacheable, @CacheEvict) to optimize performance and interacts
 * with the SectionService via Feign Client for modular content management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private static final long FREE_RESUME_LIMIT = 3L;

    private final ResumeRepository resumeRepository;
    private final SectionServiceClient sectionServiceClient;
    private final CurrentUserService currentUserService;

    /**
     * Creates a new resume and initializes its default sections (Summary, Experience, etc.)
     * via an inter-service call to Section Service.
     */

    @Override
    @CacheEvict(value = { "resumes_user", "public_resumes" }, allEntries = true)
    public ResumeResponse createResume(ResumeCreateRequest request) {
        Long currentUserId = currentUserService.requireUserId();
        request.setUserId(currentUserId);

        long currentCount = resumeRepository.countByUserId(currentUserId);
        enforceFreePlanLimit(currentCount);

        Resume resume = Resume.builder()
                .userId(currentUserId)
                .title(request.getTitle())
                .templateId(request.getTemplateId())
                .targetJobTitle(request.getTargetJobTitle())
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
                .status("DRAFT")
                .isPublic(false)
                .build();

        resume = resumeRepository.save(resume);

        // Call synchronously. Since we removed @Transactional from this method,
        // the resume is already committed to the DB. This prevents a deadlock where
        // section-service calls back to verify ownership but the resume isn't saved
        // yet.
        try {
            // Initializes Personal Info, Experience, Education, etc. via SectionService
            initializeDefaultSections(resume.getResumeId());
        } catch (Exception e) {
            log.error("Failed to initialize default sections for resume: {}. Cleaning up.", resume.getResumeId(), e);
            resumeRepository.deleteById(resume.getResumeId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to initialize sections");
        }

        log.info("Created new resume with ID {} for user {}", resume.getResumeId(), currentUserId);
        return new ResumeResponse(resume);
    }

    @Override
    @Cacheable(value = "resume", key = "#resumeId")
    public ResumeResponse getResumeById(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resume not found with ID: " + resumeId));

        if (resume.isPublic() || currentUserService.isAdmin()) {
            return new ResumeResponse(resume);
        }

        Long currentUserId = currentUserService.requireUserId();
        if (!resume.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resume.");
        }

        return new ResumeResponse(resume);
    }

    @Override
    @Cacheable(value = "resumes_user", key = "#userId")
    public List<ResumeResponse> getResumesByUser(Long userId) {
        validateUserAccess(userId);
        return resumeRepository.findByUserId(userId).stream()
                .map(ResumeResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "resumes_template", key = "#templateId")
    public List<ResumeResponse> getResumesByTemplate(Long templateId) {
        currentUserService.requireAdmin();
        return resumeRepository.findByTemplateId(templateId).stream()
                .map(ResumeResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "public_resumes", key = "'all'")
    public List<ResumeResponse> getPublicResumes() {
        return resumeRepository.findByIsPublic(true).stream()
                .map(ResumeResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = { "resume", "resumes_user", "resumes_template", "public_resumes" }, allEntries = true)
    public ResumeResponse updateResume(Long resumeId, ResumeUpdateRequest request) {
        Resume resume = requireOwnedOrAdminResume(resumeId);

        if (request.getTitle() != null) {
            resume.setTitle(request.getTitle());
        }
        if (request.getTargetJobTitle() != null) {
            resume.setTargetJobTitle(request.getTargetJobTitle());
        }
        if (request.getLanguage() != null) {
            resume.setLanguage(request.getLanguage());
        }
        if (request.getStatus() != null) {
            resume.setStatus(request.getStatus());
        }

        log.info("Updated resume with ID {}", resumeId);
        return new ResumeResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    @CacheEvict(value = { "resume", "resumes_user", "resumes_template", "public_resumes" }, allEntries = true)
    public ResumeResponse updateAtsScore(Long resumeId, AtsUpdateDTO request) {
        Resume resume = requireOwnedOrAdminResume(resumeId);
        resume.setAtsScore(request.getAtsScore());
        log.info("Updated ATS score for resume {} to {}", resumeId, request.getAtsScore());
        return new ResumeResponse(resumeRepository.save(resume));
    }

    @Override
    @CacheEvict(value = { "resumes_user" }, allEntries = true)
    public ResumeResponse duplicateResume(Long resumeId) {
        Resume original = requireOwnedOrAdminResume(resumeId);
        long currentCount = resumeRepository.countByUserId(original.getUserId());
        enforceFreePlanLimit(currentCount);

        Resume duplicate = Resume.builder()
                .userId(original.getUserId())
                .title(original.getTitle() + " (Copy)")
                .targetJobTitle(original.getTargetJobTitle())
                .templateId(original.getTemplateId())
                .language(original.getLanguage())
                .status("DRAFT")
                .isPublic(false)
                .build();

        duplicate = resumeRepository.save(duplicate);

        try {
            duplicateSections(resumeId, duplicate.getResumeId());
        } catch (Exception e) {
            log.error("Failed to duplicate sections for resume: {}. Cleaning up.", duplicate.getResumeId(), e);
            resumeRepository.deleteById(duplicate.getResumeId());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to duplicate sections");
        }

        log.info("Duplicated resume {} into new resume {}", resumeId, duplicate.getResumeId());
        return new ResumeResponse(duplicate);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "resume", "resumes_user", "public_resumes" }, allEntries = true)
    public ResumeResponse publishResume(Long resumeId) {
        Resume resume = requireOwnedOrAdminResume(resumeId);
        resume.setPublic(true);
        resume.setStatus("COMPLETE");
        log.info("Published resume {}", resumeId);
        return new ResumeResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    @CacheEvict(value = { "resume", "resumes_user", "public_resumes" }, allEntries = true)
    public ResumeResponse unpublishResume(Long resumeId) {
        Resume resume = requireOwnedOrAdminResume(resumeId);
        resume.setPublic(false);
        log.info("Unpublished resume {}", resumeId);
        return new ResumeResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    @CacheEvict(value = { "resume", "resumes_user", "public_resumes" }, allEntries = true)
    public void incrementViewCount(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resume not found with ID: " + resumeId));
        if (!resume.isPublic()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found with ID: " + resumeId);
        }
        resume.setViewCount(resume.getViewCount() + 1);
        resumeRepository.save(resume);
    }

    @Override
    @Transactional
    @CacheEvict(value = { "resume", "resumes_user", "resumes_template", "public_resumes" }, allEntries = true)
    public void deleteResume(Long resumeId) {
        Resume resume = requireOwnedOrAdminResume(resumeId);
        sectionServiceClient.deleteAllSectionsByResume(resumeId);
        resumeRepository.deleteById(resume.getResumeId());
        log.info("Deleted resume {}", resumeId);
    }

    @Override
    public List<ResumeResponse> getAllResumes() {
        currentUserService.requireAdmin();
        return resumeRepository.findAll().stream()
                .map(ResumeResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = { "resume", "resumes_user", "resumes_template", "public_resumes" }, allEntries = true)
    public void forceDeleteResume(Long resumeId) {
        currentUserService.requireAdmin();
        if (!resumeRepository.existsById(resumeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found with ID: " + resumeId);
        }
        sectionServiceClient.deleteAllSectionsByResume(resumeId);
        resumeRepository.deleteById(resumeId);
        log.info("Admin forcefully deleted resume {}", resumeId);
    }

    @Override
    public Long countUserResumes(Long userId) {
        validateUserAccess(userId);
        return resumeRepository.countByUserId(userId);
    }

    /**
     * Enforces the 3-resume limit for Free users. 
     * Premium users have unlimited access.
     */
    private void enforceFreePlanLimit(long currentCount) {
        if (!currentUserService.isPremium() && currentCount >= FREE_RESUME_LIMIT) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Free plan users can keep up to 3 resumes. Upgrade to create more.");
        }
    }

    private Resume requireOwnedOrAdminResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Resume not found with ID: " + resumeId));

        if (currentUserService.isAdmin()) {
            return resume;
        }

        Long currentUserId = currentUserService.requireUserId();
        if (!resume.getUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resume.");
        }

        return resume;
    }

    private void validateUserAccess(Long userId) {
        if (currentUserService.isAdmin()) {
            return;
        }

        Long currentUserId = currentUserService.requireUserId();
        if (!currentUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this user's resumes.");
        }
    }

    private void duplicateSections(Long sourceResumeId, Long targetResumeId) {
        List<SectionPayload> sections = sectionServiceClient.getSectionsByResume(sourceResumeId);
        for (SectionPayload section : sections) {
            sectionServiceClient.addSection(SectionPayload.builder()
                    .resumeId(targetResumeId)
                    .sectionType(section.getSectionType())
                    .title(section.getTitle())
                    .content(section.getContent())
                    .displayOrder(section.getDisplayOrder())
                    .isVisible(section.getIsVisible())
                    .aiGenerated(section.getAiGenerated())
                    .build());
        }
    }

    /**
     * Seeds a new resume with empty boilerplate sections.
     * Calls SectionService over HTTP (Feign).
     */
    private void initializeDefaultSections(Long resumeId) {
        String[] defaultTypes = { "SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS", "PROJECTS", "CERTIFICATIONS" };
        int order = 1;
        for (String type : defaultTypes) {
            String title = defaultSectionTitle(type);

            sectionServiceClient.addSection(SectionPayload.builder()
                    .resumeId(resumeId)
                    .sectionType(type)
                    .title(title)
                    .content(defaultSectionContent(type))
                    .displayOrder(order++)
                    .isVisible(true)
                    .aiGenerated(false)
                    .build());
        }
    }

    private String defaultSectionTitle(String type) {
        return switch (type) {
            case "SUMMARY" -> "Professional Summary";
            case "EXPERIENCE" -> "Work Experience";
            case "EDUCATION" -> "Education";
            case "SKILLS" -> "Skills";
            case "PROJECTS" -> "Projects";
            case "CERTIFICATIONS" -> "Certifications";
            default -> type.substring(0, 1).toUpperCase() + type.substring(1).toLowerCase();
        };
    }

    private String defaultSectionContent(String type) {
        return switch (type) {
            case "SUMMARY" -> "{\"text\":\"\"}";
            case "EXPERIENCE", "EDUCATION", "SKILLS" -> "[]";
            case "PROJECTS", "CERTIFICATIONS" -> "{\"text\":\"\"}";
            default -> "{}";
        };
    }
}
