package com.airesume.paymentservice.controller;

import com.airesume.paymentservice.dto.CreateOrderRequest;
import com.airesume.paymentservice.dto.CreateOrderResponse;
import com.airesume.paymentservice.dto.SubscriptionStatusResponse;
import com.airesume.paymentservice.dto.VerifyPaymentRequest;
import com.airesume.paymentservice.dto.VerifyPaymentResponse;
import com.airesume.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Controller", description = "Endpoints for handling user subscriptions, orders, and payment transactions")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    @Operation(summary = "Create a new payment order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(request.getBillingCycle()));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify a payment and activate subscription")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyAndActivate(request));
    }

    @PostMapping("/dev-complete")
    @Operation(summary = "Automatically complete payment order (Development bypass)")
    public ResponseEntity<VerifyPaymentResponse> completeDevPayment(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.completeDevPayment(request.getBillingCycle()));
    }

    @GetMapping("/status")
    @Operation(summary = "Get current subscription status of logged-in user")
    public ResponseEntity<SubscriptionStatusResponse> getStatus(Authentication auth) {
        return ResponseEntity.ok(paymentService.getStatus(auth.getName()));
    }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel active subscription")
    public ResponseEntity<Map<String, String>> cancelSubscription(Authentication auth) {
        return ResponseEntity.ok(Map.of("message", paymentService.cancelSubscription(auth.getName())));
    }
}
