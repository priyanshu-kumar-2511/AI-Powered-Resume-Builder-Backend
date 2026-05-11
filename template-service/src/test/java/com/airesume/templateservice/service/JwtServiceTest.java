package com.airesume.templateservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secret = Base64.getEncoder().encodeToString(Keys.secretKeyFor(SignatureAlgorithm.HS256).getEncoded());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", secret);
    }

    private String createToken(String subject, Map<String, Object> claims) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret)))
                .compact();
    }

    @Test
    void testExtractUsername() {
        String token = createToken("testuser", Map.of());
        assertEquals("testuser", jwtService.extractUsername(token));
    }

    @Test
    void testExtractRoles() {
        String token = createToken("testuser", Map.of("roles", List.of("ROLE_USER", "ROLE_ADMIN")));
        List<String> roles = jwtService.extractRoles(token);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_ADMIN"));
    }

    @Test
    void testValidateToken_Success() {
        String token = createToken("testuser", Map.of());
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void testValidateToken_Failure() {
        assertFalse(jwtService.validateToken("invalid-token"));
    }
}
