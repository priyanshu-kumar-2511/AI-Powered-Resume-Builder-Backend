package com.airesume.ai.controller;

import com.airesume.ai.dto.AiRequest;
import com.airesume.ai.security.CurrentUserService;
import com.airesume.ai.service.AiService;
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

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
public class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AiService aiService;

    @MockBean
    private CurrentUserService currentUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("API: POST /generate-summary - Should return generated summary")
    void generateSummary_ShouldReturnSummary() throws Exception {
        AiRequest request = new AiRequest();
        request.setTargetJobTitle("Developer");

        when(currentUserService.requireUserIdAsString()).thenReturn("1");
        when(aiService.generateSummary(any())).thenReturn(Map.of("summary", "Experienced developer..."));

        mockMvc.perform(post("/generate-summary")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary").value("Experienced developer..."));
    }

    @Test
    @DisplayName("API: POST /check-ats - Should return ATS score")
    void checkAts_ShouldReturnScore() throws Exception {
        AiRequest request = new AiRequest();
        
        when(currentUserService.requireUserIdAsString()).thenReturn("1");
        when(aiService.checkAtsCompatibility(any())).thenReturn(Map.of("score", 85));

        mockMvc.perform(post("/check-ats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(85));
    }
}
