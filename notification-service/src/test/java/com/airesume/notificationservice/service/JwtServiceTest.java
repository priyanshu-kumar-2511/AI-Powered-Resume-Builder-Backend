package com.airesume.notificationservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService in Notification Service.
 * Verifies claim extraction and token validation logic.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private String secret = Base64.getEncoder().encodeToString("myVerySecretKeyForTestingNotificationService1234567890".getBytes());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
    }

    private String createToken(String subject, Map<String, Object> claims, long expirationMs) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    /**
     * Verifies that the username can be extracted from a valid token.
     */
    @Test
    void testExtractUsername() {
        String token = createToken("user1", Map.of(), 1000 * 60);
        assertEquals("user1", jwtService.extractUsername(token));
    }

    /**
     * Verifies that roles can be extracted from a valid token.
     */
    @Test
    void testExtractRoles() {
        String token = createToken("user1", Map.of("roles", List.of("ROLE_USER")), 1000 * 60);
        List<String> roles = jwtService.extractRoles(token);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    /**
     * Verifies that the user ID can be extracted from various claim formats (Long, String).
     */
    @Test
    void testExtractUserId() {
        String token = createToken("user1", Map.of("userId", 123L), 1000 * 60);
        assertEquals(123L, jwtService.extractUserId(token));

        String tokenStr = createToken("user1", Map.of("userId", "456"), 1000 * 60);
        assertEquals(456L, jwtService.extractUserId(tokenStr));
        
        String tokenNull = createToken("user1", Map.of(), 1000 * 60);
        assertNull(jwtService.extractUserId(tokenNull));

        String tokenBlank = createToken("user1", Map.of("userId", "   "), 1000 * 60);
        assertNull(jwtService.extractUserId(tokenBlank));

        String tokenUnsupported = createToken("user1", Map.of("userId", true), 1000 * 60);
        assertNull(jwtService.extractUserId(tokenUnsupported));
    }

    /**
     * Verifies that the subscription plan can be extracted from a valid token.
     */
    @Test
    void testExtractPlan() {
        String token = createToken("user1", Map.of("subscriptionPlan", "PREMIUM"), 1000 * 60);
        assertEquals("PREMIUM", jwtService.extractPlan(token));
    }

    /**
     * Verifies token validation logic, including expiration and malformed string handling.
     */
    @Test
    void testValidateToken() {
        String token = createToken("user1", Map.of(), 1000 * 60);
        assertTrue(jwtService.validateToken(token));

        String expiredToken = createToken("user1", Map.of(), -1000);
        assertFalse(jwtService.validateToken(expiredToken));

        assertFalse(jwtService.validateToken("invalid-token"));
    }
}
