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

/**
 * Controller for Authentication and Identity Recovery operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "auth-controller", description = "Endpoints for user authentication, profile management, and account recovery")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with default roles and initializes a quota record")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates credentials and returns a JWT access token")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "token", token
        ));
    }

    @PostMapping("/forgot-username/initiate")
    @Operation(summary = "Initiate username recovery", description = "Validates account details and sends an OTP to the registered email")
    public ResponseEntity<Map<String, String>> initiateUsernameRecovery(@Valid @RequestBody UsernameRecoveryRequest request) {
        String result = authService.initiateUsernameRecovery(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/forgot-username/verify")
    @Operation(summary = "Verify username recovery", description = "Validates the recovery OTP and returns the recovered username")
    public ResponseEntity<Map<String, String>> verifyUsernameRecovery(@Valid @RequestBody OtpVerificationRequest request) {
        String result = authService.verifyUsernameRecovery(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/forgot-password/initiate")
    @Operation(summary = "Initiate password reset", description = "Validates username/email and sends a reset OTP")
    public ResponseEntity<Map<String, String>> initiatePasswordReset(@Valid @RequestBody PasswordResetInitiateRequest request) {
        String result = authService.initiatePasswordReset(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/forgot-password/verify")
    @Operation(summary = "Complete password reset", description = "Validates the reset OTP and updates the user password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody OtpVerificationRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update user profile", description = "Updates the authenticated user's name, age, or mobile number")
    public ResponseEntity<Map<String, String>> updateProfile(java.security.Principal principal, @Valid @RequestBody ProfileRequest request) {
        String result = authService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Allows an authenticated user to update their password by verifying the current one")
    public ResponseEntity<Map<String, String>> changePassword(java.security.Principal principal, @Valid @RequestBody PasswordChangeRequest request) {
        String result = authService.changePassword(principal.getName(), request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PutMapping("/subscription")
    @Operation(summary = "Update subscription plan", description = "Changes the user's subscription tier (e.g., FREE to PREMIUM)")
    public ResponseEntity<Map<String, String>> updateSubscription(java.security.Principal principal, @RequestParam com.airesume.authservice.model.PlanType plan) {
        String result = authService.updateSubscription(principal.getName(), plan);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @DeleteMapping("/deactivate")
    @Operation(summary = "Deactivate account", description = "Soft-deletes the user account by setting its status to inactive")
    public ResponseEntity<Map<String, String>> deactivateAccount(java.security.Principal principal) {
        String result = authService.deactivateAccount(principal.getName());
        return ResponseEntity.ok(Map.of("message", result));
    }

    @GetMapping("/profile")
    @Operation(summary = "Get user profile", description = "Fetches the full profile details of the authenticated user")
    public ResponseEntity<UserProfileResponse> getUserProfile(java.security.Principal principal) {
        return ResponseEntity.ok(authService.getUserProfile(principal.getName()));
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Invalidates the current session (stateless/placeholder)")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    @GetMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Issues a fresh JWT token for the authenticated user")
    public ResponseEntity<Map<String, String>> refresh(java.security.Principal principal) {
        String newToken = authService.refreshToken(principal.getName());
        return ResponseEntity.ok(Map.of("token", newToken));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Verifies the validity of a token and returns the associated username for inter-service security")
    public ResponseEntity<Map<String, String>> validateToken(@RequestParam String token) {
        String username = authService.validateToken(token);
        return ResponseEntity.ok(Map.of(
            "username", username,
            "status", "VALID"
        ));
    }
}
