package com.airesume.authservice.service;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.model.*;
import com.airesume.authservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AuthService in auth-service.
 * Technology: JUnit 5 + Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserQuotaRepository userQuotaRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("hashedPassword")
                .isActive(true)
                .roles(Collections.singleton(new Role(1, "ROLE_USER")))
                .build();

        registerRequest = RegisterRequest.builder()
                .username("newuser")
                .email("new@example.com")
                .password("RawPass123!")
                .fullName("New User")
                .age(25)
                .mobileNumber("+919999999999")
                .build();
    }

    @Test
    @DisplayName("Test: Register User - Success")
    void register_Success() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1, "ROLE_USER")));
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        
        String result = authService.register(registerRequest);

        assertEquals("User registered successfully", result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userQuotaRepository, times(1)).save(any(UserQuota.class));
    }

    @Test
    @DisplayName("Test: User Login - Success")
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "correctPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctPass", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken(eq("testuser"), any(Map.class))).thenReturn("mockJwtToken");

        String token = authService.login(loginRequest);

        assertEquals("mockJwtToken", token);
    }

    @Test
    @DisplayName("Test: Initiate Username Recovery")
    void initiateUsernameRecovery_Success() {
        UsernameRecoveryRequest request = new UsernameRecoveryRequest("test@example.com", "correctPass");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctPass", "hashedPassword")).thenReturn(true);
        when(otpService.generateAndSaveOtp(any(User.class), any(VerificationOtp.OtpType.class))).thenReturn("123456");
        
        String result = authService.initiateUsernameRecovery(request);

        assertEquals("Recovery OTP sent to your email", result);
        verify(emailService, times(1)).sendOtpEmail(eq("test@example.com"), eq("123456"), anyString());
    }

    @Test
    @DisplayName("Test: Verify Username Recovery")
    void verifyUsernameRecovery_Success() {
        OtpVerificationRequest request = new OtpVerificationRequest("test@example.com", "123456", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(any(User.class), eq("123456"), any(VerificationOtp.OtpType.class))).thenReturn(true);

        String result = authService.verifyUsernameRecovery(request);

        assertEquals("Username has been sent to your registered email", result);
        verify(emailService, times(1)).sendUsernameEmail("test@example.com", "testuser");
    }

    @Test
    @DisplayName("Test: Deactivate Account")
    void deactivateAccount_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        String result = authService.deactivateAccount("testuser");

        assertEquals("Account deactivated successfully", result);
        assertFalse(testUser.isActive());
    }
}
