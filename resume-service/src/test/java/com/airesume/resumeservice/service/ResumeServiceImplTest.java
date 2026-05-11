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

    @Test
    void testCreateResume_Success() {
        ResumeCreateRequest request = new ResumeCreateRequest(100L, "Software Engineer", 10L, "Backend Developer", "en");
        
        when(resumeRepository.countByUserId(100L)).thenReturn(1L);
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.createResume(request);

        assertNotNull(response);
        assertEquals("Software Engineer", response.getTitle());
        verify(resumeRepository, times(1)).save(any(Resume.class));
        verify(sectionServiceClient, times(6)).addSection(any(SectionPayload.class));
    }

    @Test
    void testCreateResume_InitializesSupportedDefaultSectionsWithExpectedContent() {
        ResumeCreateRequest request = new ResumeCreateRequest(100L, "Software Engineer", 10L, "Backend Developer", "en");

        when(resumeRepository.countByUserId(100L)).thenReturn(1L);
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        resumeService.createResume(request);

        ArgumentCaptor<SectionPayload> captor = ArgumentCaptor.forClass(SectionPayload.class);
        verify(sectionServiceClient, times(6)).addSection(captor.capture());

        List<SectionPayload> sections = captor.getAllValues();
        List<String> types = sections.stream()
                .map(SectionPayload::getSectionType)
                .collect(Collectors.toList());

        assertEquals(List.of("SUMMARY", "EXPERIENCE", "EDUCATION", "SKILLS", "PROJECTS", "CERTIFICATIONS"), types);
    }

    @Test
    void testCreateResume_FailsLimitForFreeUser() {
        ResumeCreateRequest request = new ResumeCreateRequest(100L, "Software Engineer", 10L, "Backend Developer", "en");
        when(resumeRepository.countByUserId(100L)).thenReturn(3L);

        org.springframework.web.server.ResponseStatusException exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.createResume(request);
        });

        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void testCreateResume_FailsSectionInit() {
        ResumeCreateRequest request = new ResumeCreateRequest(100L, "Software Engineer", 10L, "Backend Developer", "en");
        when(resumeRepository.countByUserId(100L)).thenReturn(1L);
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);
        doThrow(new RuntimeException("Feign Error")).when(sectionServiceClient).addSection(any());

        org.springframework.web.server.ResponseStatusException exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.createResume(request);
        });

        assertEquals(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        verify(resumeRepository).deleteById(1L);
    }

    @Test
    void testGetResumeById_Success() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));

        ResumeResponse response = resumeService.getResumeById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
    }

    @Test
    void testGetResumeById_NotFound() {
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        org.springframework.web.server.ResponseStatusException exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.getResumeById(99L);
        });

        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void testGetResumeById_Forbidden() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(currentUserService.requireUserId()).thenReturn(200L); // Different user

        org.springframework.web.server.ResponseStatusException exception = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.getResumeById(1L);
        });

        assertEquals(org.springframework.http.HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void testGetResumeById_PublicIsAllowed() {
        sampleResume.setPublic(true);
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        // Different user, but resume is public

        ResumeResponse response = resumeService.getResumeById(1L);
        assertNotNull(response);
    }

    @Test
    void testGetResumeById_AdminIsAllowed() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(currentUserService.isAdmin()).thenReturn(true);
        // Different user, but user is admin

        ResumeResponse response = resumeService.getResumeById(1L);
        assertNotNull(response);
    }

    @Test
    void testUpdateResume_Success() {
        ResumeUpdateRequest request = new ResumeUpdateRequest("Senior Dev", "Lead", "fr", "COMPLETE", null);
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.updateResume(1L, request);

        assertEquals("Senior Dev", sampleResume.getTitle());
    }

    @Test
    void testUpdateAtsScore_Success() {
        AtsUpdateDTO request = new AtsUpdateDTO(85);
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.updateAtsScore(1L, request);

        assertEquals(85, sampleResume.getAtsScore());
    }

    @Test
    void testGetResumesByUser_Success() {
        when(resumeRepository.findByUserId(100L)).thenReturn(Arrays.asList(sampleResume));

        List<ResumeResponse> responses = resumeService.getResumesByUser(100L);

        assertEquals(1, responses.size());
    }

    @Test
    void testGetResumesByUser_Forbidden() {
        when(currentUserService.requireUserId()).thenReturn(200L);

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.getResumesByUser(100L);
        });
    }

    @Test
    void testGetResumesByTemplate() {
        when(resumeRepository.findByTemplateId(10L)).thenReturn(List.of(sampleResume));
        
        List<ResumeResponse> responses = resumeService.getResumesByTemplate(10L);
        
        assertEquals(1, responses.size());
        verify(currentUserService).requireAdmin();
    }

    @Test
    void testGetPublicResumes() {
        when(resumeRepository.findByIsPublic(true)).thenReturn(List.of(sampleResume));
        
        List<ResumeResponse> responses = resumeService.getPublicResumes();
        
        assertEquals(1, responses.size());
    }

    @Test
    void testDuplicateResume_Success() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.countByUserId(100L)).thenReturn(1L);
        
        Resume duplicatedResume = Resume.builder().resumeId(2L).userId(100L).build();
        when(resumeRepository.save(any(Resume.class))).thenReturn(duplicatedResume);
        
        SectionPayload section = SectionPayload.builder().sectionType("SUMMARY").title("Sum").content("txt").build();
        when(sectionServiceClient.getSectionsByResume(1L)).thenReturn(List.of(section));

        ResumeResponse response = resumeService.duplicateResume(1L);
        
        assertNotNull(response);
        verify(sectionServiceClient).addSection(any(SectionPayload.class));
    }

    @Test
    void testDuplicateResume_FailsSectionInit() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.countByUserId(100L)).thenReturn(1L);
        
        Resume duplicatedResume = Resume.builder().resumeId(2L).userId(100L).build();
        when(resumeRepository.save(any(Resume.class))).thenReturn(duplicatedResume);
        
        when(sectionServiceClient.getSectionsByResume(1L)).thenThrow(new RuntimeException("Feign Error"));

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.duplicateResume(1L);
        });
        
        verify(resumeRepository).deleteById(2L);
    }

    @Test
    void testPublishResume() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);
        
        resumeService.publishResume(1L);
        
        assertTrue(sampleResume.isPublic());
        assertEquals("COMPLETE", sampleResume.getStatus());
    }

    @Test
    void testUnpublishResume() {
        sampleResume.setPublic(true);
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);
        
        resumeService.unpublishResume(1L);
        
        assertFalse(sampleResume.isPublic());
    }

    @Test
    void testIncrementViewCount_Success() {
        sampleResume.setPublic(true);
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        
        resumeService.incrementViewCount(1L);
        
        assertEquals(1, sampleResume.getViewCount());
        verify(resumeRepository).save(sampleResume);
    }

    @Test
    void testIncrementViewCount_NotPublic() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.incrementViewCount(1L);
        });
    }

    @Test
    void testDeleteResume_Success() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        doNothing().when(sectionServiceClient).deleteAllSectionsByResume(1L);
        doNothing().when(resumeRepository).deleteById(1L);

        assertDoesNotThrow(() -> resumeService.deleteResume(1L));
        verify(resumeRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetAllResumes() {
        when(resumeRepository.findAll()).thenReturn(List.of(sampleResume));
        
        List<ResumeResponse> responses = resumeService.getAllResumes();
        
        assertEquals(1, responses.size());
        verify(currentUserService).requireAdmin();
    }

    @Test
    void testForceDeleteResume() {
        when(resumeRepository.existsById(1L)).thenReturn(true);
        
        resumeService.forceDeleteResume(1L);
        
        verify(currentUserService).requireAdmin();
        verify(sectionServiceClient).deleteAllSectionsByResume(1L);
        verify(resumeRepository).deleteById(1L);
    }

    @Test
    void testForceDeleteResume_NotFound() {
        when(resumeRepository.existsById(1L)).thenReturn(false);
        
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.forceDeleteResume(1L);
        });
    }

    @Test
    void testCountUserResumes() {
        when(resumeRepository.countByUserId(100L)).thenReturn(5L);
        
        Long count = resumeService.countUserResumes(100L);
        
        assertEquals(5L, count);
    }

    @Test
    void testCreateResume_NullLanguage() {
        ResumeCreateRequest request = new ResumeCreateRequest(100L, "Software Engineer", 10L, "Backend Developer", null);
        
        when(resumeRepository.countByUserId(100L)).thenReturn(1L);
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.createResume(request);

        assertNotNull(response);
        assertEquals("en", sampleResume.getLanguage());
    }

    @Test
    void testDefaultSectionTitleAndContent_CustomType() {
        String title = org.springframework.test.util.ReflectionTestUtils.invokeMethod(resumeService, "defaultSectionTitle", "CUSTOM");
        assertEquals("Custom", title);

        String content = org.springframework.test.util.ReflectionTestUtils.invokeMethod(resumeService, "defaultSectionContent", "CUSTOM");
        assertEquals("{}", content);
    }

    @Test
    void testIncrementViewCount_NotFound() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.incrementViewCount(1L);
        });
    }

    @Test
    void testRequireOwnedOrAdminResume_NotFound() {
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> {
            resumeService.publishResume(99L);
        });
    }

    @Test
    void testValidateUserAccess_AdminBypass() {
        when(currentUserService.isAdmin()).thenReturn(true);
        when(resumeRepository.findByUserId(200L)).thenReturn(List.of());

        List<ResumeResponse> responses = resumeService.getResumesByUser(200L);
        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void testRequireOwnedOrAdminResume_AdminBypass() {
        Resume otherUserResume = Resume.builder()
                .resumeId(1L)
                .userId(200L)
                .title("Other User Resume")
                .build();

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(otherUserResume));
        when(currentUserService.isAdmin()).thenReturn(true);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResumeUpdateRequest request = new ResumeUpdateRequest("New Title", null, null, null, null);
        ResumeResponse response = resumeService.updateResume(1L, request);

        assertEquals("New Title", response.getTitle());
    }

    @Test
    void testCreateResume_PremiumUser_BypassesLimit() {
        ResumeCreateRequest request = new ResumeCreateRequest(100L, "Software Engineer", 10L, "Backend Developer", "en");
        
        when(currentUserService.isPremium()).thenReturn(true);
        when(resumeRepository.countByUserId(100L)).thenReturn(5L); // Higher than FREE_RESUME_LIMIT
        when(resumeRepository.save(any(Resume.class))).thenReturn(sampleResume);

        ResumeResponse response = resumeService.createResume(request);

        assertNotNull(response);
    }

    @Test
    void testDuplicateResume_EmptySections() {
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(sampleResume));
        when(resumeRepository.countByUserId(100L)).thenReturn(1L);
        
        Resume duplicatedResume = Resume.builder().resumeId(2L).userId(100L).build();
        when(resumeRepository.save(any(Resume.class))).thenReturn(duplicatedResume);
        
        when(sectionServiceClient.getSectionsByResume(1L)).thenReturn(List.of());

        ResumeResponse response = resumeService.duplicateResume(1L);
        
        assertNotNull(response);
        verify(sectionServiceClient, never()).addSection(any());
    }
}
