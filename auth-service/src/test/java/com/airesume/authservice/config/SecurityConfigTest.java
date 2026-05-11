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

    @Test
    void testRemoveLinkedInNonce() {
        // Case: null request
        assertNull(securityConfig.removeLinkedInNonce(null));
        
        // Case: non-LinkedIn request
        OAuth2AuthorizationRequest googleReq = mock(OAuth2AuthorizationRequest.class);
        when(googleReq.getAuthorizationUri()).thenReturn("https://google.com");
        assertEquals(googleReq, securityConfig.removeLinkedInNonce(googleReq));
        
        // Case: LinkedIn request
        OAuth2AuthorizationRequest linkedinReq = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://linkedin.com")
                .clientId("id")
                .attributes(m -> m.put(OidcParameterNames.NONCE, "abc"))
                .additionalParameters(m -> m.put(OidcParameterNames.NONCE, "abc"))
                .build();
        
        OAuth2AuthorizationRequest result = securityConfig.removeLinkedInNonce(linkedinReq);
        assertFalse(result.getAttributes().containsKey(OidcParameterNames.NONCE));
        assertFalse(result.getAdditionalParameters().containsKey(OidcParameterNames.NONCE));
    }
}
