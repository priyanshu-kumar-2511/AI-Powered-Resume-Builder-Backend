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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResumeController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for simple controller tests
public class ResumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ResumeService resumeService;

    @Autowired
    private ObjectMapper objectMapper;

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
}
