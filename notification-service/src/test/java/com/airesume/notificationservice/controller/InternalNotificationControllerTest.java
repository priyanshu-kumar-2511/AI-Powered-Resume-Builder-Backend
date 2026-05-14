package com.airesume.notificationservice.controller;

import com.airesume.notificationservice.dto.EmailRequest;
import com.airesume.notificationservice.service.EmailService;
import com.airesume.notificationservice.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the Internal Notification Controller.
 * Verifies inter-service email dispatch requests and payload validation.
 */
@WebMvcTest(InternalNotificationController.class)
class InternalNotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Verifies that internal services can trigger email delivery via the notification microservice.
     */
    @Test
    @WithMockUser
    void testSendEmail() throws Exception {
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test");
        request.setBody("Body");

        mockMvc.perform(post("/internal/email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
