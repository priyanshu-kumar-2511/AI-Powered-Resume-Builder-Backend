package com.airesume.paymentservice.service;

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

class JwtServiceTest {

    private JwtService jwtService;
    private String secret = Base64.getEncoder().encodeToString("mysecretkeymysecretkeymysecretkeymysecretkey".getBytes());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
    }

    private String generateToken(String subject, Map<String, Object> claims, long expirationMs) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(key)
                .compact();
    }

    @Test
    void testExtractUsername() {
        String token = generateToken("user1", Map.of(), 1000 * 60);
        assertEquals("user1", jwtService.extractUsername(token));
    }

    @Test
    void testExtractRoles() {
        String token = generateToken("user1", Map.of("roles", List.of("ROLE_USER")), 1000 * 60);
        List<String> roles = jwtService.extractRoles(token);
        assertEquals(1, roles.size());
        assertEquals("ROLE_USER", roles.get(0));
    }

    @Test
    void testValidateToken_Success() {
        String token = generateToken("user1", Map.of(), 1000 * 60);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateToken_Expired() {
        String token = generateToken("user1", Map.of(), -1000);
        assertFalse(jwtService.validateToken(token));
    }

    @Test
    void testValidateToken_Invalid() {
        assertFalse(jwtService.validateToken("invalid.token.here"));
    }
}
