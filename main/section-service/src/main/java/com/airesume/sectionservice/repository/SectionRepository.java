package com.airesume.sectionservice.repository;

import com.airesume.sectionservice.model.Section;
import com.airesume.sectionservice.model.SectionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    
    List<Section> findByResumeIdOrderByDisplayOrderAsc(Long resumeId);
    
    List<Section> findByResumeIdAndSectionType(Long resumeId, SectionType sectionType);
    
    List<Section> findByResumeIdAndAiGeneratedTrue(Long resumeId);
    
    long countByResumeId(Long resumeId);
    
    void deleteByResumeId(Long resumeId);
}
