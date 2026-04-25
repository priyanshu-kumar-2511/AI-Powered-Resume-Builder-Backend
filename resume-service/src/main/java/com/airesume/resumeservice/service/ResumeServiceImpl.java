package com.airesume.resumeservice.service;

import com.airesume.resumeservice.dto.AtsUpdateDTO;
import com.airesume.resumeservice.dto.ResumeCreateRequest;
import com.airesume.resumeservice.dto.ResumeResponse;
import com.airesume.resumeservice.dto.ResumeUpdateRequest;
import com.airesume.resumeservice.model.Resume;
import com.airesume.resumeservice.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of the ResumeService interface.
 * Contains the core business logic for resume container management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"resumes_user", "public_resumes"}, allEntries = true)
    public ResumeResponse createResume(ResumeCreateRequest request) {
        // Evaluate user quota logic based on user tier if applicable.
        long currentCount = resumeRepository.countByUserId(request.getUserId());
        if(currentCount >= 3) {
            log.warn("User {} has reached the limit of 3 resumes (Free tier logic)", request.getUserId());
            // Limit enforcement logic can be uncommented or handled at an API Gateway/User Service level.
        }

        Resume resume = Resume.builder()
                .userId(request.getUserId())
                .title(request.getTitle())
                .templateId(request.getTemplateId())
                .targetJobTitle(request.getTargetJobTitle())
                .language(request.getLanguage() != null ? request.getLanguage() : "en")
                .status("DRAFT")
                .isPublic(false)
                .build();

        resume = resumeRepository.save(resume);
        log.info("Created new resume with ID {} for user {}", resume.getResumeId(), request.getUserId());
        return new ResumeResponse(resume);
    }

    @Override
    @Cacheable(value = "resume", key = "#resumeId")
    public ResumeResponse getResumeById(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID: " + resumeId));
        return new ResumeResponse(resume);
    }

    @Override
    @Cacheable(value = "resumes_user", key = "#userId")
    public List<ResumeResponse> getResumesByUser(Long userId) {
        return resumeRepository.findByUserId(userId).stream()
                .map(ResumeResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "resumes_template", key = "#templateId")
    public List<ResumeResponse> getResumesByTemplate(Long templateId) {
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
    @CacheEvict(value = {"resume", "resumes_user", "resumes_template", "public_resumes"}, allEntries = true)
    public ResumeResponse updateResume(Long resumeId, ResumeUpdateRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID: " + resumeId));

        if (request.getTitle() != null) resume.setTitle(request.getTitle());
        if (request.getTargetJobTitle() != null) resume.setTargetJobTitle(request.getTargetJobTitle());
        if (request.getLanguage() != null) resume.setLanguage(request.getLanguage());
        if (request.getStatus() != null) resume.setStatus(request.getStatus());

        log.info("Updated resume with ID {}", resumeId);
        return new ResumeResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"resume", "resumes_user", "resumes_template", "public_resumes"}, allEntries = true)
    public ResumeResponse updateAtsScore(Long resumeId, AtsUpdateDTO request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID: " + resumeId));

        resume.setAtsScore(request.getAtsScore());
        log.info("Updated ATS score for resume {} to {}", resumeId, request.getAtsScore());
        return new ResumeResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"resumes_user"}, allEntries = true)
    public ResumeResponse duplicateResume(Long resumeId) {
        Resume original = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID: " + resumeId));

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
        log.info("Duplicated resume {} into new resume {}", resumeId, duplicate.getResumeId());
        
        // Note: Section data duplication should happen here via inter-service communication
        // or an event-driven mechanism triggering Section-Service.
        
        return new ResumeResponse(duplicate);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"resume", "resumes_user", "public_resumes"}, allEntries = true)
    public ResumeResponse publishResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID: " + resumeId));
        resume.setPublic(true);
        resume.setStatus("COMPLETE");
        log.info("Published resume {}", resumeId);
        return new ResumeResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"resume", "resumes_user", "public_resumes"}, allEntries = true)
    public ResumeResponse unpublishResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID: " + resumeId));
        resume.setPublic(false);
        log.info("Unpublished resume {}", resumeId);
        return new ResumeResponse(resumeRepository.save(resume));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"resume", "resumes_user", "public_resumes"}, allEntries = true)
    public void incrementViewCount(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found with ID: " + resumeId));
        resume.setViewCount(resume.getViewCount() + 1);
        resumeRepository.save(resume);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"resume", "resumes_user", "resumes_template", "public_resumes"}, allEntries = true)
    public void deleteResume(Long resumeId) {
        if (!resumeRepository.existsById(resumeId)) {
            throw new RuntimeException("Resume not found with ID: " + resumeId);
        }
        resumeRepository.deleteById(resumeId);
        log.info("Deleted resume {}", resumeId);
    }

    @Override
    public List<ResumeResponse> getAllResumes() {
        return resumeRepository.findAll().stream()
                .map(ResumeResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"resume", "resumes_user", "resumes_template", "public_resumes"}, allEntries = true)
    public void forceDeleteResume(Long resumeId) {
        if (!resumeRepository.existsById(resumeId)) {
            throw new RuntimeException("Resume not found with ID: " + resumeId);
        }
        resumeRepository.deleteById(resumeId);
        log.info("Admin forcefully deleted resume {}", resumeId);
    }

    @Override
    public Long countUserResumes(Long userId) {
        return resumeRepository.countByUserId(userId);
    }
}
