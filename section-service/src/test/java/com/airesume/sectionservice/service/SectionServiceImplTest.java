package com.airesume.sectionservice.service;

import com.airesume.sectionservice.model.Section;
import com.airesume.sectionservice.model.SectionType;
import com.airesume.sectionservice.repository.SectionRepository;
import com.airesume.sectionservice.service.impl.SectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SectionServiceImplTest {

    @Mock
    private SectionRepository sectionRepository;

    @InjectMocks
    private SectionServiceImpl sectionService;

    private Section sampleSection;

    @BeforeEach
    void setUp() {
        sampleSection = new Section();
        sampleSection.setSectionId(1L);
        sampleSection.setResumeId(10L);
        sampleSection.setSectionType(SectionType.EXPERIENCE);
        sampleSection.setTitle("Work Experience");
        sampleSection.setContent("{\"company\":\"Google\"}");
        sampleSection.setDisplayOrder(1);
        sampleSection.setIsVisible(true);
        sampleSection.setAiGenerated(false);
    }

    @Test
    void addSection_ShouldReturnSavedSection() {
        when(sectionRepository.save(any(Section.class))).thenReturn(sampleSection);
        Section result = sectionService.addSection(sampleSection);
        assertNotNull(result);
        assertEquals("Work Experience", result.getTitle());
        verify(sectionRepository, times(1)).save(any(Section.class));
    }

    @Test
    void getSectionsByResume_ShouldReturnOrderedList() {
        Section section2 = new Section();
        section2.setSectionId(2L);
        section2.setDisplayOrder(2);
        
        when(sectionRepository.findByResumeIdOrderByDisplayOrderAsc(10L)).thenReturn(Arrays.asList(sampleSection, section2));
        
        List<Section> results = sectionService.getSectionsByResume(10L);
        assertEquals(2, results.size());
        assertEquals(1, results.get(0).getDisplayOrder());
    }

    @Test
    void getSectionById_ShouldReturnSectionIfExists() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(sampleSection));
        Section result = sectionService.getSectionById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getSectionId());
    }

    @Test
    void toggleVisibility_ShouldInvertIsVisibleAndSave() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(sampleSection));
        when(sectionRepository.save(any(Section.class))).thenReturn(sampleSection);
        
        Section result = sectionService.toggleVisibility(1L);
        assertFalse(result.getIsVisible());
    }
}
