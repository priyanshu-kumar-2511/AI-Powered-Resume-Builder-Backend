package com.airesume.templateservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Main Security Configuration for Template Service.
 * Defines public vs protected endpoints and integrates JWT filtering.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the security filter chain.
     * Highlights:
     * - Allows public read access to all resume templates.
     * - Allows public usage increment for analytics.
     * - Requires authentication for all other operations (e.g., admin template creation).
     */
    @Bean
    @SuppressWarnings("java:S4502") // CSRF is disabled because we use JWT and the API is stateless
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // CSRF protection is not required for stateless REST APIs using JWT
            .authorizeHttpRequests(auth -> auth
                // Public GET endpoints — paths after gateway rewrite (no /api/v1/templates prefix)
                .requestMatchers(HttpMethod.GET,
                    "/",              // GET all templates
                    "/free",          // GET free templates
                    "/{templateId}",  // GET single template
                    "/category/**",   // GET by category
                    "/popular",       // GET popular
                    "/search",        // GET search
                    // Also allow with prefix in case of direct calls (not via gateway)
                    "/api/v1/templates",
                    "/api/v1/templates/free",
                    "/api/v1/templates/{templateId}",
                    "/api/v1/templates/category/**",
                    "/api/v1/templates/popular"
                ).permitAll()

                // Usage increment — public
                .requestMatchers(HttpMethod.PUT, "/{templateId}/increment-usage",
                    "/api/v1/templates/{templateId}/increment-usage").permitAll()

                // Docs & monitoring
                .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**").permitAll()

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }
}
