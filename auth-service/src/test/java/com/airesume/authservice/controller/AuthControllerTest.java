package com.airesume.authservice.controller;

import com.airesume.authservice.dto.LoginRequest;
import com.airesume.authservice.dto.RegisterRequest;
import com.airesume.authservice.dto.PasswordResetInitiateRequest;
import com.airesume.authservice.dto.OtpVerificationRequest;
import com.airesume.authservice.dto.UserProfileResponse;
import com.airesume.authservice.dto.ProfileRequest;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web Layer Tests for AuthController.
 */
@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = true)
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

    /**
     * Tests the initiation of user registration (Step 1).
     */
    @Test
    void initiateRegistration_ShouldReturnSuccess() throws Exception {
        com.airesume.authservice.dto.RegisterInitiateRequest request = new com.airesume.authservice.dto.RegisterInitiateRequest("Test Name", 25, "+919999999999", "test@example.com");
        when(authService.initiateRegistration(any())).thenReturn("OTP sent");
        mockMvc.perform(post("/api/v1/auth/register/initiate").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Tests the verification of registration OTP (Step 2).
     */
    @Test
    void verifyRegistrationOtp_ShouldReturnSuccess() throws Exception {
        OtpVerificationRequest request = new OtpVerificationRequest("test@e.com", "123456", null);
        when(authService.verifyRegistrationOtp(anyString(), anyString())).thenReturn("Verified");
        mockMvc.perform(post("/api/v1/auth/register/verify-otp").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Tests final user registration after OTP verification (Step 3).
     */
    @Test
    void register_ShouldReturnSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password@123")
                .fullName("Test User")
                .age(25)
                .mobileNumber("+919999999999")
                .otp("123456")
                .build();
        when(authService.register(any())).thenReturn("User registered successfully");
        mockMvc.perform(post("/api/v1/auth/register").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that the login endpoint returns a 200 OK status on success.
     */
    @Test
    void login_ShouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest("testuser", "Password@123");
        when(authService.login(any())).thenReturn("mock-token");
        mockMvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that an authenticated user can retrieve their profile.
     */
    @Test
    @WithMockUser(username = "testuser")
    void getProfile_ShouldReturnProfile() throws Exception {
        UserProfileResponse response = UserProfileResponse.builder().username("testuser").build();
        when(authService.getUserProfile("testuser")).thenReturn(response);
        mockMvc.perform(get("/api/v1/auth/profile")).andExpect(status().isOk());
    }

    /**
     * Tests the password reset initiation flow.
     */
    @Test
    void initiatePasswordReset_ShouldReturnSuccess() throws Exception {
        PasswordResetInitiateRequest request = new PasswordResetInitiateRequest("test@e.com");
        when(authService.initiatePasswordReset(any())).thenReturn("OTP sent");
        mockMvc.perform(post("/api/v1/auth/forgot-password/initiate").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that a user can update their own profile details.
     */
    @Test
    @WithMockUser(username = "testuser")
    void updateProfile_Success() throws Exception {
        when(authService.updateProfile(eq("testuser"), any())).thenReturn("Updated");
        mockMvc.perform(put("/api/v1/auth/profile").with(csrf()).contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"New Name\"}"))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that a valid JWT token can be parsed to retrieve the associated username.
     */
    @Test
    void validateToken_Success() throws Exception {
        when(authService.validateToken(anyString())).thenReturn("testuser");
        mockMvc.perform(get("/api/v1/auth/validate").param("token", "t")).andExpect(status().isOk());
    }

    /**
     * Tests the token refresh endpoint for authenticated users.
     */
    @Test
    @WithMockUser(username = "testuser")
    void refreshToken_Success() throws Exception {
        when(authService.refreshToken("testuser")).thenReturn("newToken");
        mockMvc.perform(get("/api/v1/auth/refresh")).andExpect(status().isOk());
    }

    /**
     * Tests the username recovery initiation.
     */
    @Test
    void initiateUsernameRecovery_Success() throws Exception {
        com.airesume.authservice.dto.UsernameRecoveryRequest req = new com.airesume.authservice.dto.UsernameRecoveryRequest("test@test.com", "password");
        when(authService.initiateUsernameRecovery(any())).thenReturn("OTP sent");
        mockMvc.perform(post("/api/v1/auth/forgot-username/initiate").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that the username recovery OTP is correctly validated.
     */
    @Test
    void verifyUsernameRecovery_Success() throws Exception {
        OtpVerificationRequest req = new OtpVerificationRequest("test@test.com", "123456", null);
        when(authService.verifyUsernameRecovery(any())).thenReturn("Username sent");
        mockMvc.perform(post("/api/v1/auth/forgot-username/verify").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    /**
     * Tests the final password reset after OTP verification.
     */
    @Test
    void resetPassword_Success() throws Exception {
        OtpVerificationRequest req = new OtpVerificationRequest("test@test.com", "123456", "Password@123");
        when(authService.resetPassword(any())).thenReturn("Password reset successful");
        mockMvc.perform(post("/api/v1/auth/forgot-password/verify").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    /**
     * Verifies that a user can permanently delete their own account.
     */
    @Test
    @WithMockUser(username = "testuser")
    void deleteOwnAccount_Success() throws Exception {
        when(authService.deleteOwnAccount("testuser")).thenReturn("Deleted");
        mockMvc.perform(delete("/api/v1/auth/profile").with(csrf())).andExpect(status().isOk());
    }
}
