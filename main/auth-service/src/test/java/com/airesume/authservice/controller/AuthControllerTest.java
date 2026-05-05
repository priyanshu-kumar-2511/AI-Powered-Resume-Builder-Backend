package com.airesume.authservice.controller;

import com.airesume.authservice.dto.LoginRequest;
import com.airesume.authservice.dto.RegisterRequest;
import com.airesume.authservice.dto.PasswordResetInitiateRequest;
import com.airesume.authservice.dto.OtpVerificationRequest;
import com.airesume.authservice.dto.UserProfileResponse;
import com.airesume.authservice.service.AuthService;
import com.airesume.authservice.service.JwtService;
import com.airesume.authservice.config.SecurityConfig;
import com.airesume.authservice.config.JwtAuthenticationFilter;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Layer Tests for AuthController.
 * Technology: JUnit 5 + Mockito + MockMvc.
 */
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private com.airesume.authservice.config.OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockitoBean
    private org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("API: POST /api/v1/auth/register - Should return success with valid data")
    void register_ShouldReturnSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password@123") // Meets complexity requirements
                .fullName("Test User")
                .age(25) // Meets min age requirement
                .mobileNumber("+919999999999") // Meets regex requirement
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn("User registered successfully");

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @DisplayName("API: POST /api/v1/auth/login - Should return token")
    void login_ShouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "Password@123");

        when(authService.login(any(LoginRequest.class))).thenReturn("mock-jwt-token");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mock-jwt-token"));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("API: GET /api/v1/auth/profile - Should return profile")
    void getProfile_ShouldReturnProfile() throws Exception {
        UserProfileResponse response = UserProfileResponse.builder()
                .username("testuser")
                .fullName("Test User")
                .email("test@example.com")
                .roles(Set.of("ROLE_USER"))
                .build();

        when(authService.getUserProfile("testuser")).thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("API: POST /api/v1/auth/forgot-password/initiate - Should return success")
    void initiatePasswordReset_ShouldReturnSuccess() throws Exception {
        PasswordResetInitiateRequest request = new PasswordResetInitiateRequest();
        request.setIdentifier("test@example.com");

        when(authService.initiatePasswordReset(any(PasswordResetInitiateRequest.class)))
                .thenReturn("OTP sent to your email");

        mockMvc.perform(post("/api/v1/auth/forgot-password/initiate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OTP sent to your email"));
    }

    @Test
    @DisplayName("API: POST /api/v1/auth/forgot-password/verify - Should return success")
    void verifyPasswordReset_ShouldReturnSuccess() throws Exception {
        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setIdentifier("test@example.com");
        request.setOtp("123456");
        request.setNewPassword("NewPassword@123");

        when(authService.resetPassword(any(OtpVerificationRequest.class)))
                .thenReturn("Password reset successful");

        mockMvc.perform(post("/api/v1/auth/forgot-password/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password reset successful"));
    }
}

