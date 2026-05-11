package com.airesume.notificationservice.service;

import com.airesume.notificationservice.dto.AdminUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserClientTest {

    @Mock
    private RestClient.Builder restClientBuilder;

    @Mock
    private RestClient restClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private AdminUserClient adminUserClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adminUserClient, "adminBaseUrl", "http://localhost:8080/api/v1/admin");
    }

    @Test
    void testGetAllUsers_NullHeader() {
        List<AdminUserDto> result = adminUserClient.getAllUsers(null);
        assertTrue(result.isEmpty());
        verifyNoInteractions(restClientBuilder);
    }

    @Test
    void testGetAllUsers_BlankHeader() {
        List<AdminUserDto> result = adminUserClient.getAllUsers("   ");
        assertTrue(result.isEmpty());
        verifyNoInteractions(restClientBuilder);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetAllUsers_Success() {
        String authHeader = "Bearer valid-token";
        AdminUserDto user = new AdminUserDto();
        user.setUserId(1L);
        user.setSubscriptionPlan("PREMIUM");
        user.setActive(true);
        List<AdminUserDto> expected = List.of(user);

        when(restClientBuilder.build()).thenReturn(restClient);
        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq(HttpHeaders.AUTHORIZATION), eq(authHeader))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);

        List<AdminUserDto> result = adminUserClient.getAllUsers(authHeader);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
        assertEquals("PREMIUM", result.get(0).getSubscriptionPlan());
        assertTrue(result.get(0).isActive());

        verify(restClientBuilder).build();
        verify(restClient).get();
        verify(requestHeadersUriSpec).uri("http://localhost:8080/api/v1/admin/users");
        verify(requestHeadersSpec).header(HttpHeaders.AUTHORIZATION, authHeader);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).body(any(ParameterizedTypeReference.class));
    }
}
