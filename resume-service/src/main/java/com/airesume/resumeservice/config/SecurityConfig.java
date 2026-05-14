package com.airesume.resumeservice.config;

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
 * Security configuration for the Resume Service.
 * Defines the security filter chain, stateless session management,
 * and endpoint-level authorization rules (RBAC).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configures the security filter chain for all HTTP requests.
     * 1. Disables CSRF (stateless API)
     * 2. Permits public GET access for resumes
     * 3. Restricted admin paths to ROLE_ADMIN
     * 4. Enforces JWT authentication for all other requests
     */
    @Bean
    @SuppressWarnings("java:S4502") // CSRF is disabled because we use JWT and the API is stateless
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // CSRF protection is not required for stateless REST APIs using JWT
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/public", "/api/v1/resumes/public").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/*/view-count", "/api/v1/resumes/*/view-count").permitAll()
                        .requestMatchers("/v3/api-docs", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**").permitAll()
                        .requestMatchers("/admin/**", "/api/v1/resumes/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
