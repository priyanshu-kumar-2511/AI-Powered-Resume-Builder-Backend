package com.airesume.notificationservice.service;

import com.airesume.notificationservice.dto.AdminUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserClient {

    private final RestClient.Builder restClientBuilder;

    @Value("${services.auth.admin-base-url:http://localhost:8080/api/v1/admin}")
    private String adminBaseUrl;

    public List<AdminUserDto> getAllUsers(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Collections.emptyList();
        }

        return restClientBuilder.build()
                .get()
                .uri(adminBaseUrl + "/users")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
