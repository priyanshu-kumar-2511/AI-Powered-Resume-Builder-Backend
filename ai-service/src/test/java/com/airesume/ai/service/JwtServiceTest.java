package com.airesume.ai.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "dGhpcy1pcy1hLXZlcnktc2VjdXJlLXNlY3JldC1rZXktMzItY2hhcnMh"; // Same 256-bit key

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
    }

    @Test
    void testExtractClaims() {
        SecretKey key = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secret));
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", List.of("ROLE_USER"));
        claims.put("userId", 123L);
        claims.put("subscriptionPlan", "PREMIUM");

        String token = Jwts.builder()
                .claims(claims)
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        assertEquals("testuser", jwtService.extractUsername(token));
        assertEquals(List.of("ROLE_USER"), jwtService.extractRoles(token));
        assertEquals(123L, jwtService.extractUserId(token));
        assertEquals("PREMIUM", jwtService.extractPlan(token));
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.string"));
    }
}
