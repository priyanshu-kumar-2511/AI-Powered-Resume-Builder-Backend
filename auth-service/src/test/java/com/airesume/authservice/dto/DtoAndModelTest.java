package com.airesume.authservice.dto;

import com.airesume.authservice.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoAndModelTest {

    @Test
    void testRegisterRequest() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .age(25)
                .mobileNumber("1234567890")
                .email("john@example.com")
                .username("johndoe")
                .password("Password123!")
                .build();

        assertEquals("John Doe", request.getFullName());
        assertEquals(25, request.getAge());
        assertEquals("1234567890", request.getMobileNumber());
        assertEquals("john@example.com", request.getEmail());
        assertEquals("johndoe", request.getUsername());
        assertEquals("Password123!", request.getPassword());

        RegisterRequest other = new RegisterRequest();
        other.setFullName("John Doe");
        other.setAge(25);
        other.setMobileNumber("1234567890");
        other.setEmail("john@example.com");
        other.setUsername("johndoe");
        other.setPassword("Password123!");

        assertEquals(request, other);
        assertEquals(request.hashCode(), other.hashCode());
        assertNotNull(request.toString());
    }

    @Test
    void testLoginRequest() {
        LoginRequest request = LoginRequest.builder()
                .username("test")
                .password("pass")
                .build();

        assertEquals("test", request.getUsername());
        assertEquals("pass", request.getPassword());

        LoginRequest other = new LoginRequest("test", "pass");
        assertEquals(request, other);
    }

    @Test
    void testUpdatePlanRequest() {
        UpdatePlanRequest request = new UpdatePlanRequest();
        request.setUsername("testuser");
        request.setPlan(PlanType.PREMIUM);

        assertEquals("testuser", request.getUsername());
        assertEquals(PlanType.PREMIUM, request.getPlan());

        UpdatePlanRequest other = new UpdatePlanRequest("testuser", PlanType.PREMIUM);
        assertEquals(request, other);
        assertNotNull(request.toString());
    }

    @Test
    void testProfileRequest() {
        ProfileRequest request = ProfileRequest.builder()
                .fullName("Jane")
                .age(30)
                .mobileNumber("0987654321")
                .build();

        assertEquals("Jane", request.getFullName());
        assertEquals(30, request.getAge());
        assertEquals("0987654321", request.getMobileNumber());

        ProfileRequest other = new ProfileRequest("Jane", 30, "0987654321");
        assertEquals(request, other);
    }

    @Test
    void testPasswordChangeRequest() {
        PasswordChangeRequest request = PasswordChangeRequest.builder()
                .currentPassword("old")
                .newPassword("new")
                .build();

        assertEquals("old", request.getCurrentPassword());
        assertEquals("new", request.getNewPassword());

        PasswordChangeRequest other = new PasswordChangeRequest("old", "new");
        assertEquals(request, other);
    }

    @Test
    void testPasswordResetInitiateRequest() {
        PasswordResetInitiateRequest request = new PasswordResetInitiateRequest();
        request.setIdentifier("testuser");

        assertEquals("testuser", request.getIdentifier());

        PasswordResetInitiateRequest other = new PasswordResetInitiateRequest("testuser");
        assertEquals(request, other);
    }

    @Test
    void testUsernameRecoveryRequest() {
        UsernameRecoveryRequest request = new UsernameRecoveryRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        assertEquals("test@example.com", request.getEmail());
        assertEquals("password", request.getPassword());

        UsernameRecoveryRequest other = new UsernameRecoveryRequest("test@example.com", "password");
        assertEquals(request, other);
    }

    @Test
    void testUserProfileResponse() {
        UserProfileResponse response = UserProfileResponse.builder()
                .userId(1L)
                .fullName("John")
                .age(20)
                .mobileNumber("123")
                .email("john@test.com")
                .username("john")
                .isActive(true)
                .subscriptionPlan(PlanType.FREE)
                .roles(new HashSet<>())
                .build();

        assertEquals(1L, response.getUserId());
        assertEquals("John", response.getFullName());
        assertEquals(20, response.getAge());
        assertEquals("123", response.getMobileNumber());
        assertEquals("john@test.com", response.getEmail());
        assertEquals("john", response.getUsername());
        assertTrue(response.isActive());
        assertEquals(PlanType.FREE, response.getSubscriptionPlan());

        UserProfileResponse other = new UserProfileResponse();
        other.setUserId(1L);
        other.setFullName("John");
        other.setAge(20);
        other.setMobileNumber("123");
        other.setEmail("john@test.com");
        other.setUsername("john");
        other.setActive(true);
        other.setSubscriptionPlan(PlanType.FREE);
        other.setRoles(new HashSet<>());

        assertEquals(response, other);
    }

    @Test
    void testOtpVerificationRequest() {
        OtpVerificationRequest request = new OtpVerificationRequest();
        request.setIdentifier("test@test.com");
        request.setOtp("123456");
        request.setNewPassword("newpass");

        assertEquals("test@test.com", request.getIdentifier());
        assertEquals("123456", request.getOtp());
        assertEquals("newpass", request.getNewPassword());

        OtpVerificationRequest other = new OtpVerificationRequest("test@test.com", "123456", "newpass");
        assertEquals(request, other);
    }

    @Test
    void testUserEntity() throws Exception {
        User user = User.builder()
                .id(1L)
                .fullName("Admin")
                .age(40)
                .mobileNumber("123456")
                .email("admin@test.com")
                .username("admin")
                .password("adminpass")
                .isActive(true)
                .enabled(true)
                .subscriptionPlan(PlanType.PREMIUM)
                .provider(ProviderType.LOCAL)
                .roles(new HashSet<>())
                .build();

        assertNotNull(user.toString());
        
        java.lang.reflect.Method onCreate = User.class.getDeclaredMethod("onCreate");
        onCreate.setAccessible(true);
        onCreate.invoke(user);
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());

        java.lang.reflect.Method onUpdate = User.class.getDeclaredMethod("onUpdate");
        onUpdate.setAccessible(true);
        onUpdate.invoke(user);
        assertNotNull(user.getUpdatedAt());

        User other = user.toBuilder().build();
        assertEquals(user, other);
    }

    @Test
    void testRoleEntity() {
        Role role = Role.builder()
                .id(1)
                .name("ROLE_USER")
                .build();

        assertEquals(1, role.getId());
        assertEquals("ROLE_USER", role.getName());

        Role other = new Role();
        other.setId(1);
        other.setName("ROLE_USER");

        assertEquals(role, other);
        assertEquals(role.hashCode(), other.hashCode());
        assertNotNull(role.toString());
    }

    @Test
    void testUserQuotaEntity() {
        UserQuota quota = UserQuota.builder()
                .id(1L)
                .user(new User())
                .aiCallsUsed(5)
                .atsChecksUsed(2)
                .lastResetDate(LocalDateTime.now())
                .build();

        assertEquals(1L, quota.getId());
        assertNotNull(quota.getUser());
        assertEquals(5, quota.getAiCallsUsed());
        assertEquals(2, quota.getAtsChecksUsed());
        assertNotNull(quota.getLastResetDate());

        UserQuota other = quota.toBuilder().build();
        assertEquals(quota, other);
    }

    @Test
    void testVerificationOtpEntity() {
        VerificationOtp otp = VerificationOtp.builder()
                .id(1L)
                .otpCode("123456")
                .type(VerificationOtp.OtpType.PASSWORD_RESET)
                .user(new User())
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        assertEquals(1L, otp.getId());
        assertEquals("123456", otp.getOtpCode());
        assertEquals(VerificationOtp.OtpType.PASSWORD_RESET, otp.getType());
        assertNotNull(otp.getUser());
        assertNotNull(otp.getExpiryDate());
        assertFalse(otp.isExpired());

        VerificationOtp expired = otp.toBuilder()
                .expiryDate(LocalDateTime.now().minusHours(1))
                .build();
        assertTrue(expired.isExpired());

        assertEquals(otp, otp.toBuilder().build());
    }

    @Test
    void testBillingCycleEnum() {
        assertNotNull(BillingCycle.valueOf("MONTHLY"));
        assertNotNull(BillingCycle.valueOf("YEARLY"));
    }
}
