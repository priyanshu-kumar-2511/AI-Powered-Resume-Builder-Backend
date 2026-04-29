package com.airesume.resumeservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public CacheManager cacheManager() {
        // Local in-memory caches are more predictable for dev than Redis-backed
        // serialization when multiple services are being iterated on together.
        return new ConcurrentMapCacheManager(
                "resume",
                "resumes_user",
                "resumes_template",
                "public_resumes",
                "sections_resume",
                "sections_type",
                "sections_ai",
                "sections_count"
        );
    }
}
