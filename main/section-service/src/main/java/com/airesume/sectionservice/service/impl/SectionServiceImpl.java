package com.airesume.sectionservice.service.impl;

import com.airesume.sectionservice.client.ResumeServiceClient;
import com.airesume.sectionservice.model.Section;
import com.airesume.sectionservice.model.SectionType;
import com.airesume.sectionservice.repository.SectionRepository;
import com.airesume.sectionservice.service.SectionService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final ResumeServiceClient resumeServiceClient;

    @Override
    @Transactional
    @CacheEvict(value = "sections_resume", key = "#section.resumeId")
    public Section addSection(Section section) {
        verifyResumeAccess(section.getResumeId());
        if (section.getDisplayOrder() == null) {
            section.setDisplayOrder((int) sectionRepository.countByResumeId(section.getResumeId()));
        }
        return sectionRepository.save(section);
    }

    @Override
    @Cacheable(value = "sections_resume", key = "#resumeId")
    public List<Section> getSectionsByResume(Long resumeId) {
        verifyResumeAccess(resumeId);
        return sectionRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId);
    }

    @Override
    @Cacheable(value = "section", key = "#sectionId")
    public Section getSectionById(Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found with id: " + sectionId));
        verifyResumeAccess(section.getResumeId());
        return section;
    }

    @Override
    @Cacheable(value = "sections_type", key = "#resumeId + '_' + #type")
    public List<Section> getSectionsByType(Long resumeId, SectionType type) {
        verifyResumeAccess(resumeId);
        return sectionRepository.findByResumeIdAndSectionType(resumeId, type);
    }

    @Override
    @Cacheable(value = "sections_ai", key = "#resumeId")
    public List<Section> getAiGeneratedSections(Long resumeId) {
        verifyResumeAccess(resumeId);
        return sectionRepository.findByResumeIdAndAiGeneratedTrue(resumeId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"section", "sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public Section updateSection(Long sectionId, Section sectionDetails) {
        Section section = getSectionById(sectionId);
        if (sectionDetails.getResumeId() != null && !section.getResumeId().equals(sectionDetails.getResumeId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resume ownership cannot be changed for an existing section.");
        }
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
        verifyResumeAccess(resumeId);
        List<Section> sections = sectionRepository.findByResumeIdOrderByDisplayOrderAsc(resumeId);
        Map<Long, Section> sectionMap = sections.stream()
                .collect(Collectors.toMap(Section::getSectionId, s -> s));

        for (int index = 0; index < sectionIds.size(); index++) {
            Section section = sectionMap.get(sectionIds.get(index));
            if (section != null) {
                section.setDisplayOrder(index);
                sectionRepository.save(section);
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"section", "sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public List<Section> bulkUpdate(List<Section> sections) {
        sections.stream()
                .map(Section::getResumeId)
                .distinct()
                .forEach(this::verifyResumeAccess);
        return sectionRepository.saveAll(sections);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"section", "sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public void deleteSection(Long sectionId) {
        Section section = getSectionById(sectionId);
        sectionRepository.deleteById(section.getSectionId());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"section", "sections_resume", "sections_type", "sections_ai"}, allEntries = true)
    public void deleteAllSectionsByResume(Long resumeId) {
        verifyResumeAccess(resumeId);
        sectionRepository.deleteByResumeId(resumeId);
    }

    @Override
    @Cacheable(value = "sections_count", key = "#resumeId")
    public long countSections(Long resumeId) {
        verifyResumeAccess(resumeId);
        return sectionRepository.countByResumeId(resumeId);
    }

    private void verifyResumeAccess(Long resumeId) {
        try {
            resumeServiceClient.getResumeById(resumeId);
        } catch (FeignException.NotFound ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found with ID: " + resumeId, ex);
        } catch (FeignException.Forbidden ex) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this resume.", ex);
        } catch (FeignException.Unauthorized ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required to access this resume.", ex);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to verify access for resume {}", resumeId, ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not verify resume access.", ex);
        }
    }
}
