package com.airesume.authservice.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Security configuration for the Auth Service.
 *
 * FIX: The original config used SessionCreationPolicy.STATELESS globally.
 * Spring Security's OAuth2 login REQUIRES a session to store the PKCE
 * state parameter between the initial redirect and Google's callback.
 * With STATELESS, the state is never found on callback → OAuth2 fails
 * with "Authorization request not found" or "state mismatch" error.
 *
 * Solution: Use IF_REQUIRED so sessions are created only when needed
 * (i.e. during OAuth2 flow). JWT-authenticated API calls remain
 * effectively stateless because the client sends the Bearer token on
 * every request and no server-side session is consulted for those paths.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/v1/auth/register",
                    "/api/v1/auth/login",
                    "/api/v1/auth/forgot-username/**",
                    "/api/v1/auth/forgot-password/**",
                    "/api/v1/auth/logout",
                    "/api/v1/auth/validate",
                    "/api/v1/internal/**"
                ).permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(linkedinAwareAuthorizationRequestResolver())
                )
                .successHandler(oAuth2SuccessHandler)
                .failureHandler((request, response, exception) -> {
                    log.error("OAuth2 login failed: {}", exception.getMessage(), exception);

                    String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:4200/login")
                            .queryParam("oauthError", "true")
                            .queryParam("reason", exception.getMessage())
                            .build()
                            .toUriString();

                    response.sendRedirect(targetUrl);
                })
            )
            /*
             * IF_REQUIRED: sessions are created only when Spring Security
             * needs one (OAuth2 state storage). API calls that present a
             * JWT never trigger session creation.
             *
             * DO NOT use STATELESS here — it breaks the OAuth2 code-exchange
             * because the authorization request (state/nonce) is stored in
             * the HttpSession and is unreachable when Google redirects back.
             */
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private OAuth2AuthorizationRequestResolver linkedinAwareAuthorizationRequestResolver() {
        DefaultOAuth2AuthorizationRequestResolver delegate =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return new OAuth2AuthorizationRequestResolver() {
            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
                return removeLinkedInNonce(delegate.resolve(request));
            }

            @Override
            public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
                return removeLinkedInNonce(delegate.resolve(request, clientRegistrationId));
            }
        };
    }

    private OAuth2AuthorizationRequest removeLinkedInNonce(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null || !isLinkedInRequest(authorizationRequest)) {
            return authorizationRequest;
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .attributes(attributes -> attributes.remove(OidcParameterNames.NONCE))
                .additionalParameters(parameters -> parameters.remove(OidcParameterNames.NONCE))
                .build();
    }

    private boolean isLinkedInRequest(OAuth2AuthorizationRequest authorizationRequest) {
        String authorizationUri = authorizationRequest.getAuthorizationUri();
        return authorizationUri != null && authorizationUri.contains("linkedin.com");
    }
}
