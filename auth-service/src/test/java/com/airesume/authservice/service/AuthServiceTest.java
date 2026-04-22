package com.airesume.authservice.service;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.model.Role;
import com.airesume.authservice.model.User;
import com.airesume.authservice.model.VerificationOtp;
import com.airesume.authservice.repository.RoleRepository;
import com.airesume.authservice.repository.UserRepository;
import com.airesume.authservice.repository.UserQuotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
    void register_Success() {
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1, "ROLE_USER")));
        when(passwordEncoder.encode(any())).thenReturn("hashedPassword");
        
        String result = authService.register(registerRequest);

        assertEquals("User registered successfully", result);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_UsernameExists_Fails() {
        when(userRepository.existsByUsername(any())).thenReturn(true);

        String result = authService.register(registerRequest);

        assertEquals("Username already exists", result);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("testuser", "correctPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctPass", "hashedPassword")).thenReturn(true);
        when(jwtService.generateToken("testuser")).thenReturn("mockJwtToken");

        String token = authService.login(loginRequest);

        assertEquals("mockJwtToken", token);
    }

    @Test
    void login_WrongPassword_ThrowsException() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "hashedPassword")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Incorrect Password", ex.getMessage());
    }

    @Test
    void initiateUsernameRecovery_Success() {
        UsernameRecoveryRequest request = new UsernameRecoveryRequest("test@example.com", "correctPass");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctPass", "hashedPassword")).thenReturn(true);
        when(otpService.generateAndSaveOtp(testUser, VerificationOtp.OtpType.USERNAME_RECOVERY)).thenReturn("123456");

        String result = authService.initiateUsernameRecovery(request);

        assertEquals("OTP sent to your registered email", result);
        verify(emailService, times(1)).sendOtpEmail("test@example.com", "123456", "Username Recovery");
    }

    @Test
    void verifyUsernameRecovery_Success() {
        OtpVerificationRequest request = new OtpVerificationRequest("test@example.com", "123456", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(testUser, "123456", VerificationOtp.OtpType.USERNAME_RECOVERY)).thenReturn(true);

        String result = authService.verifyUsernameRecovery(request);

        assertEquals("Username has been sent to your registered email", result);
        verify(emailService, times(1)).sendUsernameEmail("test@example.com", "testuser");
    }
}
