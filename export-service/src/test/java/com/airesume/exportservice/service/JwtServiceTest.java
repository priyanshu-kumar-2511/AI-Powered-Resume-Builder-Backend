package com.airesume.exportservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService in Export Service.
 * Verifies claim extraction and robust token validation including expiration and signature checks.
 */
class JwtServiceTest {

    private JwtService jwtService;
    // 256-bit secret key for testing
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateToken(String username, Long userId, List<String> roles, String plan) {
        return Jwts.builder()
                .claims(Map.of("userId", userId, "roles", roles, "subscriptionPlan", plan))
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Verifies successful extraction of the username from a valid token.
     */
    @Test
    void testExtractUsername() {
        String token = generateToken("user@example.com", 1L, List.of("ROLE_USER"), "FREE");
        assertEquals("user@example.com", jwtService.extractUsername(token));
    }

    /**
     * Verifies successful extraction of the user ID from a valid token.
     */
    @Test
    void testExtractUserId() {
        String token = generateToken("user@example.com", 1L, List.of("ROLE_USER"), "FREE");
        assertEquals(1L, jwtService.extractUserId(token));
    }

    /**
     * Verifies successful extraction of roles from a valid token.
     */
    @Test
    void testExtractRoles() {
        String token = generateToken("user@example.com", 1L, List.of("ROLE_USER", "ROLE_ADMIN"), "FREE");
        List<String> roles = jwtService.extractRoles(token);
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    /**
     * Verifies successful extraction of the subscription plan from a valid token.
     */
    @Test
    void testExtractPlan() {
        String token = generateToken("user@example.com", 1L, List.of("ROLE_USER"), "PREMIUM");
        assertEquals("PREMIUM", jwtService.extractPlan(token));
    }

    /**
     * Verifies that a properly signed, non-expired token is considered valid.
     */
    @Test
    void testValidateToken_Valid() {
        String token = generateToken("user@example.com", 1L, List.of("ROLE_USER"), "FREE");
        assertTrue(jwtService.validateToken(token));
    }

    /**
     * Verifies that tokens with tampered signatures are rejected.
     */
    @Test
    void testValidateToken_InvalidSignature() {
        String token = generateToken("user@example.com", 1L, List.of("ROLE_USER"), "FREE");
        token += "invalid";
        assertFalse(jwtService.validateToken(token));
    }

    /**
     * Verifies that malformed token strings are rejected.
     */
    @Test
    void testValidateToken_Malformed() {
        assertFalse(jwtService.validateToken("not.a.real.token"));
    }

    /**
     * Verifies that tokens are rejected once they surpass their expiration timestamp.
     */
    @Test
    void testValidateToken_Expired() {
        String expiredToken = Jwts.builder()
                .subject("user@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 20000))
                .expiration(new Date(System.currentTimeMillis() - 10000)) // expired in the past
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
        
        assertFalse(jwtService.validateToken(expiredToken));
    }

    /**
     * Verifies user ID extraction across various data types and edge cases (nulls, blanks, non-numeric strings).
     */
    @Test
    void testExtractUserId_EdgeCases() {
        // String type but blank
        String blankToken = Jwts.builder()
                .claims(Map.of("userId", "  "))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
        assertNull(jwtService.extractUserId(blankToken));

        // Unsupported type (Boolean)
        String booleanToken = Jwts.builder()
                .claims(Map.of("userId", true))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
        assertNull(jwtService.extractUserId(booleanToken));

        // String type valid
        String stringToken = Jwts.builder()
                .claims(Map.of("userId", "999"))
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
        assertEquals(999L, jwtService.extractUserId(stringToken));

        // Null userId
        String nullToken = Jwts.builder()
                .claims(Map.of("otherClaim", "value")) // non-empty claims map, but no userId
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
        assertNull(jwtService.extractUserId(nullToken));
    }
}

