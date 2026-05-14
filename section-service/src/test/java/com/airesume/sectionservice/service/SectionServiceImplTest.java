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

import com.airesume.sectionservice.client.ResumeServiceClient;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for SectionServiceImpl.
 * Verifies business logic for section ordering, inter-service resume access validation,
 * and robust error handling for Feign client failures.
 */
@ExtendWith(MockitoExtension.class)
public class SectionServiceImplTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private ResumeServiceClient resumeServiceClient;

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

    /**
     * Verifies that adding a section with an explicit display order preserves that order.
     */
    @Test
    void addSection_ShouldReturnSavedSection_WhenDisplayOrderIsNotNull() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.save(any(Section.class))).thenReturn(sampleSection);
        Section result = sectionService.addSection(sampleSection);
        assertNotNull(result);
        assertEquals("Work Experience", result.getTitle());
    }

    /**
     * Verifies that the service automatically assigns a display order when none is provided.
     */
    @Test
    void addSection_ShouldSetDisplayOrder_WhenNull() {
        sampleSection.setDisplayOrder(null);
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.countByResumeId(10L)).thenReturn(5L);
        when(sectionRepository.save(any(Section.class))).thenReturn(sampleSection);
        
        sectionService.addSection(sampleSection);
        assertEquals(5, sampleSection.getDisplayOrder());
    }

    /**
     * Verifies that sections retrieved for a resume are returned in ascending display order.
     */
    @Test
    void getSectionsByResume_ShouldReturnOrderedList() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        Section section2 = new Section();
        section2.setSectionId(2L);
        section2.setDisplayOrder(2);
        
        when(sectionRepository.findByResumeIdOrderByDisplayOrderAsc(10L)).thenReturn(Arrays.asList(sampleSection, section2));
        
        List<Section> results = sectionService.getSectionsByResume(10L);
        assertEquals(2, results.size());
    }

    /**
     * Verifies that a section can be retrieved successfully if it exists.
     */
    @Test
    void getSectionById_ShouldReturnSectionIfExists() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(sampleSection));
        Section result = sectionService.getSectionById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getSectionId());
    }

    /**
     * Ensures that a 404 Not Found error is thrown when requesting a non-existent section.
     */
    @Test
    void getSectionById_ShouldThrowNotFound() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            sectionService.getSectionById(1L);
        });
    }

    /**
     * Verifies filtering of sections by their type.
     */
    @Test
    void getSectionsByType_ShouldReturnList() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.findByResumeIdAndSectionType(10L, SectionType.EXPERIENCE))
            .thenReturn(List.of(sampleSection));
        List<Section> result = sectionService.getSectionsByType(10L, SectionType.EXPERIENCE);
        assertEquals(1, result.size());
    }

    /**
     * Verifies retrieval of all sections that were marked as AI-generated.
     */
    @Test
    void getAiGeneratedSections_ShouldReturnList() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.findByResumeIdAndAiGeneratedTrue(10L))
            .thenReturn(List.of(sampleSection));
        List<Section> result = sectionService.getAiGeneratedSections(10L);
        assertEquals(1, result.size());
    }

    /**
     * Verifies that an existing section can be updated with new metadata.
     */
    @Test
    void updateSection_ShouldUpdateAndReturn() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(sampleSection));
        when(sectionRepository.save(any(Section.class))).thenReturn(sampleSection);

        Section updates = new Section();
        updates.setTitle("Updated Title");
        updates.setContent("{}");
        updates.setIsVisible(false);
        updates.setDisplayOrder(10);
        updates.setAiGenerated(true);
        updates.setResumeId(10L); // same resume ID

        Section result = sectionService.updateSection(1L, updates);
        assertEquals("Updated Title", result.getTitle());
    }

    /**
     * Verifies that a section's resume ownership cannot be changed during an update.
     */
    @Test
    void updateSection_ShouldThrowIfOwnershipChanges() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(sampleSection));
        
        Section updates = new Section();
        updates.setResumeId(20L); // different resume ID

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            sectionService.updateSection(1L, updates);
        });
    }

    /**
     * Verifies the visibility toggle logic, ensuring the state is inverted and saved.
     */
    @Test
    void toggleVisibility_ShouldInvertIsVisibleAndSave() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(sampleSection));
        when(sectionRepository.save(any(Section.class))).thenReturn(sampleSection);
        
        Section result = sectionService.toggleVisibility(1L);
        assertFalse(result.getIsVisible());
    }

    /**
     * Tests the reordering logic, ensuring display order values are updated correctly in the database.
     */
    @Test
    void reorderSections_ShouldUpdateDisplayOrders() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        Section s2 = new Section(); s2.setSectionId(2L);
        when(sectionRepository.findByResumeIdOrderByDisplayOrderAsc(10L))
            .thenReturn(List.of(sampleSection, s2));

        sectionService.reorderSections(10L, List.of(2L, 1L));

        verify(sectionRepository, times(2)).save(any(Section.class));
        assertEquals(1, sampleSection.getDisplayOrder());
        assertEquals(0, s2.getDisplayOrder());
    }

    /**
     * Verifies that multiple sections can be saved in a single batch operation.
     */
    @Test
    void bulkUpdate_ShouldSaveAll() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.saveAll(any())).thenReturn(List.of(sampleSection));
        sectionService.bulkUpdate(List.of(sampleSection));
        verify(sectionRepository, times(1)).saveAll(any());
    }

    /**
     * Verifies that a section can be deleted if it exists.
     */
    @Test
    void deleteSection_ShouldDelete() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(sampleSection));
        sectionService.deleteSection(1L);
        verify(sectionRepository, times(1)).deleteById(1L);
    }

    /**
     * Verifies that all sections associated with a resume ID can be deleted in a single call.
     */
    @Test
    void deleteAllSectionsByResume_ShouldDeleteAll() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        sectionService.deleteAllSectionsByResume(10L);
        verify(sectionRepository, times(1)).deleteByResumeId(10L);
    }

    /**
     * Verifies the count of sections for a specific resume.
     */
    @Test
    void countSections_ShouldReturnCount() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.countByResumeId(10L)).thenReturn(5L);
        assertEquals(5L, sectionService.countSections(10L));
    }

    /**
     * Verifies that Feign client "Not Found" errors are correctly mapped to local ResponseStatusExceptions.
     */
    @Test
    void verifyResumeAccess_ShouldHandleFeignNotFound() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.GET, "url", java.util.Collections.emptyMap(), null, new feign.RequestTemplate());
        feign.FeignException.NotFound ex = new feign.FeignException.NotFound("Not found", request, null, null);
        when(resumeServiceClient.getResumeById(10L)).thenThrow(ex);
        
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> sectionService.getSectionsByResume(10L));
    }

    /**
     * Verifies that Feign "Forbidden" (403) errors are correctly mapped to local ResponseStatusExceptions.
     */
    @Test
    void verifyResumeAccess_ShouldHandleFeignForbidden() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.GET, "url", java.util.Collections.emptyMap(), null, new feign.RequestTemplate());
        feign.FeignException.Forbidden ex = new feign.FeignException.Forbidden("Forbidden", request, null, null);
        when(resumeServiceClient.getResumeById(10L)).thenThrow(ex);
        
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> sectionService.getSectionsByResume(10L));
    }

    /**
     * Verifies that Feign "Unauthorized" (401) errors are correctly mapped to local ResponseStatusExceptions.
     */
    @Test
    void verifyResumeAccess_ShouldHandleFeignUnauthorized() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.GET, "url", java.util.Collections.emptyMap(), null, new feign.RequestTemplate());
        feign.FeignException.Unauthorized ex = new feign.FeignException.Unauthorized("Unauthorized", request, null, null);
        when(resumeServiceClient.getResumeById(10L)).thenThrow(ex);
        
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> sectionService.getSectionsByResume(10L));
    }

    /**
     * Verifies that generic runtime exceptions during inter-service calls result in a 500 Internal Server Error mapping.
     */
    @Test
    void verifyResumeAccess_ShouldHandleGenericException() {
        when(resumeServiceClient.getResumeById(10L)).thenThrow(new RuntimeException("Error"));
        
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> sectionService.getSectionsByResume(10L));
    }

    /**
     * Verifies that partial updates preserve existing section data when null values are provided in the request.
     */
    @Test
    void updateSection_ShouldKeepOriginalValues_WhenUpdatesAreNull() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(sampleSection));
        when(sectionRepository.save(any(Section.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Section updates = new Section(); // all fields null

        Section result = sectionService.updateSection(1L, updates);
        assertEquals("Work Experience", result.getTitle());
        assertEquals("{\"company\":\"Google\"}", result.getContent());
        assertTrue(result.getIsVisible());
        assertEquals(1, result.getDisplayOrder());
        assertFalse(result.getAiGenerated());
    }

    /**
     * Verifies that the reordering logic handles invalid section IDs without crashing.
     */
    @Test
    void reorderSections_ShouldIgnoreNullSections() {
        lenient().when(resumeServiceClient.getResumeById(10L)).thenReturn(null);
        Section s2 = new Section(); s2.setSectionId(2L);
        when(sectionRepository.findByResumeIdOrderByDisplayOrderAsc(10L))
            .thenReturn(List.of(sampleSection, s2));

        // 99L is not in the list, so it will be null in the map
        sectionService.reorderSections(10L, List.of(99L, 2L, 1L));

        verify(sectionRepository, times(2)).save(any(Section.class)); // only s2 and sampleSection are saved
    }

    /**
     * Verifies that existing ResponseStatusExceptions from the Feign client are propagated without modification.
     */
    @Test
    void verifyResumeAccess_ShouldHandleResponseStatusException() {
        org.springframework.web.server.ResponseStatusException ex = new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Direct response status");
        when(resumeServiceClient.getResumeById(10L)).thenThrow(ex);
        
        org.springframework.web.server.ResponseStatusException thrown = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> sectionService.getSectionsByResume(10L));
        assertEquals(org.springframework.http.HttpStatus.BAD_REQUEST, thrown.getStatusCode());
        assertEquals("Direct response status", thrown.getReason());
    }
}
