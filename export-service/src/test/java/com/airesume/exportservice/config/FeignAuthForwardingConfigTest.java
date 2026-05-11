package com.airesume.exportservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;

class FeignAuthForwardingConfigTest {

    private final FeignAuthForwardingConfig config = new FeignAuthForwardingConfig();
    private final RequestInterceptor interceptor = config.authForwardingInterceptor();

    @AfterEach
    void tearDown() {
        ExportAuthContext.clear();
    }

    @Test
    void testAuthForwardingInterceptor_NullAuth() {
        ExportAuthContext.setAuthorization(null);
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertFalse(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testAuthForwardingInterceptor_BlankAuth() {
        ExportAuthContext.setAuthorization("    ");
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertFalse(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void testAuthForwardingInterceptor_ValidAuth() {
        ExportAuthContext.setAuthorization("Bearer valid_token");
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertTrue(template.headers().containsKey(HttpHeaders.AUTHORIZATION));
        assertTrue(template.headers().get(HttpHeaders.AUTHORIZATION).contains("Bearer valid_token"));
    }
}
