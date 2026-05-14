package com.airesume.exportservice.config;

import com.airesume.exportservice.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
/**
 * Unit tests for the JWT Authentication Filter in Export Service.
 * Verifies that incoming HTTP requests are correctly authenticated based on the Authorization header.
 */
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Verifies that requests without an Authorization header proceed through the filter chain without authentication.
     */
    @Test
    void testDoFilterInternal_NoHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies that a valid JWT token results in a populated SecurityContext with the correct user details.
     */
    @Test
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("user@example.com");
        when(jwtService.extractRoles(token)).thenReturn(java.util.List.of("ROLE_USER"));
        when(jwtService.extractUserId(token)).thenReturn(1L);
        when(jwtService.extractPlan(token)).thenReturn("PREMIUM");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user@example.com", SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    /**
     * Verifies that invalid tokens do not populate the SecurityContext.
     */
    @Test
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException {
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies that malformed Authorization headers (e.g., missing "Bearer " prefix) are ignored.
     */
    @Test
    void testDoFilterInternal_MalformedHeader() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("MalformedHeader");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies that if a request is already authenticated, the filter does not overwrite it or re-validate the token.
     */
    @Test
    void testDoFilterInternal_AlreadyAuthenticated() throws ServletException, IOException {
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("already_user", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(existingAuth, SecurityContextHolder.getContext().getAuthentication());
        verify(jwtService, never()).validateToken(any());
    }

    /**
     * Verifies that tokens with missing usernames are rejected during filter processing.
     */
    @Test
    void testDoFilterInternal_NullUsername() throws ServletException, IOException {
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn(null);
        when(jwtService.extractRoles(token)).thenReturn(java.util.List.of("ROLE_USER"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies that tokens with missing role information are rejected during filter processing.
     */
    @Test
    void testDoFilterInternal_NullRoles() throws ServletException, IOException {
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn("user@example.com");
        when(jwtService.extractRoles(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

