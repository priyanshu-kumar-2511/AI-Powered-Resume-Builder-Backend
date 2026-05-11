package com.airesume.authservice.dto;

import com.airesume.authservice.model.*;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ExhaustiveDtoTest {

    @Test
    void testRegisterRequest_Exhaustive() {
        RegisterRequest base = RegisterRequest.builder()
                .fullName("f").age(25).mobileNumber("+911234567890").email("e@m.com").username("user").password("Pass@123").build();
        
        assertEquals(base, base);
        assertEquals(base, base.toBuilder().build());
        
        // Test each field as null
        assertNotEquals(base, base.toBuilder().fullName(null).build());
        assertNotEquals(base, base.toBuilder().age(null).build());
        assertNotEquals(base, base.toBuilder().mobileNumber(null).build());
        assertNotEquals(base, base.toBuilder().email(null).build());
        assertNotEquals(base, base.toBuilder().username(null).build());
        assertNotEquals(base, base.toBuilder().password(null).build());
    }

    @Test
    void testUserProfileResponse_Exhaustive() {
        UserProfileResponse base = UserProfileResponse.builder()
                .userId(1L).username("u").email("e").fullName("f").age(20).mobileNumber("m")
                .subscriptionPlan(PlanType.FREE).isActive(true).build();
        
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
    }

    @Test
    void testVerificationOtp_Exhaustive() {
        VerificationOtp base = VerificationOtp.builder()
                .id(1L).otpCode("1").type(VerificationOtp.OtpType.PASSWORD_RESET).build();
        
        assertEquals(base, base);
        assertEquals(base, base.toBuilder().build());
        assertNotEquals(base, base.toBuilder().id(2L).build());
        assertNotEquals(base, base.toBuilder().otpCode(null).build());
        assertNotEquals(base, base.toBuilder().type(null).build());
    }

    @Test
    void testUserQuota_Exhaustive() {
        UserQuota base = UserQuota.builder().id(1L).aiCallsUsed(1).atsChecksUsed(1).build();
        assertEquals(base, base);
        assertNotEquals(base, base.toBuilder().id(2L).build());
        assertNotEquals(base, base.toBuilder().aiCallsUsed(2).build());
        assertNotEquals(base, base.toBuilder().atsChecksUsed(2).build());
    }

    @Test
    void testProfileRequest_Exhaustive() {
        ProfileRequest base = ProfileRequest.builder().fullName("f").age(20).mobileNumber("m").build();
        assertEquals(base, base);
        assertEquals(base, base.toBuilder().build());
        assertNotEquals(base, base.toBuilder().fullName(null).build());
        assertNotEquals(base, base.toBuilder().age(null).build());
        assertNotEquals(base, base.toBuilder().mobileNumber(null).build());
    }

    @Test
    void testOtpVerificationRequest_Exhaustive() {
        OtpVerificationRequest base = new OtpVerificationRequest("e", "o", "p");
        assertEquals(base, base);
        assertNotEquals(base, new OtpVerificationRequest(null, "o", "p"));
        assertNotEquals(base, new OtpVerificationRequest("e", null, "p"));
        assertNotEquals(base, new OtpVerificationRequest("e", "o", null));
    }

    @Test
    void testRole_Exhaustive() {
        Role base = new Role(1, "ROLE_USER");
        assertEquals(base, base);
        assertNotEquals(base, new Role(2, "ROLE_USER"));
        assertNotEquals(base, new Role(1, null));
    }

    @Test
    void testUser_Exhaustive() {
        User base = User.builder().id(1L).username("u").email("e").fullName("f").age(20).mobileNumber("m")
                .password("p").isActive(true).enabled(true).subscriptionPlan(PlanType.FREE).provider(ProviderType.LOCAL).build();
        
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
    }
}
