package com.airesume.authservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.test.context.ActiveProfiles;

import jakarta.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityConfig internal logic.
 * Mocks necessary beans to avoid full security context overhead.
 */
@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Validates that the system correctly identifies LinkedIn authorization URIs.
     */
    @Test
    void testIsLinkedInRequest() {
        OAuth2AuthorizationRequest req = mock(OAuth2AuthorizationRequest.class);
        
        when(req.getAuthorizationUri()).thenReturn("https://www.linkedin.com/oauth/v2/authorization");
        assertTrue(securityConfig.isLinkedInRequest(req));
        
        when(req.getAuthorizationUri()).thenReturn("https://accounts.google.com/o/oauth2/v2/auth");
        assertFalse(securityConfig.isLinkedInRequest(req));
        
        when(req.getAuthorizationUri()).thenReturn(null);
        assertFalse(securityConfig.isLinkedInRequest(req));
    }

    /**
     * Tests the modification of OAuth2 requests for different providers.
     * Verifies LinkedIn nonce removal and Google account-selection prompt addition.
     */
    @Test
    void testCustomizeAuthorizationRequest() {
        // Case: null request
        assertNull(securityConfig.customizeAuthorizationRequest(null));
        
        // Case: LinkedIn request (should remove nonce)
        OAuth2AuthorizationRequest linkedinReq = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://linkedin.com")
                .clientId("id")
                .attributes(m -> m.put(OidcParameterNames.NONCE, "abc"))
                .additionalParameters(m -> m.put(OidcParameterNames.NONCE, "abc"))
                .build();
        
        OAuth2AuthorizationRequest result = securityConfig.customizeAuthorizationRequest(linkedinReq);
        assertFalse(result.getAttributes().containsKey(OidcParameterNames.NONCE));
        assertFalse(result.getAdditionalParameters().containsKey(OidcParameterNames.NONCE));

        // Case: Google request (should add select_account prompt)
        OAuth2AuthorizationRequest googleReq = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("google-id")
                .build();
        
        OAuth2AuthorizationRequest googleResult = securityConfig.customizeAuthorizationRequest(googleReq);
        assertEquals("select_account", googleResult.getAdditionalParameters().get("prompt"));
    }
}
