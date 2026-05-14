package com.airesume.authservice.controller;

import com.airesume.authservice.dto.UpdatePlanRequest;
import com.airesume.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for internal service-to-service communication.
 * Provides endpoints for other microservices (like Payment Service) to update 
 * user metadata or subscription states securely.
 */
@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
@Tag(name = "Internal User Controller", description = "Internal endpoints for user plan updates")
public class InternalUserController {

    private final AuthService authService;

    /**
     * Updates a user's subscription plan and returns a fresh JWT token.
     * Typically triggered by Payment Service after a successful order.
     */
    @PostMapping("/update-plan")
    @Operation(summary = "Update user subscription plan (Internal)")
    public ResponseEntity<Map<String, String>> updatePlan(@RequestBody UpdatePlanRequest request) {
        String message = authService.updateSubscriptionByUsername(request.getUsername(), request.getPlan());
        String token = authService.refreshToken(request.getUsername());
        return ResponseEntity.ok(Map.of(
                "message", message,
                "token", token
        ));
    }
}
