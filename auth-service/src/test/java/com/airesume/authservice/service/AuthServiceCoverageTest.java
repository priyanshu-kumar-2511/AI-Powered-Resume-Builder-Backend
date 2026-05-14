package com.airesume.authservice.service;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.model.*;
import com.airesume.authservice.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Exhaustive edge-case unit tests for the Authentication Service.
 * Verifies failure modes, boundary conditions, and complex state transitions 
 * for account management, security tokens, and administrative overrides.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AuthServiceCoverageTest {

    @Mock
    private UserRepository userRepository;


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

    @Mock
    private UserQuotaRepository quotaRepository;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPass")
                .fullName("Test User")
                .isActive(true)
                .subscriptionPlan(PlanType.FREE)
                .provider(ProviderType.LOCAL)
                .roles(new HashSet<>(List.of(new Role(1, "ROLE_USER"))))
                .build();
    }

    /**
     * Verifies that registration fails if the default user role is missing from the system.
     */
    @Test
    void register_DefaultRoleNotFound_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .username("new")
                .email("new@test.com")
                .build();
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.register(request));
    }

    /**
     * Ensures that login attempts for suspended accounts are correctly blocked.
     */
    @Test
    void login_AccountSuspended_ThrowsException() {
        sampleUser.setActive(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        LoginRequest request = new LoginRequest("testuser", "password");
        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    /**
     * Verifies that invalid security tokens result in a validation exception.
     */
    @Test
    void validateToken_InvalidToken_ThrowsException() {
        when(jwtService.validateToken("invalidToken")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.validateToken("invalidToken"));
    }

    /**
     * Verifies successful deactivation of a user account.
     */
    @Test
    void deactivateAccount_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        String result = authService.deactivateAccount("testuser");

        assertEquals("Account deactivated successfully", result);
        assertFalse(sampleUser.isActive());
        verify(userRepository).save(sampleUser);
    }

    /**
     * Verifies that account deactivation fails if the username is not found.
     */
    @Test
    void deactivateAccount_NotFound() {
        when(userRepository.findByUsername("none")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.deactivateAccount("none"));
    }

    /**
     * Verifies that a user's subscription plan can be successfully updated.
     */
    @Test
    void updateSubscription_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        String result = authService.updateSubscription("testuser", PlanType.PREMIUM);

        assertEquals("Subscription updated to PREMIUM", result);
        assertEquals(PlanType.PREMIUM, sampleUser.getSubscriptionPlan());
    }

    /**
     * Verifies that subscription updates fail if the user account does not exist.
     */
    @Test
    void updateSubscription_NotFound() {
        when(userRepository.findByUsername("none")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.updateSubscription("none", PlanType.PREMIUM));
    }

    /**
     * Verifies that an administrator can manually update a user's active status.
     */
    @Test
    void updateUserStatus_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));

        String result = authService.updateUserStatus("testuser", false);

        assertEquals("User status updated successfully", result);
        assertFalse(sampleUser.isActive());
    }

    /**
     * Verifies that manual status updates fail if the target user is not found.
     */
    @Test
    void updateUserStatus_NotFound() {
        when(userRepository.findByUsername("none")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.updateUserStatus("none", false));
    }

    /**
     * Verifies that an administrator can successfully change a user's role.
     */
    @Test
    void updateUserRole_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        Role role = new Role(2, "ROLE_ADMIN");
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(role));

        String result = authService.updateUserRole("testuser", "ROLE_ADMIN");

        assertEquals("User role updated successfully", result);
        assertTrue(sampleUser.getRoles().contains(role));
    }

    /**
     * Verifies that role updates fail if the target user is missing.
     */
    @Test
    void updateUserRole_NotFound() {
        when(userRepository.findByUsername("none")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.updateUserRole("none", "ROLE_ADMIN"));
    }

    @Test
    void updateUserRole_RoleNotFound() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(roleRepository.findByName("INVALID")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.updateUserRole("testuser", "INVALID"));
    }

    @Test
    void getUsersByRole_Success() {
        when(userRepository.findAllByRoles_Name("ROLE_USER")).thenReturn(List.of(sampleUser));

        List<UserProfileResponse> responses = authService.getUsersByRole("ROLE_USER");

        assertEquals(1, responses.size());
        assertEquals("testuser", responses.get(0).getUsername());
    }

    @Test
    void getUsersByPlan_Success() {
        when(userRepository.findBySubscriptionPlan(PlanType.FREE)).thenReturn(List.of(sampleUser));

        List<UserProfileResponse> responses = authService.getUsersByPlan(PlanType.FREE);

        assertEquals(1, responses.size());
        assertEquals("testuser", responses.get(0).getUsername());
    }

    @Test
    void deleteUserPermanently_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        UserQuota quota = new UserQuota();
        when(quotaRepository.findByUserId(1L)).thenReturn(Optional.of(quota));

        String result = authService.deleteUserPermanently(1L);

        assertEquals("User permanently deleted", result);
        verify(quotaRepository).delete(quota);
        verify(userRepository).delete(sampleUser);
    }

    @Test
    void deleteUserPermanently_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.deleteUserPermanently(99L));
    }

    @Test
    void initiateUsernameRecovery_IncorrectPassword_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        UsernameRecoveryRequest req = new UsernameRecoveryRequest("test@example.com", "wrongPass");
        assertThrows(RuntimeException.class, () -> authService.initiateUsernameRecovery(req));
    }

    @Test
    void verifyUsernameRecovery_OtpInvalid_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(otpService.validateOtp(any(), any(), any())).thenReturn(false);

        OtpVerificationRequest req = new OtpVerificationRequest("test@example.com", "123456", null);
        assertThrows(RuntimeException.class, () -> authService.verifyUsernameRecovery(req));
    }

    @Test
    void resetPassword_OtpInvalid_ThrowsException() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(sampleUser));
        when(otpService.validateOtp(any(), any(), any())).thenReturn(false);

        OtpVerificationRequest req = new OtpVerificationRequest("test@example.com", "123456", "newPass");
        assertThrows(RuntimeException.class, () -> authService.resetPassword(req));
    }

    @Test
    void changePassword_IncorrectPassword_ThrowsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongOld", "encodedPass")).thenReturn(false);

        PasswordChangeRequest req = new PasswordChangeRequest("wrongOld", "newPass");
        assertThrows(RuntimeException.class, () -> authService.changePassword("testuser", req));
    }

    @Test
    void changePassword_UserNotFound() {
        when(userRepository.findByUsername("none")).thenReturn(Optional.empty());

        PasswordChangeRequest req = new PasswordChangeRequest("old", "new");
        assertThrows(RuntimeException.class, () -> authService.changePassword("none", req));
    }
}
