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
import java.util.HashSet;
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
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserQuotaRepository userQuotaRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OtpRepository otpRepository;

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
                .fullName("Test User")
                .isActive(true)
                .roles(new HashSet<>(Collections.singleton(new Role(1, "ROLE_USER"))))
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
    @DisplayName("Test: Initiate Registration - Success")
    void initiateRegistration_Success() {
        RegisterInitiateRequest req = new RegisterInitiateRequest("New User", 25, "+919999999999", "new@example.com");
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1, "ROLE_USER")));
        when(otpService.generateAndSaveOtp(any(User.class), eq(VerificationOtp.OtpType.REGISTRATION))).thenReturn("123456");

        String result = authService.initiateRegistration(req);

        assertEquals("Verification OTP sent to your email", result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendOtpEmail(eq("new@example.com"), eq("123456"), anyString());
    }

    @Test
    @DisplayName("Test: Initiate Registration - Inactive User Cleanup")
    void initiateRegistration_InactiveUserCleanup() {
        RegisterInitiateRequest req = new RegisterInitiateRequest("New User", 25, "+919999999999", "new@example.com");
        User inactiveUser = User.builder().id(2L).email("new@example.com").isActive(false).build();
        
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(inactiveUser));
        when(userQuotaRepository.findByUserId(2L)).thenReturn(Optional.of(new UserQuota()));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1, "ROLE_USER")));
        when(otpService.generateAndSaveOtp(any(User.class), eq(VerificationOtp.OtpType.REGISTRATION))).thenReturn("123456");

        String result = authService.initiateRegistration(req);

        assertEquals("Verification OTP sent to your email", result);
        verify(userRepository).delete(inactiveUser);
        verify(userQuotaRepository).delete(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Test: Verify Registration OTP - Success")
    void verifyRegistrationOtp_Success() {
        User inactiveUser = User.builder().email("new@example.com").isActive(false).build();
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(inactiveUser));
        when(otpService.validateOtp(any(User.class), eq("123456"), eq(VerificationOtp.OtpType.REGISTRATION))).thenReturn(true);

        String result = authService.verifyRegistrationOtp("new@example.com", "123456");

        assertEquals("OTP verified successfully", result);
    }

    @Test
    @DisplayName("Test: Verify Registration OTP - Active User")
    void verifyRegistrationOtp_ActiveUser() {
        User activeUser = User.builder().email("new@example.com").isActive(true).build();
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(activeUser));

        assertThrows(RuntimeException.class, () -> authService.verifyRegistrationOtp("new@example.com", "123456"));
    }

    @Test
    @DisplayName("Test: Verify Registration OTP - Session Not Found")
    void verifyRegistrationOtp_NotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.verifyRegistrationOtp("none@e.com", "123456"));
    }

    @Test
    @DisplayName("Test: Verify Registration OTP - Invalid OTP")
    void verifyRegistrationOtp_InvalidOtp() {
        User inactiveUser = User.builder().email("new@example.com").isActive(false).build();
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(inactiveUser));
        when(otpService.validateOtp(any(), any(), any())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.verifyRegistrationOtp("new@example.com", "wrong"));
    }

    @Test
    @DisplayName("Test: Register User (Step 3) - Success")
    void register_Success() {
        registerRequest.setOtp("123456");
        User inactiveUser = User.builder().email("new@example.com").isActive(false).build();
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(inactiveUser));
        when(otpService.validateOtp(any(User.class), eq("123456"), eq(VerificationOtp.OtpType.REGISTRATION))).thenReturn(true);
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("RawPass123!")).thenReturn("hashedPassword");

        String result = authService.register(registerRequest);

        assertEquals("User registered successfully", result);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userQuotaRepository, times(1)).save(any(UserQuota.class));
    }

    @Test
    @DisplayName("Test: Register User (Step 3) - Session Not Found")
    void register_SessionNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("Test: Register User (Step 3) - Already Active")
    void register_AlreadyActive() {
        User activeUser = User.builder().email("new@example.com").isActive(true).build();
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(activeUser));
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
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

    @Test
    @DisplayName("Test: Login - Incorrect Password")
    void login_IncorrectPassword() {
        LoginRequest loginRequest = new LoginRequest("testuser", "wrongPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPass", "hashedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("Test: Login - Suspended Account")
    void login_SuspendedAccount() {
        testUser.setActive(false);
        LoginRequest loginRequest = new LoginRequest("testuser", "correctPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("correctPass", "hashedPassword")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("ACCOUNT_SUSPENDED", ex.getMessage());
    }

    @Test
    @DisplayName("Test: Register - Username Taken")
    void register_UsernameTaken() {
        registerRequest.setOtp("123456");
        User inactiveUser = User.builder().email("new@example.com").isActive(false).build();
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(inactiveUser));
        when(otpService.validateOtp(any(User.class), eq("123456"), eq(VerificationOtp.OtpType.REGISTRATION))).thenReturn(true);
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    @Test
    @DisplayName("Test: Update Profile")
    void updateProfile_Success() {
        ProfileRequest request = ProfileRequest.builder()
                .fullName("New Name")
                .mobileNumber("+919999999999")
                .age(30)
                .build();
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        String result = authService.updateProfile("testuser", request);

        assertEquals("Profile updated successfully", result);
        assertEquals("New Name", testUser.getFullName());
    }

    @Test
    @DisplayName("Test: Delete Own Account")
    void deleteOwnAccount_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userQuotaRepository.findByUserId(1L)).thenReturn(Optional.of(new UserQuota()));

        String result = authService.deleteOwnAccount("testuser");

        assertEquals("Account permanently deleted successfully", result);
        verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("Test: Change Password - Success")
    void changePassword_Success() {
        PasswordChangeRequest request = new PasswordChangeRequest("oldPass", "newPass");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", "hashedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHashed");

        String result = authService.changePassword("testuser", request);

        assertEquals("Password changed successfully", result);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Test: Update Subscription - Success")
    void updateSubscription_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        String result = authService.updateSubscription("testuser", PlanType.PREMIUM);

        assertEquals("Subscription updated to PREMIUM", result);
        assertEquals(PlanType.PREMIUM, testUser.getSubscriptionPlan());
    }

    @Test
    @DisplayName("Test: Admin - Suspend User")
    void suspendUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        String result = authService.suspendUserById(1L, "Bad behavior");

        assertTrue(result.contains("suspended"));
        assertFalse(testUser.isActive());
        verify(emailService).sendSuspensionEmail(anyString(), anyString(), eq("Bad behavior"));
    }

    @Test
    @DisplayName("Test: Admin - Reactivate User")
    void reactivateUserById_Success() {
        testUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        String result = authService.reactivateUserById(1L);

        assertTrue(result.contains("reactivated"));
        assertTrue(testUser.isActive());
    }

    @Test
    @DisplayName("Test: Admin - Update Role")
    void updateUserRoleById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(new Role(2, "ROLE_ADMIN")));

        String result = authService.updateUserRoleById(1L, "ROLE_ADMIN");

        assertTrue(result.contains("role updated"));
        verify(emailService).sendAdminPromotionEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Test: Audit Logs")
    void getAuditLogs_Success() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));

        var logs = authService.getAuditLogs();

        assertFalse(logs.isEmpty());
        assertEquals("USER_REGISTERED", logs.get(0).get("actionType"));
    }

    @Test
    @DisplayName("Test: Admin - Reactivate User already active - still succeeds")
    void reactivateUserById_AlreadyActive() {
        testUser.setActive(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        String result = authService.reactivateUserById(1L);

        assertTrue(result.contains("reactivated"));
        assertTrue(testUser.isActive());
    }

    @Test
    @DisplayName("Test: Admin - Update Role - Invalid Role")
    void updateUserRoleById_InvalidRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_INVALID")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.updateUserRoleById(1L, "ROLE_INVALID"));
        assertTrue(ex.getMessage().contains("Role not found"));
    }

    @Test
    @DisplayName("Test: Initiate Password Reset")
    void initiatePasswordReset_Success() {
        PasswordResetInitiateRequest request = new PasswordResetInitiateRequest("test@example.com");
        when(userRepository.findByUsernameOrEmail("test@example.com", "test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.generateAndSaveOtp(any(), any())).thenReturn("654321");

        String result = authService.initiatePasswordReset(request);

        assertTrue(result.contains("OTP") || result.contains("sent"));
        verify(emailService).sendOtpEmail(eq("test@example.com"), eq("654321"), anyString());
    }

    @Test
    @DisplayName("Test: Reset Password via OTP")
    void resetPassword_Success() {
        OtpVerificationRequest request = new OtpVerificationRequest("test@example.com", "654321", "newPass123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(any(), eq("654321"), any())).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("newHashedPass");

        String result = authService.resetPassword(request);

        assertEquals("Password reset successful", result);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Test: Admin - Update Plan by ID - Success")
    void updateSubscriptionById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        String result = authService.updateSubscriptionById(1L, PlanType.PREMIUM);

        assertTrue(result.contains("PREMIUM"));
        assertEquals(PlanType.PREMIUM, testUser.getSubscriptionPlan());
        verify(emailService).sendPremiumActivationEmail(any(), any());
    }

    @Test
    @DisplayName("Test: Admin - Update Plan by Username - Free Plan case")
    void updateSubscriptionByUsername_Free() {
        testUser.setSubscriptionPlan(PlanType.PREMIUM);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        String result = authService.updateSubscriptionByUsername("testuser", PlanType.FREE);

        assertTrue(result.contains("FREE"));
        assertEquals(PlanType.FREE, testUser.getSubscriptionPlan());
        verify(emailService).sendPremiumCancellationEmail(any(), any());
    }

    @Test
    @DisplayName("Test: Admin - Update Role - ROLE_USER demotion email")
    void updateUserRoleById_Demotion() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1, "ROLE_USER")));

        authService.updateUserRoleById(1L, "ROLE_USER");

        verify(emailService).sendAdminDemotionEmail(any(), any());
    }

    @Test
    @DisplayName("Test: Audit Logs - Suspended user")
    void getAuditLogs_Suspended() {
        testUser.setActive(false);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));

        var logs = authService.getAuditLogs();

        assertEquals("USER_SUSPENDED", logs.get(0).get("actionType"));
    }
    @Test
    @DisplayName("Test: Validate Token - Success")
    void validateToken_Success() {
        when(jwtService.validateToken("validToken")).thenReturn(true);
        when(jwtService.extractUsername("validToken")).thenReturn("testuser");

        String result = authService.validateToken("validToken");

        assertEquals("testuser", result);
    }

    @Test
    @DisplayName("Test: Validate Token - Invalid")
    void validateToken_Invalid() {
        when(jwtService.validateToken("invalidToken")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.validateToken("invalidToken"));
    }

    @Test
    @DisplayName("Test: Refresh Token - Success")
    void refreshToken_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(eq("testuser"), any())).thenReturn("newToken");

        String result = authService.refreshToken("testuser");

        assertEquals("newToken", result);
    }

    @Test
    @DisplayName("Test: Get All Users")
    void getAllUsers_Success() {
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));

        var result = authService.getAllUsers();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Test: Update User Status")
    void updateUserStatus_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        String result = authService.updateUserStatus("testuser", false);

        assertFalse(testUser.isActive());
        assertEquals("User status updated successfully", result);
    }

    @Test
    @DisplayName("Test: Update User Role by Username")
    void updateUserRole_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(new Role(2, "ROLE_ADMIN")));

        String result = authService.updateUserRole("testuser", "ROLE_ADMIN");

        assertEquals("User role updated successfully", result);
        assertTrue(testUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Test: Get Users by Role")
    void getUsersByRole_Success() {
        when(userRepository.findAllByRoles_Name("ROLE_USER")).thenReturn(Collections.singletonList(testUser));

        var result = authService.getUsersByRole("ROLE_USER");

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Test: Get Users by Plan")
    void getUsersByPlan_Success() {
        when(userRepository.findBySubscriptionPlan(PlanType.FREE)).thenReturn(Collections.singletonList(testUser));

        var result = authService.getUsersByPlan(PlanType.FREE);

        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Test: Delete User Permanently")
    void deleteUserPermanently_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userQuotaRepository.findByUserId(1L)).thenReturn(Optional.of(new UserQuota()));

        String result = authService.deleteUserPermanently(1L);

        assertEquals("User permanently deleted", result);
        verify(userRepository).delete(testUser);
        verify(userQuotaRepository).delete(any());
    }

    @Test
    @DisplayName("Test: Update Subscription By Username - Premium")
    void updateSubscriptionByUsername_Premium() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        authService.updateSubscriptionByUsername("testuser", PlanType.PREMIUM);

        assertEquals(PlanType.PREMIUM, testUser.getSubscriptionPlan());
        verify(emailService).sendPremiumActivationEmail(any(), any());
    }

    @Test
    @DisplayName("Test: Get User Profile")
    void getUserProfile_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        var response = authService.getUserProfile("testuser");

        assertEquals("testuser", response.getUsername());
    }

    @Test
    @DisplayName("Test: Register - Email Taken (Initiate Step)")
    void register_EmailTaken() {
        RegisterInitiateRequest req = new RegisterInitiateRequest("New User", 25, "+919999999999", "new@example.com");
        User activeUser = User.builder().email("new@example.com").isActive(true).build();
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(activeUser));

        assertThrows(RuntimeException.class, () -> authService.initiateRegistration(req));
    }

    @Test
    @DisplayName("Test: Password Reset - OTP Invalid")
    void resetPassword_InvalidOtp() {
        OtpVerificationRequest request = new OtpVerificationRequest("test@example.com", "wrong", "newPass");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(otpService.validateOtp(any(), eq("wrong"), any())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.resetPassword(request));
    }

    @Test
    void getAuditLogs_NullTimestamp() {
        testUser.setCreatedAt(null);
        when(userRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        
        var logs = authService.getAuditLogs();
        
        assertNull(logs.get(0).get("timestamp"));
    }

    @Test
    void updateSubscriptionByUsername_OtherPlan() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        // Plan is already FREE, we don't send any email
        authService.updateSubscriptionById(1L, PlanType.FREE);
        verify(emailService, never()).sendPremiumActivationEmail(any(), any());
    }

    @Test
    void updateUserRoleById_OtherRole() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName("ROLE_OTHER")).thenReturn(Optional.of(new Role(3, "ROLE_OTHER")));
        
        authService.updateUserRoleById(1L, "ROLE_OTHER");
        
        verify(emailService, never()).sendAdminPromotionEmail(any(), any());
        verify(emailService, never()).sendAdminDemotionEmail(any(), any());
    }

    @Test
    void login_AdminSuccess() {
        LoginRequest loginRequest = new LoginRequest("testuser", "Password@123");
        testUser.setRoles(new HashSet<>(Collections.singleton(new Role(2, "ROLE_ADMIN"))));
        testUser.setPassword("encodedPassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken(anyString(), any())).thenReturn("mockToken");

        String token = authService.login(loginRequest);

        assertEquals("mockToken", token);
        verify(emailService).sendAdminLoginAlertEmail(any(), any());
    }

    @Test
    void updateSubscriptionByUsername_NullPlan() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        authService.updateSubscriptionByUsername("testuser", null);
        
        verify(emailService, never()).sendPremiumActivationEmail(any(), any());
        verify(emailService, never()).sendPremiumCancellationEmail(any(), any());
    }
}

