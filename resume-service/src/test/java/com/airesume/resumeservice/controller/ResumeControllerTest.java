package com.airesume.resumeservice.controller;

import com.airesume.resumeservice.dto.ResumeCreateRequest;
import com.airesume.resumeservice.dto.ResumeResponse;
import com.airesume.resumeservice.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the Resume Controller.
 * Verifies resume creation, retrieval, duplication, and lifecycle status updates.
 */
@WebMvcTest(ResumeController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple controller tests
public class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResumeService resumeService;

    @MockitoBean
    private com.airesume.resumeservice.service.JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Verifies that a new resume can be successfully created via the POST endpoint.
     */
    @Test
    @DisplayName("API: POST / - Should create a resume")
    void createResume_ShouldReturnCreatedResume() throws Exception {
        ResumeCreateRequest request = new ResumeCreateRequest();
        request.setUserId(1L);
        request.setTitle("My Resume");
        
        ResumeResponse response = new ResumeResponse();
        response.setResumeId(10L);
        response.setUserId(1L);
        response.setTitle("My Resume");
        response.setCreatedAt(LocalDateTime.now());
        
        when(resumeService.createResume(any(ResumeCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resumeId").value(10))
                .andExpect(jsonPath("$.title").value("My Resume"));
    }

    /**
     * Verifies the retrieval of a specific resume by its ID.
     */
    @Test
    @DisplayName("API: GET /{resumeId} - Should return resume")
    void getResumeById_ShouldReturnResume() throws Exception {
        ResumeResponse response = new ResumeResponse();
        response.setResumeId(10L);
        response.setUserId(1L);
        response.setTitle("My Resume");

        when(resumeService.getResumeById(10L)).thenReturn(response);

        mockMvc.perform(get("/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumeId").value(10))
                .andExpect(jsonPath("$.title").value("My Resume"));
    }

    /**
     * Verifies that all resumes belonging to a specific user can be retrieved.
     */
    @Test
    @DisplayName("API: GET /user/{userId} - Should return user resumes")
    void getResumesByUser_ShouldReturnList() throws Exception {
        when(resumeService.getResumesByUser(1L)).thenReturn(List.of(new ResumeResponse()));

        mockMvc.perform(get("/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    /**
     * Verifies the resume duplication/cloning functionality.
     */
    @Test
    @DisplayName("API: POST /{resumeId}/duplicate - Should duplicate resume")
    void duplicateResume_ShouldReturnCreated() throws Exception {
        when(resumeService.duplicateResume(10L)).thenReturn(new ResumeResponse());

        mockMvc.perform(post("/10/duplicate"))
                .andExpect(status().isCreated());
    }

    /**
     * Verifies retrieval of publicly shared resumes.
     */
    @Test
    @DisplayName("API: GET /public - Should return public resumes")
    void getPublicResumes_ShouldReturnList() throws Exception {
        when(resumeService.getPublicResumes()).thenReturn(List.of());

        mockMvc.perform(get("/public"))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that administrators can retrieve the master list of all resumes.
     */
    @Test
    @DisplayName("API: GET /admin/all - Should return all resumes for admin")
    void getAllResumes_ShouldReturnOk() throws Exception {
        when(resumeService.getAllResumes()).thenReturn(List.of());

        mockMvc.perform(get("/admin/all"))
                .andExpect(status().isOk());
    }

    /**
     * Verifies filtering of resumes by their associated template ID.
     */
    @Test
    @DisplayName("API: GET /template/{templateId} - Should return resumes by template")
    void getResumesByTemplate_ShouldReturnList() throws Exception {
        when(resumeService.getResumesByTemplate(1L)).thenReturn(List.of(new ResumeResponse()));

        mockMvc.perform(get("/template/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    /**
     * Tests the update functionality for resume titles and metadata.
     */
    @Test
    @DisplayName("API: PUT /{resumeId} - Should update resume")
    void updateResume_ShouldReturnUpdatedResume() throws Exception {
        com.airesume.resumeservice.dto.ResumeUpdateRequest request = new com.airesume.resumeservice.dto.ResumeUpdateRequest();
        request.setTitle("Updated Title");

        ResumeResponse response = new ResumeResponse();
        response.setResumeId(10L);
        response.setTitle("Updated Title");

        when(resumeService.updateResume(any(Long.class), any(com.airesume.resumeservice.dto.ResumeUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    /**
     * Verifies that the ATS score of a resume can be updated.
     */
    @Test
    @DisplayName("API: PUT /{resumeId}/ats-score - Should update ATS score")
    void updateAtsScore_ShouldReturnUpdatedResume() throws Exception {
        com.airesume.resumeservice.dto.AtsUpdateDTO request = new com.airesume.resumeservice.dto.AtsUpdateDTO();
        request.setAtsScore(85);

        ResumeResponse response = new ResumeResponse();
        response.setResumeId(10L);
        response.setAtsScore(85);

        when(resumeService.updateAtsScore(any(Long.class), any(com.airesume.resumeservice.dto.AtsUpdateDTO.class))).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/10/ats-score")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.atsScore").value(85));
    }

    /**
     * Verifies that a resume can be marked as public for sharing.
     */
    @Test
    @DisplayName("API: PUT /{resumeId}/publish - Should publish resume")
    void publishResume_ShouldReturnUpdatedResume() throws Exception {
        ResumeResponse response = new ResumeResponse();
        response.setResumeId(10L);
        response.setPublic(true);

        when(resumeService.publishResume(10L)).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/10/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.public").value(true));
    }

    /**
     * Verifies that a previously public resume can be made private.
     */
    @Test
    @DisplayName("API: PUT /{resumeId}/unpublish - Should unpublish resume")
    void unpublishResume_ShouldReturnUpdatedResume() throws Exception {
        ResumeResponse response = new ResumeResponse();
        response.setResumeId(10L);
        response.setPublic(false);

        when(resumeService.unpublishResume(10L)).thenReturn(response);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/10/unpublish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.public").value(false));
    }

    /**
     * Verifies that the view count for a resume can be incremented when accessed via public links.
     */
    @Test
    @DisplayName("API: PUT /{resumeId}/view-count - Should increment view count")
    void incrementViewCount_ShouldReturnOk() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/10/view-count"))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that a resume can be deleted by its owner.
     */
    @Test
    @DisplayName("API: DELETE /{resumeId} - Should delete resume")
    void deleteResume_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/10"))
                .andExpect(status().isNoContent());
    }

    /**
     * Verifies that administrators can bypass standard checks and force delete any resume.
     */
    @Test
    @DisplayName("API: DELETE /admin/{resumeId} - Should force delete resume")
    void forceDeleteResume_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/admin/10"))
                .andExpect(status().isNoContent());
    }

    /**
     * Verifies the count of resumes for a given user for administrative reporting.
     */
    @Test
    @DisplayName("API: GET /admin/count/{userId} - Should return count")
    void countUserResumes_ShouldReturnCount() throws Exception {
        when(resumeService.countUserResumes(1L)).thenReturn(5L);

        mockMvc.perform(get("/admin/count/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(5));
    }
}
