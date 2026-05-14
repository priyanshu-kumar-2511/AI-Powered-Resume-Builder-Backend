package com.airesume.authservice.service;

import com.airesume.authservice.model.PlanType;
import com.airesume.authservice.model.Role;
import com.airesume.authservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService in Auth Service.
 * Verifies token generation, extraction, and validation logic.
 */
class JwtServiceTest {

    private JwtService jwtService;
    // Base64 encoded 256-bit key
    private final String secret = "dGhpcy1pcy1hLXZlcnktc2VjdXJlLXNlY3JldC1rZXktMzItY2hhcnMh"; 

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
    }

    /**
     * Verifies that the service can generate a token from a username and claims, and correctly extract the username back.
     */
    @Test
    void testGenerateAndExtractToken() {
        String username = "testuser";
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ADMIN");

        String token = jwtService.generateToken(username, claims);
        assertNotNull(token);

        assertEquals(username, jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, username));
        assertTrue(jwtService.validateToken(token));
    }

    /**
     * Verifies that a valid token can be generated directly from a User entity, including their roles and plan.
     */
    @Test
    void testGenerateTokenForUser() {
        User user = User.builder()
                .id(1L)
                .username("realuser")
                .roles(Collections.singleton(new Role(1, "ROLE_USER")))
                .subscriptionPlan(PlanType.FREE)
                .build();

        String token = jwtService.generateTokenForUser(user);
        assertNotNull(token);
        assertEquals("realuser", jwtService.extractUsername(token));
    }

    /**
     * Verifies that the service correctly identifies and rejects malformed token strings.
     */
    @Test
    void testInvalidToken() {
        assertFalse(jwtService.validateToken("invalid-token-string"));
    }

    /**
     * Verifies that a token generated for one user is considered invalid when checked against a different username.
     */
    @Test
    void testTokenWithWrongUsername() {
        String token = jwtService.generateToken("user1");
        assertFalse(jwtService.isTokenValid(token, "user2"));
    }
}
