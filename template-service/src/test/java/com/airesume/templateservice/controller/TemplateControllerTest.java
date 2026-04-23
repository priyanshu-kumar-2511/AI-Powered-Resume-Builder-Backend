package com.airesume.templateservice.controller;

import com.airesume.templateservice.config.JwtAuthenticationFilter;
import com.airesume.templateservice.config.SecurityConfig;
import com.airesume.templateservice.controller.TemplateController;
import com.airesume.templateservice.dto.TemplateResponseDTO;
import com.airesume.templateservice.model.Category;
import com.airesume.templateservice.model.Template;
import com.airesume.templateservice.model.Tier;
import com.airesume.templateservice.service.JwtService;
import com.airesume.templateservice.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Layer Tests for TemplateController.
 * Technology: JUnit 5 + Mockito + MockMvc + Spring Security Test.
 * Verifies API routing, serialization, and RBAC (Role-Based Access Control).
 */
@WebMvcTest(TemplateController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc
public class TemplateControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulated HTTP client for controller testing

    @MockBean
    private TemplateService templateService; // Mocking the business layer

    @MockBean
    private JwtService jwtService; // Mocking the JWT service required by SecurityConfig

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    @Test
    @DisplayName("API: GET /api/v1/templates - Should return list of templates (Public)")
    void getAllTemplates_ShouldReturnList() throws Exception {
        Template template = Template.builder()
                .templateId(1L)
                .name("Modern")
                .category(Category.MODERN)
                .tier(Tier.FREE)
                .isActive(true)
                .build();

        // GIVEN: Service returns a list
        when(templateService.getAllActiveTemplates()).thenReturn(Arrays.asList(template));

        // WHEN & THEN: Perform request and verify JSON output
        mockMvc.perform(get("/api/v1/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Modern"))
                .andExpect(jsonPath("$[0].category").value("MODERN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN") // Mocking an authenticated Admin user
    @DisplayName("API: POST /api/v1/templates - Admin should be able to create")
    void createTemplate_AsAdmin_ShouldReturnCreated() throws Exception {
        Template template = Template.builder()
                .name("New Template")
                .category(Category.PROFESSIONAL)
                .tier(Tier.FREE)
                .build();

        when(templateService.createTemplate(any(Template.class))).thenReturn(template);

        mockMvc.perform(post("/api/v1/templates")
                        .with(csrf()) // Adding CSRF token for security bypass in tests
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Template"));
    }

    @Test
    @WithMockUser(roles = "USER") // Mocking a standard User (Not Admin)
    @DisplayName("API: POST /api/v1/templates - Standard user should be forbidden")
    void createTemplate_AsUser_ShouldReturnForbidden() throws Exception {
        Template template = Template.builder().name("Forbidden").build();

        mockMvc.perform(post("/api/v1/templates")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isForbidden()); // Expecting 403 Forbidden
    }

    @Test
    @DisplayName("API: GET /api/v1/templates/{id} - Should return full template layout")
    void getTemplateById_ShouldReturnFullTemplate() throws Exception {
        Template template = Template.builder()
                .templateId(1L)
                .name("Modern")
                .htmlLayout("<html></html>")
                .build();

        when(templateService.getTemplateById(1L)).thenReturn(template);

        mockMvc.perform(get("/api/v1/templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.htmlLayout").value("<html></html>"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("API: PUT /api/v1/templates/{id}/deactivate - Admin should be able to deactivate")
    void deactivateTemplate_AsAdmin_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/api/v1/templates/1/deactivate").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
