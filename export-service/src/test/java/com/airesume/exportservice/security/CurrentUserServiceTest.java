package com.airesume.exportservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserServiceTest {

    private final CurrentUserService currentUserService = new CurrentUserService();

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testRequireUserId_WithNumber() {
        setAuthenticationDetails(Map.of("userId", 123L));
        assertEquals(123L, currentUserService.requireUserId());
    }

    @Test
    void testRequireUserId_WithString() {
        setAuthenticationDetails(Map.of("userId", "456"));
        assertEquals(456L, currentUserService.requireUserId());
    }

    @Test
    void testRequireUserId_MissingOrInvalid() {
        setAuthenticationDetails(Map.of("userId", "")); // blank string
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, currentUserService::requireUserId);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());

        setAuthenticationDetails(Map.of("otherKey", "val")); // missing key
        ex = assertThrows(ResponseStatusException.class, currentUserService::requireUserId);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        
        SecurityContextHolder.clearContext(); // No auth
        ex = assertThrows(ResponseStatusException.class, currentUserService::requireUserId);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }

    @Test
    void testIsPremium() {
        setAuthenticationDetails(Map.of("subscriptionPlan", "PREMIUM"));
        assertTrue(currentUserService.isPremium());

        setAuthenticationDetails(Map.of("subscriptionPlan", "premium")); // case insensitive
        assertTrue(currentUserService.isPremium());

        setAuthenticationDetails(Map.of("subscriptionPlan", "FREE"));
        assertFalse(currentUserService.isPremium());

        setAuthenticationDetails(Map.of("otherKey", "val"));
        assertFalse(currentUserService.isPremium());
        
        SecurityContextHolder.clearContext(); // No auth
        assertFalse(currentUserService.isPremium());
    }

    private void setAuthenticationDetails(Map<String, Object> details) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void testRequireUserId_DetailsNotMap() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails("NOT_A_MAP_OBJECT");
        SecurityContextHolder.getContext().setAuthentication(auth);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, currentUserService::requireUserId);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
    }
}
