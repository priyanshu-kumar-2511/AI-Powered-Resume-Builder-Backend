package com.airesume.resumeservice.config;

import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeignAuthForwardingConfigTest {

    private final FeignAuthForwardingConfig config = new FeignAuthForwardingConfig();

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void apply_ShouldForwardAuthorizationHeader_WhenPresent() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer some-token");

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        RequestTemplate template = new RequestTemplate();
        config.authForwardingInterceptor().apply(template);

        assertTrue(template.headers().containsKey("Authorization"));
        assertTrue(template.headers().get("Authorization").contains("Bearer some-token"));
    }

    @Test
    void apply_ShouldNotForwardAuthorizationHeader_WhenBlank() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("  ");

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        RequestTemplate template = new RequestTemplate();
        config.authForwardingInterceptor().apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
    }

    @Test
    void apply_ShouldNotForward_WhenNoRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();

        RequestTemplate template = new RequestTemplate();
        config.authForwardingInterceptor().apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
    }

    @Test
    void apply_ShouldNotForward_WhenAuthHeaderNull() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        RequestTemplate template = new RequestTemplate();
        config.authForwardingInterceptor().apply(template);

        assertFalse(template.headers().containsKey("Authorization"));
    }
}
