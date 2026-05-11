package com.airesume.resumeservice.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @InjectMocks
    private CurrentUserService currentUserService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireUserId_ShouldReturnId_WhenNumber() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(Map.of("userId", 100L));

        Long userId = currentUserService.requireUserId();

        assertEquals(100L, userId);
    }

    @Test
    void requireUserId_ShouldReturnId_WhenStringNumber() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(Map.of("userId", "200"));

        Long userId = currentUserService.requireUserId();

        assertEquals(200L, userId);
    }

    @Test
    void requireUserId_ShouldThrowException_WhenInvalidString() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(Map.of("userId", "invalid"));

        assertThrows(ResponseStatusException.class, () -> currentUserService.requireUserId());
    }

    @Test
    void requireUserId_ShouldThrowException_WhenNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(Map.of());

        assertThrows(ResponseStatusException.class, () -> currentUserService.requireUserId());
    }

    @Test
    void requireUserId_ShouldThrowException_WhenNotMap() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn("Some String");

        assertThrows(ResponseStatusException.class, () -> currentUserService.requireUserId());
    }

    @Test
    void isPremium_ShouldReturnTrue_WhenPremium() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(Map.of("subscriptionPlan", "PREMIUM"));

        assertTrue(currentUserService.isPremium());
    }

    @Test
    void isPremium_ShouldReturnFalse_WhenNotPremium() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(Map.of("subscriptionPlan", "FREE"));

        assertFalse(currentUserService.isPremium());
    }

    @Test
    void isAdmin_ShouldReturnTrue_WhenHasAdminRole() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();

        assertTrue(currentUserService.isAdmin());
    }

    @Test
    void isAdmin_ShouldReturnFalse_WhenDoesNotHaveAdminRole() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(authentication).getAuthorities();

        assertFalse(currentUserService.isAdmin());
    }

    @Test
    void isAdmin_ShouldReturnFalse_WhenAuthenticationNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertFalse(currentUserService.isAdmin());
    }

    @Test
    void requireAdmin_ShouldPass_WhenHasAdminRole() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();

        assertDoesNotThrow(() -> currentUserService.requireAdmin());
    }

    @Test
    void requireAdmin_ShouldThrowException_WhenDoesNotHaveAdminRole() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        Collection authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(authentication).getAuthorities();

        assertThrows(ResponseStatusException.class, () -> currentUserService.requireAdmin());
    }

    @Test
    void isPremium_ShouldReturnFalse_WhenPlanNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(Map.of());

        assertFalse(currentUserService.isPremium());
    }

    @Test
    void isAdmin_ShouldReturnFalse_WhenAuthoritiesNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        doReturn(null).when(authentication).getAuthorities();

        assertFalse(currentUserService.isAdmin());
    }

    @Test
    void requireUserId_ShouldThrowException_WhenAuthenticationNull() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> currentUserService.requireUserId());
    }

    @Test
    void requireUserId_ShouldThrowException_WhenBlankString() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getDetails()).thenReturn(Map.of("userId", "   "));

        assertThrows(ResponseStatusException.class, () -> currentUserService.requireUserId());
    }
}
