package com.airesume.authservice.dto;

import com.airesume.authservice.model.PlanType;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Authentication Data Transfer Objects (DTOs).
 * Verifies data integrity, builder patterns, and equality logic for auth-related payloads.
 */
class DtoTest {

    /**
     * Verifies the login request DTO fields and builder.
     */
    @Test
    void testLoginRequest() {
        LoginRequest req = new LoginRequest("user", "pass");
        assertEquals("user", req.getUsername());
        assertEquals("pass", req.getPassword());
        
        LoginRequest req2 = LoginRequest.builder().username("user").password("pass").build();
        assertEquals(req.getUsername(), req2.getUsername());
        assertNotNull(req.toString());
        assertEquals(req.hashCode(), req2.hashCode());
        assertTrue(req.equals(req2));
    }

    /**
     * Verifies the user registration request DTO fields.
     */
    @Test
    void testRegisterRequest() {
        RegisterRequest req = RegisterRequest.builder()
                .username("user")
                .email("a@b.com")
                .password("p")
                .fullName("fn")
                .age(20)
                .mobileNumber("123")
                .build();
        assertEquals("user", req.getUsername());
        assertEquals("a@b.com", req.getEmail());
        assertNotNull(req.toString());
        
        RegisterRequest req2 = new RegisterRequest();
        req2.setUsername("user");
        assertEquals("user", req2.getUsername());
    }

    /**
     * Verifies the user profile response DTO data mapping.
     */
    @Test
    void testUserProfileResponse() {
        UserProfileResponse res = UserProfileResponse.builder()
                .userId(1L)
                .username("u")
                .email("e")
                .fullName("f")
                .roles(Collections.singleton("R"))
                .subscriptionPlan(PlanType.FREE)
                .isActive(true)
                .build();
        assertEquals(1L, res.getUserId());
        assertNotNull(res.toString());
        
        UserProfileResponse res2 = new UserProfileResponse();
        res2.setUserId(1L);
        assertEquals(1L, res2.getUserId());
    }

    @Test
    void testOtpVerificationRequest() {
        OtpVerificationRequest req = new OtpVerificationRequest("id", "otp", "pass");
        assertEquals("id", req.getIdentifier());
        assertEquals("otp", req.getOtp());
        assertEquals("pass", req.getNewPassword());
        assertNotNull(req.toString());
    }

    @Test
    void testProfileRequest() {
        ProfileRequest req = ProfileRequest.builder().fullName("n").build();
        assertEquals("n", req.getFullName());
        assertNotNull(req.toString());
    }

    @Test
    void testUpdatePlanRequest() {
        UpdatePlanRequest req = new UpdatePlanRequest("user", PlanType.PREMIUM);
        assertEquals(PlanType.PREMIUM, req.getPlan());
        assertEquals("user", req.getUsername());
        assertNotNull(req.toString());
    }
    @Test
    void testPasswordResetInitiateRequest() {
        PasswordResetInitiateRequest req = new PasswordResetInitiateRequest("id");
        assertEquals("id", req.getIdentifier());
        assertNotNull(req.toString());
    }

    @Test
    void testPasswordChangeRequest() {
        PasswordChangeRequest req = new PasswordChangeRequest("old", "new");
        assertEquals("old", req.getCurrentPassword());
        assertEquals("new", req.getNewPassword());
        assertNotNull(req.toString());
    }

    @Test
    void testUsernameRecoveryRequest() {
        UsernameRecoveryRequest req = new UsernameRecoveryRequest("email", "pass");
        assertEquals("email", req.getEmail());
        assertEquals("pass", req.getPassword());
        assertNotNull(req.toString());
    }
    @Test
    void testEqualsAndHashCode() {
        LoginRequest l1 = new LoginRequest("u", "p");
        LoginRequest l2 = new LoginRequest("u", "p");
        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());

        RegisterRequest r1 = RegisterRequest.builder().username("u").email("e").build();
        RegisterRequest r2 = RegisterRequest.builder().username("u").email("e").build();
        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        UserProfileResponse up1 = UserProfileResponse.builder().userId(1L).build();
        UserProfileResponse up2 = UserProfileResponse.builder().userId(1L).build();
        assertEquals(up1, up2);
        assertEquals(up1.hashCode(), up2.hashCode());
    }
}
