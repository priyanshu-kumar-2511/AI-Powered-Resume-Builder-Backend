package com.airesume.authservice.controller;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Payment endpoints for the Razorpay premium upgrade flow.
 * All endpoints require a valid JWT (enforced by SecurityConfig via .anyRequest().authenticated()).
 *
 * Routes (via API Gateway):
 *   POST /api/v1/payments/create-order
 *   POST /api/v1/payments/verify
 *   GET  /api/v1/payments/status
 *   POST /api/v1/payments/cancel
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Step 1: Create a Razorpay order.
     * Frontend calls this, gets orderId + amount, then opens Razorpay checkout.
     */
    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(request.getBillingCycle()));
    }

    /**
     * Step 2: Verify payment after Razorpay checkout success.
     * Verifies HMAC signature → upgrades plan → returns new JWT.
     */
    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyAndActivate(request));
    }

    /**
     * Get the current user's subscription status.
     * Used by profile page and navbar to show plan info.
     */
    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusResponse> getStatus(Authentication auth) {
        return ResponseEntity.ok(paymentService.getStatus(auth.getName()));
    }

    /**
     * Cancel the active subscription.
     * User remains PREMIUM until endDate, then scheduler downgrades to FREE.
     */
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelSubscription(Authentication auth) {
        String message = paymentService.cancelSubscription(auth.getName());
        return ResponseEntity.ok(Map.of("message", message));
    }
}
