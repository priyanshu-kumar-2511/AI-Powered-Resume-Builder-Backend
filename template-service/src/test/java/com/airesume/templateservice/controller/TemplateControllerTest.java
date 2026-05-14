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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @MockitoBean
    private TemplateService templateService; // Mocking the business layer

    @MockitoBean
    private JwtService jwtService; // Mocking the JWT service required by SecurityConfig

    @Autowired
    private ObjectMapper objectMapper; // For converting objects to JSON

    /**
     * Verifies that all active templates are publicly accessible.
     */
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
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Modern"))
                .andExpect(jsonPath("$[0].category").value("MODERN"));
    }

    /**
     * Verifies that administrative users have the authority to create new templates.
     */
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

        mockMvc.perform(post("")
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

        mockMvc.perform(post("")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isForbidden()); // Expecting 403 Forbidden
    }

    /**
     * Verifies retrieval of a specific template by its ID, including HTML layout details.
     */
    @Test
    @DisplayName("API: GET /api/v1/templates/{id} - Should return full template layout")
    void getTemplateById_ShouldReturnFullTemplate() throws Exception {
        Template template = Template.builder()
                .templateId(1L)
                .name("Modern")
                .htmlLayout("<html></html>")
                .build();

        when(templateService.getTemplateById(1L)).thenReturn(template);

        mockMvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.htmlLayout").value("<html></html>"));
    }

    /**
     * Verifies that administrators can retrieve the complete list of all templates (active and inactive).
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("API: GET /api/v1/templates/admin - Admin should see all templates")
    void getAllTemplatesForAdmin_ShouldReturnList() throws Exception {
        Template template = Template.builder().name("Admin Template").build();
        when(templateService.getAllTemplates()).thenReturn(Arrays.asList(template));

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Admin Template"));
    }

    /**
     * Verifies filtering of templates by the FREE tier.
     */
    @Test
    @DisplayName("API: GET /api/v1/templates/free - Should return free templates")
    void getFreeTemplates_ShouldReturnList() throws Exception {
        Template template = Template.builder().name("Free").tier(Tier.FREE).build();
        when(templateService.getTemplatesByTier(Tier.FREE)).thenReturn(Arrays.asList(template));

        mockMvc.perform(get("/free"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Free"));
    }

    /**
     * Verifies filtering of templates by the PREMIUM tier.
     */
    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("API: GET /api/v1/templates/premium - User should see premium templates")
    void getPremiumTemplates_ShouldReturnList() throws Exception {
        Template template = Template.builder().name("Premium").tier(Tier.PREMIUM).build();
        when(templateService.getTemplatesByTier(Tier.PREMIUM)).thenReturn(Arrays.asList(template));

        mockMvc.perform(get("/premium"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Premium"));
    }

    /**
     * Verifies filtering of templates by their design category (e.g., MODERN, PROFESSIONAL).
     */
    @Test
    @DisplayName("API: GET /api/v1/templates/category/{category} - Should filter by category")
    void getTemplatesByCategory_ShouldReturnList() throws Exception {
        Template template = Template.builder().name("Modern").category(Category.MODERN).build();
        when(templateService.getTemplatesByCategory(Category.MODERN)).thenReturn(Arrays.asList(template));

        mockMvc.perform(get("/category/MODERN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Modern"));
    }

    /**
     * Verifies retrieval of the most popular templates based on usage metrics.
     */
    @Test
    @DisplayName("API: GET /api/v1/templates/popular - Should return popular templates")
    void getPopularTemplates_ShouldReturnList() throws Exception {
        Template template = Template.builder().name("Popular").usageCount(100L).build();
        when(templateService.getPopularTemplates()).thenReturn(Arrays.asList(template));

        mockMvc.perform(get("/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Popular"));
    }

    /**
     * Verifies that administrators can update an existing template's details.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("API: PUT /api/v1/templates/{id} - Admin should be able to update")
    void updateTemplate_AsAdmin_ShouldReturnUpdated() throws Exception {
        Template template = Template.builder().name("Updated").build();
        when(templateService.updateTemplate(eq(1L), any())).thenReturn(template);

        mockMvc.perform(put("/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(template)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    /**
     * Verifies that administrators can deactivate a template to hide it from users.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("API: PUT /api/v1/templates/{id}/deactivate - Admin should be able to deactivate")
    void deactivateTemplate_AsAdmin_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/1/deactivate").with(csrf()))
                .andExpect(status().isNoContent());
    }

    /**
     * Verifies that the global usage count for a template can be incremented.
     */
    @Test
    @DisplayName("API: PUT /api/v1/templates/{id}/increment-usage - Should increment count")
    void incrementUsage_ShouldReturnOk() throws Exception {
        mockMvc.perform(put("/1/increment-usage").with(csrf()))
                .andExpect(status().isOk());
    }
}
