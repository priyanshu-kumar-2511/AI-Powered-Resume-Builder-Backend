package com.airesume.adminserver.config;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Security configuration for the Spring Boot Admin Server.
 * Configures login pages, asset permissions, and CSRF protection 
 * for the admin dashboard.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AdminServerProperties adminServer;

    public SecurityConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    /**
     * Configures the security filter chain.
     * Allows public access to static assets and login page, while
     * securing all other management endpoints.
     * 
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

        http
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(this.adminServer.path("/assets/**")).permitAll()
                        .requestMatchers(this.adminServer.path("/login")).permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage(this.adminServer.path("/login"))
                        .successHandler(successHandler))
                .logout(logout -> logout
                        .logoutUrl(this.adminServer.path("/logout")))
                .httpBasic(httpBasic -> {
                })
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                this.adminServer.path("/instances"),
                                this.adminServer.path("/instances/*"),
                                "/actuator/**"));

        return http.build();
    }
}
