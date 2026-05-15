package com.airesume.authservice.config;

import com.airesume.authservice.model.PlanType;
import com.airesume.authservice.model.ProviderType;
import com.airesume.authservice.model.Role;
import com.airesume.authservice.model.User;
import com.airesume.authservice.repository.RoleRepository;
import com.airesume.authservice.repository.UserRepository;
import com.airesume.authservice.service.EmailService;
import com.airesume.authservice.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OAuth2SuccessHandlerTest {

    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private OAuth2User oAuth2User;
    @Mock
    private RedirectStrategy redirectStrategy;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .fullName("Test User")
                .roles(new HashSet<>(Collections.singleton(new Role(1, "ROLE_USER"))))
                .subscriptionPlan(PlanType.FREE)
                .build();
        oAuth2SuccessHandler.setRedirectStrategy(redirectStrategy);
    }

    @Test
    void onAuthenticationSuccess_ExistingUser_Google() throws Exception {
        ReflectionTestUtils.setField(oAuth2SuccessHandler, "frontendPublicUrl", "http://localhost:4200///");
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(authToken.getPrincipal()).thenReturn(oAuth2User);
        when(authToken.getAuthorizedClientRegistrationId()).thenReturn("google");
        
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(anyString(), any())).thenReturn("mockToken");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("http://localhost:4200/login"));
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("token=mockToken"));
    }

    @Test
    void onAuthenticationSuccess_NewUser_LinkedIn() throws Exception {
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(authToken.getPrincipal()).thenReturn(oAuth2User);
        when(authToken.getAuthorizedClientRegistrationId()).thenReturn("linkedin");
        
        when(oAuth2User.getAttribute("emailAddress")).thenReturn("new@example.com");
        when(oAuth2User.getAttribute("given_name")).thenReturn("New");
        when(oAuth2User.getAttribute("family_name")).thenReturn("User");
        
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1, "ROLE_USER")));
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(existingUser);
        when(jwtService.generateToken(anyString(), any())).thenReturn("mockToken");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authToken);

        verify(userRepository).save(any(User.class));
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("token=mockToken"));
    }

    @Test
    void onAuthenticationSuccess_ExceptionHandling() throws Exception {
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(authToken.getPrincipal()).thenThrow(new RuntimeException("Something went wrong"));

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("oauthError=true"));
    }

    @Test
    void onAuthenticationSuccess_MissingEmail() throws Exception {
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(authToken.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn(null);
        when(oAuth2User.getAttribute("emailAddress")).thenReturn("");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("reason=OAuth provider did not return an email address"));
    }

    @Test
    void onAuthenticationSuccess_MissingName_Google() throws Exception {
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(authToken.getPrincipal()).thenReturn(oAuth2User);
        when(authToken.getAuthorizedClientRegistrationId()).thenReturn("google");
        
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn(null);
        
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(anyString(), any())).thenReturn("mockToken");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("token=mockToken"));
    }

    @Test
    void onAuthenticationSuccess_NameFallbackAndUsernameCollision() throws Exception {
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(authToken.getPrincipal()).thenReturn(oAuth2User);
        when(authToken.getAuthorizedClientRegistrationId()).thenReturn("other"); // ProviderType.OAUTH2

        when(oAuth2User.getAttribute("email")).thenReturn("collision@test.com");
        when(oAuth2User.getAttribute("name")).thenReturn("");
        when(oAuth2User.getAttribute("given_name")).thenReturn(null);
        when(oAuth2User.getAttribute("family_name")).thenReturn(null);

        when(userRepository.findByEmail("collision@test.com")).thenReturn(Optional.empty());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role(1, "ROLE_USER")));
        
        // Simulate username collision twice
        when(userRepository.existsByUsername("collision")).thenReturn(true);
        when(userRepository.existsByUsername("collision1")).thenReturn(true);
        when(userRepository.existsByUsername("collision2")).thenReturn(false);
        
        when(userRepository.save(any())).thenReturn(existingUser);
        when(jwtService.generateToken(anyString(), any())).thenReturn("mockToken");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authToken);

        verify(userRepository).save(argThat(user -> user.getUsername().equals("collision2") && user.getFullName().equals("User")));
        verify(redirectStrategy).sendRedirect(eq(request), eq(response), contains("token=mockToken"));
    }

    @Test
    void onAuthenticationSuccess_GenericOAuthToken() throws Exception {
        Authentication authToken = mock(Authentication.class); // Not OAuth2AuthenticationToken
        when(authToken.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttribute("email")).thenReturn("gen@test.com");
        when(userRepository.findByEmail("gen@test.com")).thenReturn(Optional.of(existingUser));

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authToken);

        verify(redirectStrategy).sendRedirect(eq(request), eq(response), anyString());
    }

    @Test
    void onAuthenticationSuccess_UsesFrontendUrlWithoutChangingSafeBehavior() throws Exception {
        ReflectionTestUtils.setField(oAuth2SuccessHandler, "frontendPublicUrl", "https://resumeai.example.com/app/");

        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(authToken.getPrincipal()).thenReturn(oAuth2User);
        when(authToken.getAuthorizedClientRegistrationId()).thenReturn("google");
        when(oAuth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oAuth2User.getAttribute("name")).thenReturn("Test User");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(anyString(), any())).thenReturn("mockToken");

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authToken);

        verify(redirectStrategy).sendRedirect(
                eq(request),
                eq(response),
                contains("https://resumeai.example.com/app/login")
        );
    }
}
