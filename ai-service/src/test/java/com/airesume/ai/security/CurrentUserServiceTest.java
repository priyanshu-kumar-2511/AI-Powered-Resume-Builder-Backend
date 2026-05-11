package com.airesume.ai.security;

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
    void testRequireUserIdAsString_WithNumber() {
        setAuthenticationDetails(Map.of("userId", 123L));
        assertEquals("123", currentUserService.requireUserIdAsString());
    }

    @Test
    void testRequireUserIdAsString_WithString() {
        setAuthenticationDetails(Map.of("userId", "456"));
        assertEquals("456", currentUserService.requireUserIdAsString());
    }

    @Test
    void testRequireUserIdAsString_MissingOrInvalid() {
        setAuthenticationDetails(Map.of("userId", "")); // blank string
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, currentUserService::requireUserIdAsString);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());

        setAuthenticationDetails(Map.of("otherKey", "val")); // missing key
        ex = assertThrows(ResponseStatusException.class, currentUserService::requireUserIdAsString);
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
        
        SecurityContextHolder.clearContext(); // No auth
        ex = assertThrows(ResponseStatusException.class, currentUserService::requireUserIdAsString);
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

    @Test
    void testGetDetail_DetailsNotAMap() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails("Not A Map Object"); // set details to a String instead of a Map
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        assertFalse(currentUserService.isPremium());
    }

    private void setAuthenticationDetails(Map<String, Object> details) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", null);
        auth.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
