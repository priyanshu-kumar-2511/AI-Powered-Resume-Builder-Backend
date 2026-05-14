package com.airesume.ai.controller;

import com.airesume.ai.dto.AiRequest;
import com.airesume.ai.security.CurrentUserService;
import com.airesume.ai.service.AiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for the AI Controller.
 * Verifies that the AI endpoints correctly delegate to the AI service
 * and return expected JSON structures.
 */
@WebMvcTest(AiController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private com.airesume.ai.service.JwtService jwtService;

    @MockBean
    private com.airesume.ai.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        when(currentUserService.requireUserIdAsString()).thenReturn("user1");
    }

    /**
     * Verifies that the summary generation endpoint returns 200 OK and valid content.
     */
    @Test
    void testGenerateSummary() throws Exception {
        AiRequest request = new AiRequest();
        when(aiService.generateSummary(any())).thenReturn(Map.of("content", "summary"));

        mockMvc.perform(post("/generate-summary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("summary"));
    }

    /**
     * Verifies that the experience bullets generation endpoint returns expected content.
     */
    @Test
    void testGenerateBullets() throws Exception {
        AiRequest request = new AiRequest();
        when(aiService.generateBullets(any())).thenReturn(Map.of("content", "bullets"));

        mockMvc.perform(post("/generate-bullets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("bullets"));
    }

    /**
     * Verifies that the ATS compatibility check returns the expected score.
     */
    @Test
    void testCheckAts() throws Exception {
        AiRequest request = new AiRequest();
        when(aiService.checkAtsCompatibility(any())).thenReturn(Map.of("score", 90));

        mockMvc.perform(post("/check-ats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(90));
    }

    /**
     * Verifies skill suggestions based on a target job title.
     */
    @Test
    void testSuggestSkills() throws Exception {
        when(aiService.suggestSkills(anyLong(), anyString())).thenReturn(Collections.singletonList("Java"));

        mockMvc.perform(get("/suggest-skills/1")
                .param("jobTitle", "Developer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Java"));
    }

    /**
     * Verifies the retrieval of current AI quota for a user.
     */
    @Test
    void testGetQuota() throws Exception {
        when(aiService.getUserQuota(anyString())).thenReturn(Map.of("remaining", 10));

        mockMvc.perform(get("/quota/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remaining").value(10));
    }

    /**
     * Verifies AI cover letter generation based on resume content.
     */
    @Test
    void testGenerateCoverLetter() throws Exception {
        AiRequest request = new AiRequest();
        when(aiService.generateCoverLetter(any())).thenReturn(Map.of("content", "letter"));

        mockMvc.perform(post("/generate-cover-letter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("letter"));
    }

    /**
     * Verifies the AI-powered section improvement endpoint.
     */
    @Test
    void testImproveSection() throws Exception {
        AiRequest request = new AiRequest();
        when(aiService.improveSection(any())).thenReturn(Map.of("content", "improved"));

        mockMvc.perform(post("/improve-section")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("improved"));
    }

    /**
     * Verifies the resume tailoring endpoint which aligns a resume to a job description.
     */
    @Test
    void testTailorResume() throws Exception {
        AiRequest request = new AiRequest();
        when(aiService.tailorResume(any())).thenReturn(Map.of("status", "QUEUED"));

        mockMvc.perform(post("/tailor-resume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    /**
     * Verifies the AI-powered resume translation endpoint.
     */
    @Test
    void testTranslateResume() throws Exception {
        AiRequest request = new AiRequest();
        when(aiService.translateResume(any())).thenReturn(Map.of("status", "QUEUED"));

        mockMvc.perform(post("/translate-resume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("QUEUED"));
    }

    /**
     * Verifies retrieval of all AI generation events for a specific user.
     */
    @Test
    void testGetHistory() throws Exception {
        when(aiService.getUserHistory(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/history/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * Verifies the internal job fit analysis which compares a resume against a job description.
     */
    @Test
    void testAnalyzeJobFit() throws Exception {
        AiRequest request = new AiRequest();
        when(aiService.analyzeJobFit(any())).thenReturn(Map.of("score", 80));

        mockMvc.perform(post("/internal/analyze-job-fit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(80));
    }

    /**
     * Verifies that PDF template extraction handles multipart file uploads.
     */
    @Test
    void testExtractTemplateFromPdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy content".getBytes());
        com.airesume.ai.dto.TemplateExtractionResponse response = com.airesume.ai.dto.TemplateExtractionResponse.builder()
                .htmlLayout("<div></div>")
                .cssStyles(".css {}")
                .thumbnailUrl("data:image/jpeg;base64,abc")
                .build();
        when(aiService.extractTemplateFromPdf(any())).thenReturn(response);

        mockMvc.perform(multipart("/templates/extract-from-pdf").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.htmlLayout").value("<div></div>"));
    }

    /**
     * Verifies retrieval of global administrative AI usage statistics.
     */
    @Test
    void testGetAdminStats() throws Exception {
        when(aiService.getUsageStats()).thenReturn(Map.of("totalCalls", 100));

        mockMvc.perform(get("/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCalls").value(100));
    }

    /**
     * Verifies retrieval of detailed token-level usage statistics for cost monitoring.
     */
    @Test
    void testGetUsageStats() throws Exception {
        when(aiService.getUsageStats()).thenReturn(Map.of("totalTokens", 5000));

        mockMvc.perform(get("/admin/usage-stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTokens").value(5000));
    }

    /**
     * Verifies retrieval of AI processing costs broken down by individual users.
     */
    @Test
    void testGetCostByUser() throws Exception {
        when(aiService.getCostByUser()).thenReturn(Map.of("totalCost", 1.5));

        mockMvc.perform(get("/admin/cost-by-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCost").value(1.5));
    }
}
