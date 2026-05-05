package com.airesume.resumeservice.service;

import com.airesume.resumeservice.dto.AtsUpdateDTO;
import com.airesume.resumeservice.dto.ResumeCreateRequest;
import com.airesume.resumeservice.dto.ResumeResponse;
import com.airesume.resumeservice.dto.ResumeUpdateRequest;
import com.airesume.resumeservice.client.dto.SectionPayload;
import com.airesume.resumeservice.model.Resume;
import com.airesume.resumeservice.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.airesume.resumeservice.security.CurrentUserService;
import com.airesume.resumeservice.client.SectionServiceClient;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 and Mockito tests for ResumeServiceImpl.
 * Tests business logic and CRUD operations.
 */
@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private SectionServiceClient sectionServiceClient;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private Resume sampleResume;

    @BeforeEach
    void setUp() {
        lenient().when(currentUserService.requireUserId()).thenReturn(100L);
        lenient().when(currentUserService.isAdmin()).thenReturn(false);
        lenient().when(currentUserService.isPremium()).thenReturn(false);
        sampleResume = Resume.builder()
                .resumeId(1L)
                .userId(100L)
                .title("Software Engineer")
                .targetJobTitle("Backend Developer")
                .templateId(10L)
                .atsScore(50)
                .status("DRAFT")
                .language("en")
                .isPublic(false)
                .viewCount(0)
                .build();
    }

    /**
     * Test successful creation of a resume.
     */
    @Test
    void testCreateResume_Success() {
        ResumeCreateRequest request = new ResumeCreateRequest(100L, "Software Engineer", 10L, "Backend Developer", "en");
        
        when(resumeRepository.countByUserId(100L)).thenReturn(1L);
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.createResume(request);

        assertNotNull(response);
        assertEquals("Software Engineer", response.getTitle());
        verify(resumeRepository, times(1)).save(any(Resume.class));
        verify(sectionServiceClient, times(4)).addSection(any(SectionPayload.class));
    }

    @Test
    void testCreateResume_InitializesSupportedDefaultSectionsWithExpectedContent() {
        ResumeCreateRequest request = new ResumeCreateRequest(100L, "Software Engineer", 10L, "Backend Developer", "en");

        when(resumeRepository.countByUserId(100L)).thenReturn(1L);
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        resumeService.createResume(request);

        ArgumentCaptor<SectionPayload> captor = ArgumentCaptor.forClass(SectionPayload.class);
        verify(sectionServiceClient, times(4)).addSection(captor.capture());

        List<SectionPayload> sections = captor.getAllValues();
        List<String> types = sections.stream()
                .map(SectionPayload::getSectionType)
                .collect(Collectors.toList());

        assertEquals(List.of("SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS"), types);
        assertEquals("{\"text\":\"\"}", sections.get(0).getContent());
        assertEquals("[]", sections.get(1).getContent());
        assertEquals("[]", sections.get(2).getContent());
        assertEquals("[]", sections.get(3).getContent());
    }

    /**
     * Test retrieving a resume by valid ID.
     */
    @Test
    void testGetResumeById_Success() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
        verify(resumeRepository, times(1)).findById(1L);
    }

    /**
     * Test retrieving a resume throws exception when ID not found.
     */
    @Test
    void testGetResumeById_NotFound() {
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        org.springframework.web.server.ResponseStatusException exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.getResumeById(99L);
        });

        assertEquals("Resume not found with ID: 99", exception.getReason());
    }

    /**
     * Test updating a resume's metadata.
     */
    @Test
    void testUpdateResume_Success() {
        ResumeUpdateRequest request = new ResumeUpdateRequest("Senior Dev", "Lead", "fr", "COMPLETE");
        
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.updateResume(1L, request);

        assertEquals("Senior Dev", sampleResume.getTitle());
        assertEquals("COMPLETE", sampleResume.getStatus());
        verify(resumeRepository, times(1)).save(sampleResume);
    }

    /**
     * Test updating the ATS score specifically.
     */
    @Test
    void testUpdateAtsScore_Success() {
        AtsUpdateDTO request = new AtsUpdateDTO(85);
        
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.updateAtsScore(1L, request);

        assertEquals(85, sampleResume.getAtsScore());
        verify(resumeRepository, times(1)).save(sampleResume);
    }

    /**
     * Test fetching resumes for a specific user.
     */
    @Test
    void testGetResumesByUser() {
        when(resumeRepository.findByUserId(100L)).thenReturn(Arrays.asList(sampleResume));

        List<ResumeResponse> responses = resumeService.getResumesByUser(100L);

        assertEquals(1, responses.size());
        assertEquals(100L, responses.get(0).getUserId());
    }

    /**
     * Test deleting a resume by user.
     */
    @Test
    void testDeleteResume_Success() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        doNothing().when(sectionServiceClient).deleteAllSectionsByResume(1L);
        doNothing().when(resumeRepository).deleteById(1L);

        assertDoesNotThrow(() -> resumeService.deleteResume(1L));
        verify(resumeRepository, times(1)).deleteById(1L);
    }
}
