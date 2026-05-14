package com.airesume.authservice.dto;

import com.airesume.authservice.model.*;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Exhaustive unit tests for Authentication service DTOs and entities.
 * Verifies detailed property mapping, equality, and hashcode stability across 
 * all identity management models and their builder variations.
 */
class ExhaustiveDtoTest {

    /**
     * Verifies user registration request data mapping and builder equality.
     */
    @Test
    void testRegisterRequest_Exhaustive() {
        RegisterRequest base = RegisterRequest.builder()
                .fullName("Full Name").age(25).mobileNumber("+911234567890").email("test@example.com")
                .username("testuser").password("Pass@123").otp("123456").build();
        
        assertEquals(base, base);
        assertEquals(base, base.toBuilder().build());
        
        // Test each field as null
        assertNotEquals(base, base.toBuilder().fullName(null).build());
        assertNotEquals(base, base.toBuilder().age(null).build());
        assertNotEquals(base, base.toBuilder().mobileNumber(null).build());
        assertNotEquals(base, base.toBuilder().email(null).build());
        assertNotEquals(base, base.toBuilder().username(null).build());
        assertNotEquals(base, base.toBuilder().password(null).build());
        assertNotEquals(base, base.toBuilder().otp(null).build());
    }

    /**
     * Verifies user profile response fields and equality branches.
     */
    @Test
    void testUserProfileResponse_Exhaustive() {
        UserProfileResponse base = UserProfileResponse.builder()
                .userId(1L).username("u").email("e").fullName("f").age(20).mobileNumber("m")
                .subscriptionPlan(PlanType.FREE).isActive(true).roles(Set.of("ROLE_USER")).build();
        
        assertEquals(base, base);
        assertEquals(base, base.toBuilder().build());
        
        assertNotEquals(base, base.toBuilder().userId(2L).build());
        assertNotEquals(base, base.toBuilder().username(null).build());
        assertNotEquals(base, base.toBuilder().email(null).build());
        assertNotEquals(base, base.toBuilder().fullName(null).build());
        assertNotEquals(base, base.toBuilder().age(null).build());
        assertNotEquals(base, base.toBuilder().mobileNumber(null).build());
        assertNotEquals(base, base.toBuilder().subscriptionPlan(PlanType.PREMIUM).build());
        assertNotEquals(base, base.toBuilder().isActive(false).build());
        assertNotEquals(base, base.toBuilder().roles(null).build());
    }

    /**
     * Verifies OTP verification entity mapping and builder.
     */
    @Test
    void testVerificationOtp_Exhaustive() {
        VerificationOtp base = VerificationOtp.builder()
                .id(1L).otpCode("123456").type(VerificationOtp.OtpType.PASSWORD_RESET)
                .expiryDate(java.time.LocalDateTime.now().plusHours(1)).build();
        
        assertEquals(base, base);
        assertEquals(base, base.toBuilder().build());
        assertNotEquals(base, base.toBuilder().id(2L).build());
        assertNotEquals(base, base.toBuilder().otpCode(null).build());
        assertNotEquals(base, base.toBuilder().type(null).build());
        assertNotEquals(base, base.toBuilder().expiryDate(null).build());
    }

    /**
     * Verifies user quota entity builder and property variations.
     */
    @Test
    void testUserQuota_Exhaustive() {
        UserQuota base = UserQuota.builder().id(1L).aiCallsUsed(1).atsChecksUsed(1).build();
        assertEquals(base, base);
        assertNotEquals(base, base.toBuilder().id(2L).build());
        assertNotEquals(base, base.toBuilder().aiCallsUsed(2).build());
        assertNotEquals(base, base.toBuilder().atsChecksUsed(2).build());
    }

    /**
     * Verifies profile update request DTO fields and equality.
     */
    @Test
    void testProfileRequest_Exhaustive() {
        ProfileRequest base = ProfileRequest.builder().fullName("f").age(20).mobileNumber("m").build();
        assertEquals(base, base);
        assertEquals(base, base.toBuilder().build());
        assertNotEquals(base, base.toBuilder().fullName(null).build());
        assertNotEquals(base, base.toBuilder().age(null).build());
        assertNotEquals(base, base.toBuilder().mobileNumber(null).build());
    }

    /**
     * Verifies OTP verification request DTO fields.
     */
    @Test
    void testOtpVerificationRequest_Exhaustive() {
        OtpVerificationRequest base = new OtpVerificationRequest("e", "o", "p");
        assertEquals(base, base);
        assertNotEquals(base, new OtpVerificationRequest(null, "o", "p"));
        assertNotEquals(base, new OtpVerificationRequest("e", null, "p"));
        assertNotEquals(base, new OtpVerificationRequest("e", "o", null));
    }

    /**
     * Verifies Role entity equality and field mapping.
     */
    @Test
    void testRole_Exhaustive() {
        Role base = new Role(1, "ROLE_USER");
        assertEquals(base, base);
        assertNotEquals(base, new Role(2, "ROLE_USER"));
        assertNotEquals(base, new Role(1, null));
    }

    /**
     * Comprehensive verification of the User entity across all persistence fields.
     */
    @Test
    void testUser_Exhaustive() {
        User base = User.builder().id(1L).username("u").email("e").fullName("f").age(20).mobileNumber("m")
                .password("p").isActive(true).enabled(true).subscriptionPlan(PlanType.FREE)
                .provider(ProviderType.LOCAL).roles(Set.of(new Role(1, "ROLE_USER"))).build();
        
        assertEquals(base, base);
        assertEquals(base, base.toBuilder().build());
        assertNotEquals(base, base.toBuilder().id(2L).build());
        assertNotEquals(base, base.toBuilder().username(null).build());
        assertNotEquals(base, base.toBuilder().email(null).build());
        assertNotEquals(base, base.toBuilder().fullName(null).build());
        assertNotEquals(base, base.toBuilder().age(null).build());
        assertNotEquals(base, base.toBuilder().mobileNumber(null).build());
        assertNotEquals(base, base.toBuilder().password(null).build());
        assertNotEquals(base, base.toBuilder().isActive(false).build());
        assertNotEquals(base, base.toBuilder().enabled(false).build());
        assertNotEquals(base, base.toBuilder().subscriptionPlan(PlanType.PREMIUM).build());
        assertNotEquals(base, base.toBuilder().provider(ProviderType.GOOGLE).build());
        // roles are @EqualsAndHashCode.Exclude in User model
        assertEquals(base, base.toBuilder().roles(Set.of()).build());
    }
}
