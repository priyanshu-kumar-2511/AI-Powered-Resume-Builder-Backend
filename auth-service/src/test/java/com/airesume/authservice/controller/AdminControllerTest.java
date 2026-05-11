package com.airesume.authservice.controller;

import com.airesume.authservice.service.AuthService;
import com.airesume.authservice.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(AdminControllerTest.MinimalSecurityConfig.class)
class AdminControllerTest {

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class MinimalSecurityConfig {
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers() throws Exception {
        when(authService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSuspendUser() throws Exception {
        when(authService.suspendUserById(anyLong(), anyString())).thenReturn("User suspended");

        mockMvc.perform(put("/api/v1/admin/users/1/suspend")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"Test Reason\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User suspended"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSuspendUser_NoReason() throws Exception {
        when(authService.suspendUserById(anyLong(), anyString())).thenReturn("User suspended");

        mockMvc.perform(put("/api/v1/admin/users/1/suspend")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateRole() throws Exception {
        when(authService.updateUserRoleById(anyLong(), anyString())).thenReturn("Role updated");

        mockMvc.perform(put("/api/v1/admin/users/1/role")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\": \"ROLE_ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role updated"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogs() throws Exception {
        when(authService.getAuditLogs()).thenReturn(List.of(Map.of("action", "TEST")));

        mockMvc.perform(get("/api/v1/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("TEST"));
    }

/*
    @Test
    @WithMockUser(roles = "USER")
    void testAccessDeniedForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isForbidden());
    }
*/
    @Test
    @WithMockUser(roles = "ADMIN")
    void testReactivateUser() throws Exception {
        when(authService.reactivateUserById(1L)).thenReturn("User reactivated");
        mockMvc.perform(put("/api/v1/admin/users/1/reactivate"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdatePlanById() throws Exception {
        when(authService.updateSubscriptionById(eq(1L), any())).thenReturn("Plan updated");
        mockMvc.perform(put("/api/v1/admin/users/1/subscription")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"plan\":\"PREMIUM\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser() throws Exception {
        when(authService.deleteUserPermanently(1L)).thenReturn("Deleted");
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/admin/users/1"))
                .andExpect(status().isOk());
    }
}

