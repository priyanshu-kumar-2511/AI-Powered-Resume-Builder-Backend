package com.airesume.sectionservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService in Section Service.
 * Verifies claim extraction and token validation logic.
 */
public class JwtServiceTest {

    private JwtService jwtService;
    // A valid base64 encoded 256-bit key
    private final String jwtSecret = "Wk11NnlOQmQ1bEFvSHVTS0Iwa1dNb0dJSEJwYmdNdkRYRzRjTVpYd1A1Zz0=";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", jwtSecret);
    }

    private String generateTestToken(String username, Long userId) {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret);
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", List.of("ROLE_USER"))
                .claim("subscriptionPlan", "FREE")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 3600000L))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    private String generateExpiredToken() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret);
        return Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000L))
                .expiration(new Date(System.currentTimeMillis() - 3600000L))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }

    /**
     * Verifies successful extraction of the username (subject) from a valid token.
     */
    @Test
    void extractUsername_ShouldReturnUsername() {
        String token = generateTestToken("testuser", 1L);
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    /**
     * Verifies that user roles are correctly extracted from the token's claims.
     */
    @Test
    void extractRoles_ShouldReturnRoles() {
        String token = generateTestToken("testuser", 1L);
        List<String> roles = jwtService.extractRoles(token);
        assertNotNull(roles);
        assertTrue(roles.contains("ROLE_USER"));
    }

    /**
     * Verifies successful extraction of the user ID when stored as a numeric value.
     */
    @Test
    void extractUserId_ShouldReturnUserId() {
        String token = generateTestToken("testuser", 1L);
        Long userId = jwtService.extractUserId(token);
        assertEquals(1L, userId);
    }

    /**
     * Verifies that the user ID can be extracted even if it's stored as a string in the token.
     */
    @Test
    void extractUserId_WithStringVar_ShouldReturnUserId() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret);
        String token = Jwts.builder()
                .claim("userId", "2")
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
        Long userId = jwtService.extractUserId(token);
        assertEquals(2L, userId);
    }

    /**
     * Verifies successful extraction of the subscription plan from the token's claims.
     */
    @Test
    void extractPlan_ShouldReturnPlan() {
        String token = generateTestToken("testuser", 1L);
        String plan = jwtService.extractPlan(token);
        assertEquals("FREE", plan);
    }

    /**
     * Verifies that a valid token passes the validation check.
     */
    @Test
    void validateToken_ShouldReturnTrue() {
        String token = generateTestToken("testuser", 1L);
        assertTrue(jwtService.validateToken(token));
    }

    /**
     * Verifies that expired tokens fail the validation check.
     */
    @Test
    void validateToken_ShouldReturnFalse_WhenExpired() {
        String token = generateExpiredToken();
        assertFalse(jwtService.validateToken(token));
    }

    /**
     * Verifies that custom claims can be extracted using specific claim retrieval functions.
     */
    @Test
    void extractClaim_ShouldExtractCustomClaim() {
        String token = generateTestToken("testuser", 1L);
        String sub = jwtService.extractClaim(token, Claims::getSubject);
        assertEquals("testuser", sub);
    }

    /**
     * Verifies that missing user ID claims result in a null return.
     */
    @Test
    void extractUserId_ShouldReturnNull_WhenMissing() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret);
        String token = Jwts.builder()
                .subject("testuser")
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
        assertNull(jwtService.extractUserId(token));
    }

    /**
     * Verifies that blank user ID strings result in a null return.
     */
    @Test
    void extractUserId_ShouldReturnNull_WhenBlankString() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret);
        String token = Jwts.builder()
                .subject("testuser")
                .claim("userId", "   ")
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
        assertNull(jwtService.extractUserId(token));
    }

    /**
     * Verifies that unsupported data types for user ID result in a null return.
     */
    @Test
    void extractUserId_ShouldReturnNull_WhenUnsupportedType() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret);
        String token = Jwts.builder()
                .subject("testuser")
                .claim("userId", true)
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
        assertNull(jwtService.extractUserId(token));
    }
}
