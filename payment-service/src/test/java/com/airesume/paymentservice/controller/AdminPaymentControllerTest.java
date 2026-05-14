package com.airesume.paymentservice.controller;

import com.airesume.paymentservice.dto.SubscriptionStats;
import com.airesume.paymentservice.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for the Administrative Payment Controller.
 * Verifies global subscription management and revenue reporting for admin users.
 */
@WebMvcTest(AdminPaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private com.airesume.paymentservice.service.JwtService jwtService;

    @MockBean
    private com.airesume.paymentservice.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Verifies that admins can retrieve a paginated list of all system subscriptions.
     */
    @Test
    void testGetAllSubscriptions() throws Exception {
        Page<com.airesume.paymentservice.dto.AdminSubscriptionResponse> page = new PageImpl<>(Collections.emptyList());
        when(paymentService.getSubscriptions(any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/admin/subscriptions")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Tests the retrieval of global subscription statistics and revenue data.
     */
    @Test
    void testGetStats() throws Exception {
        SubscriptionStats stats = new SubscriptionStats();
        when(paymentService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/v1/admin/subscriptions/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
