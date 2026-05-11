package com.airesume.sectionservice.config;

import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class FeignAuthForwardingConfigTest {

    private FeignAuthForwardingConfig config;

    @BeforeEach
    void setUp() {
        config = new FeignAuthForwardingConfig();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void apply_ShouldForwardAuthHeader() {
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
    void apply_ShouldNotForwardIfNoAuthHeader() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn(null);

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        RequestTemplate template = new RequestTemplate();
        config.authForwardingInterceptor().apply(template);

        assertTrue(!template.headers().containsKey("Authorization"));
    }

    @Test
    void apply_ShouldNotCrashIfNoRequestAttributes() {
        RequestContextHolder.resetRequestAttributes();

        RequestTemplate template = new RequestTemplate();
        config.authForwardingInterceptor().apply(template);

        assertTrue(!template.headers().containsKey("Authorization"));
    }

    @Test
    void apply_ShouldNotForwardIfAttributesNotServlet() {
        org.springframework.web.context.request.RequestAttributes mockAttributes = mock(org.springframework.web.context.request.RequestAttributes.class);
        RequestContextHolder.setRequestAttributes(mockAttributes);

        RequestTemplate template = new RequestTemplate();
        config.authForwardingInterceptor().apply(template);

        assertTrue(!template.headers().containsKey("Authorization"));
    }

    @Test
    void apply_ShouldNotForwardIfAuthHeaderBlank() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("   ");

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        RequestTemplate template = new RequestTemplate();
        config.authForwardingInterceptor().apply(template);

        assertTrue(!template.headers().containsKey("Authorization"));
    }
}
