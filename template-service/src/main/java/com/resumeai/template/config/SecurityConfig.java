package com.resumeai.template.config;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for stateless REST APIs
            .authorizeHttpRequests(auth -> auth
                // Public Endpoints: Accessible by anyone (Guests)
                .requestMatchers(HttpMethod.GET, "/api/v1/templates", "/api/v1/templates/free", "/api/v1/templates/{templateId}", "/api/v1/templates/category/**", "/api/v1/templates/popular").permitAll()
                
                // Internal Usage Increment: Public but usually called by other services
                .requestMatchers(HttpMethod.PUT, "/api/v1/templates/{templateId}/increment-usage").permitAll()
                
                // Documentation & Monitoring
                .requestMatchers("/v3/api-docs/**", "/api/v1/templates/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/**").permitAll()
                
                // All other requests require authentication (Role checks done at Controller level)
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No JSESSIONID
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
