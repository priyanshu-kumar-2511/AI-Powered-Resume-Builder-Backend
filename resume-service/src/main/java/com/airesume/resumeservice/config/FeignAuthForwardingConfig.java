package com.airesume.resumeservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Configuration for Feign Client Authentication Forwarding.
 * Ensures that the JWT token from the incoming request is propagated 
 * to downstream microservices for authorized inter-service communication.
 */
@Configuration
public class FeignAuthForwardingConfig {

    /**
     * Creates a Feign RequestInterceptor that extracts the Authorization header 
     * from the current web context and adds it to the Feign request template.
     */
    @Bean
    public RequestInterceptor authForwardingInterceptor() {
        return template -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }

            String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (authorization != null && !authorization.isBlank()) {
                template.header(HttpHeaders.AUTHORIZATION, authorization);
            }
        };
    }
}
