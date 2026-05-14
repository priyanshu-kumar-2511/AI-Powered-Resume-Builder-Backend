package com.airesume.ai.config;

import com.airesume.ai.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Exhaustive security and JWT integration tests.
 * Covers edge cases for JWT claim types, filter chain bypass logic,
 * and robust parsing of roles and subscription plans.
 */
class ExhaustiveSecurityTest {

    private JwtService jwtService;
    private final String secret = "mysecretkeymustbe32characterslongforhmacsha256!!";
    private String base64Secret;
    private SecretKey key;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        base64Secret = Base64.getEncoder().encodeToString(secret.getBytes());
        key = Keys.hmacShaKeyFor(secret.getBytes());
        ReflectionTestUtils.setField(jwtService, "jwtSecret", base64Secret);
        SecurityContextHolder.clearContext();
    }

    /**
     * Tests JWT user ID extraction across multiple data types (Long, String, etc.).
     */
    @Test
    void testJwtService_ExtractUserId_Variations() {
        try {
            // Case 1: Number
            java.util.Map<String, Object> claims1 = new java.util.HashMap<>();
            claims1.put("userId", 123);
            String tokenNum = Jwts.builder()
                    .setSubject("u1")
                    .setClaims(claims1)
                    .signWith(key)
                    .compact();
            assertEquals(123L, jwtService.extractUserId(tokenNum));

            // Case 2: String
            java.util.Map<String, Object> claims2 = new java.util.HashMap<>();
            claims2.put("userId", "456");
            String tokenStr = Jwts.builder()
                    .setSubject("u2")
                    .setClaims(claims2)
                    .signWith(key)
                    .compact();
            assertEquals(456L, jwtService.extractUserId(tokenStr));

            // Case 3: Null/Missing
            String tokenNull = Jwts.builder()
                    .setSubject("u3")
                    .signWith(key)
                    .compact();
            assertNull(jwtService.extractUserId(tokenNull));
            
            // Case 4: Blank String
            java.util.Map<String, Object> claims4 = new java.util.HashMap<>();
            claims4.put("userId", " ");
            String tokenBlank = Jwts.builder()
                    .setSubject("u4")
                    .setClaims(claims4)
                    .signWith(key)
                    .compact();
            assertNull(jwtService.extractUserId(tokenBlank));

            // Case 5: Unexpected Type (Boolean)
            java.util.Map<String, Object> claims5 = new java.util.HashMap<>();
            claims5.put("userId", true);
            String tokenBool = Jwts.builder()
                    .setSubject("u5")
                    .setClaims(claims5)
                    .signWith(key)
                    .compact();
            assertNull(jwtService.extractUserId(tokenBool));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Verifies token validation against expired, tampered, and valid tokens.
     */
    @Test
    void testJwtService_ValidateToken_Variations() {
        // Case 1: Valid
        String validToken = Jwts.builder()
                .subject("u")
                .expiration(new Date(System.currentTimeMillis() + 10000))
                .signWith(key)
                .compact();
        assertTrue(jwtService.validateToken(validToken));

        // Case 2: Expired
        String expiredToken = Jwts.builder()
                .subject("u")
                .expiration(new Date(System.currentTimeMillis() - 10000))
                .signWith(key)
                .compact();
        assertFalse(jwtService.validateToken(expiredToken));

        // Case 3: Invalid Signature
        String invalidToken = validToken + "tampered";
        assertFalse(jwtService.validateToken(invalidToken));
    }

    /**
     * Verifies extraction of roles and subscription plans from token claims.
     */
    @Test
    void testJwtService_ExtractRolesAndPlan() {
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("roles", java.util.List.of("ROLE_USER", "ROLE_ADMIN"));
        claims.put("subscriptionPlan", "PREMIUM");
        
        String token = Jwts.builder()
                .setSubject("u1")
                .setClaims(claims)
                .signWith(key)
                .compact();
                
        java.util.List<String> roles = jwtService.extractRoles(token);
        assertEquals(2, roles.size());
        assertTrue(roles.contains("ROLE_ADMIN"));
        assertEquals("PREMIUM", jwtService.extractPlan(token));
    }

    /**
     * Ensures the JWT filter bypasses validation if a security context already exists.
     */
    @Test
    void testJwtAuthenticationFilter_AlreadyAuthenticated() throws Exception {
        JwtService mockJwtService = mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mockJwtService);
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        // Set an existing authentication
        org.springframework.security.core.Authentication existingAuth = mock(org.springframework.security.core.Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid");
        
        filter.doFilterInternal(request, response, filterChain);
        
        // Should not call validateToken because already authenticated
        verify(mockJwtService, never()).validateToken(anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    /**
     * Tests the filter's behavior with various Authorization header formats.
     */
    @Test
    void testJwtAuthenticationFilter_HeaderVariations() throws Exception {
        JwtService mockJwtService = mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mockJwtService);
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        // Case 1: Null Header
        when(request.getHeader("Authorization")).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);

        // Case 2: Wrong Prefix
        when(request.getHeader("Authorization")).thenReturn("Basic 123");
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(2)).doFilter(request, response);
        
        // Case 3: Valid Bearer but validateToken fails
        when(request.getHeader("Authorization")).thenReturn("Bearer token");
        when(mockJwtService.validateToken("token")).thenReturn(false);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(3)).doFilter(request, response);
    }

    @Test
    void testJwtAuthenticationFilter_MissingRoles() throws Exception {
        JwtService mockJwtService = mock(JwtService.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(mockJwtService);
        
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("Authorization")).thenReturn("Bearer valid");
        when(mockJwtService.validateToken("valid")).thenReturn(true);
        when(mockJwtService.extractUsername("valid")).thenReturn("user1");
        when(mockJwtService.extractRoles("valid")).thenReturn(null); // Null roles

        filter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        
        // Case with Missing Username
        when(mockJwtService.extractUsername("valid")).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
