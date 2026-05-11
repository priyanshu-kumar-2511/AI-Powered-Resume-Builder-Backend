package com.airesume.paymentservice.controller;

import com.airesume.paymentservice.dto.AdminSubscriptionResponse;
import com.airesume.paymentservice.dto.SubscriptionStats;
import com.airesume.paymentservice.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Admin Payment Controller", description = "Endpoints for administrator to monitor and manage platform subscriptions")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all subscriptions (Paginated)")
    public ResponseEntity<Page<AdminSubscriptionResponse>> getAllSubscriptions(Pageable pageable) {
        return ResponseEntity.ok(paymentService.getSubscriptions(pageable));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get high-level subscription statistics and revenue")
    public ResponseEntity<SubscriptionStats> getStats() {
        return ResponseEntity.ok(paymentService.getStats());
    }
}
