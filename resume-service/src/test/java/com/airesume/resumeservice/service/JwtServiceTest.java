package com.airesume.resumeservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService in Resume Service.
 * Verifies claim extraction (username, roles, userId, plan) and token validity checks.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    // A valid Base64 encoded 256-bit key for HMAC-SHA256
    private final String testSecret = "8f3a3e6b7d2c9a1f4e8b3d6c9a2f5e8b3d6c9a2f5e8b3d6c9a2f5e8b3d6c9a2f";
    private SecretKey key;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(testSecret);
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateTestToken(String subject, Map<String, Object> claims, long expirationMillis) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    /**
     * Verifies that the username (subject) can be correctly extracted.
     */
    @Test
    void extractUsername_ShouldReturnSubject() {
        String token = generateTestToken("testuser", Map.of(), 10000);
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    /**
     * Verifies that user roles are correctly extracted as a list.
     */
    @Test
    void extractRoles_ShouldReturnRolesList() {
        List<String> roles = List.of("ROLE_USER", "ROLE_PREMIUM");
        String token = generateTestToken("testuser", Map.of("roles", roles), 10000);
        
        List<String> extractedRoles = jwtService.extractRoles(token);
        assertNotNull(extractedRoles);
        assertTrue(extractedRoles.containsAll(roles));
    }

    /**
     * Verifies that the user ID can be extracted when provided as a number.
     */
    @Test
    void extractUserId_ShouldReturnId_WhenNumber() {
        String token = generateTestToken("testuser", Map.of("userId", 100), 10000);
        assertEquals(100L, jwtService.extractUserId(token));
    }

    /**
     * Verifies that the user ID can be extracted when provided as a string.
     */
    @Test
    void extractUserId_ShouldReturnId_WhenString() {
        String token = generateTestToken("testuser", Map.of("userId", "200"), 10000);
        assertEquals(200L, jwtService.extractUserId(token));
    }

    /**
     * Verifies that missing user IDs result in a null return.
     */
    @Test
    void extractUserId_ShouldReturnNull_WhenMissing() {
        String token = generateTestToken("testuser", Map.of(), 10000);
        assertNull(jwtService.extractUserId(token));
    }

    /**
     * Verifies that blank user ID strings result in a null return.
     */
    @Test
    void extractUserId_ShouldReturnNull_WhenBlankString() {
        String token = generateTestToken("testuser", Map.of("userId", "   "), 10000);
        assertNull(jwtService.extractUserId(token));
    }

    /**
     * Verifies that unsupported data types for user ID result in a null return.
     */
    @Test
    void extractUserId_ShouldReturnNull_WhenUnsupportedType() {
        String token = generateTestToken("testuser", Map.of("userId", true), 10000);
        assertNull(jwtService.extractUserId(token));
    }

    /**
     * Verifies that the subscription plan can be extracted.
     */
    @Test
    void extractPlan_ShouldReturnPlan() {
        String token = generateTestToken("testuser", Map.of("subscriptionPlan", "PREMIUM"), 10000);
        assertEquals("PREMIUM", jwtService.extractPlan(token));
    }

    /**
     * Verifies that a valid token passes the validation check.
     */
    @Test
    void validateToken_ShouldReturnTrue_WhenValid() {
        String token = generateTestToken("testuser", Map.of(), 10000);
        assertTrue(jwtService.validateToken(token));
    }

    /**
     * Verifies that expired tokens fail the validation check.
     */
    @Test
    void validateToken_ShouldReturnFalse_WhenExpired() {
        String token = generateTestToken("testuser", Map.of(), -10000);
        assertFalse(jwtService.validateToken(token));
    }

    /**
     * Verifies that tokens with invalid signatures fail the validation check.
     */
    @Test
    void validateToken_ShouldReturnFalse_WhenInvalidSignature() {
        String token = generateTestToken("testuser", Map.of(), 10000) + "invalid";
        assertFalse(jwtService.validateToken(token));
    }
}
