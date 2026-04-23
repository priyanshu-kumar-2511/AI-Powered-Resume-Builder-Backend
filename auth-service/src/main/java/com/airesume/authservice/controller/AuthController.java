package com.airesume.authservice.controller;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Endpoints for user authentication, registration, and profile management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "token", token
        ));
    }

    @PostMapping("/forgot-username/initiate")
    @Operation(summary = "Initiate username recovery")
    public ResponseEntity<Map<String, String>> initiateUsernameRecovery(@Valid @RequestBody UsernameRecoveryRequest request) {
        String result = authService.initiateUsernameRecovery(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/forgot-username/verify")
    @Operation(summary = "Verify username recovery")
    public ResponseEntity<Map<String, String>> verifyUsernameRecovery(@Valid @RequestBody OtpVerificationRequest request) {
        String result = authService.verifyUsernameRecovery(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * FIX: Now accepts PasswordResetInitiateRequest with a single `identifier` field
     * (accepts either email or username) instead of separate username + email fields.
     */
    @PostMapping("/forgot-password/initiate")
    @Operation(summary = "Initiate password reset")
    public ResponseEntity<Map<String, String>> initiatePasswordReset(@Valid @RequestBody PasswordResetInitiateRequest request) {
        String result = authService.initiatePasswordReset(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/forgot-password/verify")
    @Operation(summary = "Complete password reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody OtpVerificationRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<Map<String, String>> updateProfile(java.security.Principal principal, @Valid @RequestBody ProfileRequest request) {
        String result = authService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(authService.getUserProfile(username));
    }

    @GetMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<Map<String, String>> refresh(java.security.Principal principal) {
        String newToken = authService.refreshToken(principal.getName());
        return ResponseEntity.ok(Map.of("token", newToken));
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token")
    public ResponseEntity<Map<String, String>> validateToken(@RequestParam String token) {
        String username = authService.validateToken(token);
        return ResponseEntity.ok(Map.of(
            "username", username,
            "status", "VALID"
        ));
    }
}
