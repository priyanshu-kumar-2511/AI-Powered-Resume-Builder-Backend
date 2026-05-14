package com.airesume.authservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuration for JPA Repositories.
 * Enables automated repository implementation scanning for the auth service.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.airesume.authservice.repository")
public class JpaConfig {
}
