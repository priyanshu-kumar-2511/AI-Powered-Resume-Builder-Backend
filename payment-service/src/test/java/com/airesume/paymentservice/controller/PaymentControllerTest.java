package com.airesume.paymentservice.controller;

import com.airesume.paymentservice.dto.CreateOrderRequest;
import com.airesume.paymentservice.dto.CreateOrderResponse;
import com.airesume.paymentservice.dto.VerifyPaymentRequest;
import com.airesume.paymentservice.dto.VerifyPaymentResponse;
import com.airesume.paymentservice.service.JwtService;
import com.airesume.paymentservice.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void testCreateOrder() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setBillingCycle("MONTHLY");
        when(paymentService.createOrder(anyString())).thenReturn(CreateOrderResponse.builder().orderId("123").build());

        mockMvc.perform(post("/api/v1/payments/create-order")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/v1/payments/status"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testVerifyPayment() throws Exception {
        VerifyPaymentRequest request = VerifyPaymentRequest.builder()
                .razorpayOrderId("123")
                .razorpayPaymentId("456")
                .razorpaySignature("sig")
                .billingCycle("MONTHLY")
                .build();
        
        when(paymentService.verifyAndActivate(any())).thenReturn(VerifyPaymentResponse.builder().success(true).build());

        mockMvc.perform(post("/api/v1/payments/verify")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCompleteDevPayment() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setBillingCycle("YEARLY");
        
        when(paymentService.completeDevPayment(anyString())).thenReturn(VerifyPaymentResponse.builder().success(true).build());

        mockMvc.perform(post("/api/v1/payments/dev-complete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testCancelSubscription() throws Exception {
        when(paymentService.cancelSubscription(anyString())).thenReturn("Success");

        mockMvc.perform(post("/api/v1/payments/cancel")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
