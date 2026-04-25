package com.airesume.sectionservice.service.impl;

import com.airesume.sectionservice.model.Section;
import com.airesume.sectionservice.model.SectionType;
import com.airesume.sectionservice.repository.SectionRepository;
import com.airesume.sectionservice.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the SectionService interface.
 * Handles the business logic for managing resume sections, including database interactions
 * via SectionRepository.
 * 
 * @author ResumeAI Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;

    @Override
    @Transactional
    @CacheEvict(value = "sections_resume", key = "#section.resumeId")
    public Section addSection(Section section) {
        return sectionRepository.save(section);
    }

    @Override
    @Cacheable(value = "sections_resume", key = "#resumeId")
    public List<Section> getSectionsByResume(Long resumeId) {
        return sectionRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId);
    }

    @Override
    @Cacheable(value = "section", key = "#sectionId")
    public Section getSectionById(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found with id: " + sectionId));
    }

    @Override
    @Cacheable(value = "sections_type", key = "#resumeId + '_' + #type")
    public List<Section> getSectionsByType(Long resumeId, SectionType type) {
        return sectionRepository.findByResumeIdAndSectionType(resumeId, type);
    }

    @Override
    @Cacheable(value = "sections_ai", key = "#resumeId")
    public List<Section> getAiGeneratedSections(Long resumeId) {
        return sectionRepository.findByResumeIdAndAiGeneratedTrue(resumeId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"section", "sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public Section updateSection(Long sectionId, Section sectionDetails) {
        Section section = getSectionById(sectionId);
        if (sectionDetails.getTitle() != null) {
            section.setTitle(sectionDetails.getTitle());
        }
        if (sectionDetails.getContent() != null) {
            section.setContent(sectionDetails.getContent());
        }
        if (sectionDetails.getIsVisible() != null) {
            section.setIsVisible(sectionDetails.getIsVisible());
        }
        if (sectionDetails.getDisplayOrder() != null) {
            section.setDisplayOrder(sectionDetails.getDisplayOrder());
        }
        if (sectionDetails.getAiGenerated() != null) {
            section.setAiGenerated(sectionDetails.getAiGenerated());
        }
        return sectionRepository.save(section);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"section", "sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public Section toggleVisibility(Long sectionId) {
        Section section = getSectionById(sectionId);
        section.setIsVisible(!section.getIsVisible());
        return sectionRepository.save(section);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public void reorderSections(Long resumeId, List<Long> sectionIds) {
        List<Section> sections = sectionRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId);
        Map<Long, Section> sectionMap = sections.stream()
                .collect(Collectors.toMap(Section::getSectionId, s -> s));

        for (int i = 0; i < sectionIds.size(); i++) {
            Section section = sectionMap.get(sectionIds.get(i));
            if (section != null) {
                section.setDisplayOrder(i + 1);
                sectionRepository.save(section);
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"section", "sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public List<Section> bulkUpdate(List<Section> sections) {
        return sectionRepository.saveAll(sections);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"section", "sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public void deleteSection(Long sectionId) {
        sectionRepository.deleteById(sectionId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"section", "sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public void deleteAllSectionsByResume(Long resumeId) {
        sectionRepository.deleteByResumeId(resumeId);
    }

    @Override
    @Cacheable(value = "sections_count", key = "#resumeId")
    public long countSections(Long resumeId) {
        return sectionRepository.countByResumeId(resumeId);
    }
}
