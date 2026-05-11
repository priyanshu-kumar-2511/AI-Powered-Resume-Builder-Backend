package com.airesume.authservice.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void testUser() {
        User user = User.builder()
                .id(1L)
                .username("u")
                .email("e")
                .roles(new HashSet<>())
                .build();
        assertEquals(1L, user.getId());
        assertNotNull(user.toString());
        
        User user2 = new User();
        user2.setId(1L);
        assertEquals(1L, user2.getId());
    }

    @Test
    void testRole() {
        Role role = new Role(1, "R");
        assertEquals(1, role.getId());
        assertEquals("R", role.getName());
        assertNotNull(role.toString());
        
        Role role2 = Role.builder().id(1).name("R").build();
        assertEquals(role.hashCode(), role2.hashCode());
    }

    @Test
    void testUserQuota() {
        UserQuota q = UserQuota.builder().id(1L).aiCallsUsed(10).build();
        assertEquals(1L, q.getId());
        assertEquals(10, q.getAiCallsUsed());
        assertNotNull(q.toString());
        
        UserQuota q2 = new UserQuota();
        q2.setId(1L);
        assertEquals(1L, q2.getId());
    }

    @Test
    void testVerificationOtp() {
        VerificationOtp otp = VerificationOtp.builder()
                .id(1L)
                .otpCode("123")
                .type(VerificationOtp.OtpType.PASSWORD_RESET)
                .expiryDate(LocalDateTime.now())
                .build();
        assertEquals("123", otp.getOtpCode());
        assertNotNull(otp.toString());
    }
}
