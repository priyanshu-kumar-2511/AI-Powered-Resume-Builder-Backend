package com.airesume.authservice.controller;

import com.airesume.authservice.dto.CreateOrderRequest;
import com.airesume.authservice.dto.CreateOrderResponse;
import com.airesume.authservice.dto.SubscriptionStatusResponse;
import com.airesume.authservice.dto.VerifyPaymentRequest;
import com.airesume.authservice.dto.VerifyPaymentResponse;
import com.airesume.authservice.service.PaymentService;
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
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.createOrder(request.getBillingCycle()));
    }

    @PostMapping("/verify")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(paymentService.verifyAndActivate(request));
    }

    @PostMapping("/dev-complete")
    public ResponseEntity<VerifyPaymentResponse> completeDevPayment(
            @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.ok(paymentService.completeDevPayment(request.getBillingCycle()));
    }

    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusResponse> getStatus(Authentication auth) {
        return ResponseEntity.ok(paymentService.getStatus(auth.getName()));
    }

    @PostMapping("/cancel")
    public ResponseEntity<Map<String, String>> cancelSubscription(Authentication auth) {
        return ResponseEntity.ok(Map.of("message", paymentService.cancelSubscription(auth.getName())));
    }
}
