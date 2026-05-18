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
 * Main Controller for Authentication and Identity Management.
 * Handles the full lifecycle of a user account: registration (3-step), login,
 * password/username recovery, and profile CRUD operations.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "Endpoints for user authentication, registration, and profile management")
public class AuthController {

    private final AuthService authService;

    /**
     * Handles the registration of a new user.
     * @param request the registration details including username, email, and password
     * @return a success message upon successful registration
     */
    @PostMapping("/register/initiate")
    @Operation(summary = "Initiate registration (Step 1)")
    public ResponseEntity<Map<String, String>> initiateRegistration(@Valid @RequestBody RegisterInitiateRequest request) {
        String result = authService.initiateRegistration(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/register/verify-otp")
    @Operation(summary = "Verify registration OTP (Step 2)")
    public ResponseEntity<Map<String, String>> verifyRegistrationOtp(@Valid @RequestBody OtpVerificationRequest request) {
        String result = authService.verifyRegistrationOtp(request.getIdentifier(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user (Step 3)")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * Authenticates a user and generates a JWT token.
     * @param request the login credentials (username and password)
     * @return a map containing the JWT token and a success message
     */
    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "token", token
        ));
    }

    /**
     * Initiates the username recovery process by sending an OTP to the user's email.
     * @param request the email address associated with the account
     * @return a message indicating the OTP was sent
     */
    @PostMapping("/forgot-username/initiate")
    @Operation(summary = "Initiate username recovery")
    public ResponseEntity<Map<String, String>> initiateUsernameRecovery(@Valid @RequestBody UsernameRecoveryRequest request) {
        String result = authService.initiateUsernameRecovery(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * Verifies the OTP for username recovery. If successful, the username is emailed to the user.
     * @param request the email and the OTP provided by the user
     * @return a success message confirming the username was sent
     */
    @PostMapping("/forgot-username/verify")
    @Operation(summary = "Verify username recovery")
    public ResponseEntity<Map<String, String>> verifyUsernameRecovery(@Valid @RequestBody OtpVerificationRequest request) {
        String result = authService.verifyUsernameRecovery(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * Initiates the password reset process by sending an OTP to the user's email.
     * FIX: Now accepts PasswordResetInitiateRequest with a single `identifier` field
     * (accepts either email or username) instead of separate username + email fields.
     * @param request the identifier (email or username) of the account
     * @return a message indicating the OTP was sent
     */
    @PostMapping("/forgot-password/initiate")
    @Operation(summary = "Initiate password reset")
    public ResponseEntity<Map<String, String>> initiatePasswordReset(@Valid @RequestBody PasswordResetInitiateRequest request) {
        String result = authService.initiatePasswordReset(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * Completes the password reset process after verifying the OTP.
     * @param request the email, OTP, and the new password
     * @return a success message confirming the password change
     */
    @PostMapping("/forgot-password/verify")
    @Operation(summary = "Complete password reset")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody OtpVerificationRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * Updates the authenticated user's profile information.
     * @param principal the currently authenticated user
     * @param request the updated profile details (name, age, mobile, etc.)
     * @return a success message
     */
    @PutMapping("/profile")
    @Operation(summary = "Update user profile")
    public ResponseEntity<Map<String, String>> updateProfile(java.security.Principal principal, @Valid @RequestBody ProfileRequest request) {
        String result = authService.updateProfile(principal.getName(), request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * Permanently deletes the currently authenticated user's own account.
     * @param principal the currently authenticated user
     * @return a success message
     */
    @DeleteMapping("/profile")
    @Operation(summary = "Permanently delete own account")
    public ResponseEntity<Map<String, String>> deleteOwnAccount(java.security.Principal principal) {
        String result = authService.deleteOwnAccount(principal.getName());
        return ResponseEntity.ok(Map.of("message", result));
    }

    /**
     * Retrieves the authenticated user's profile details.
     * @return the user's profile information
     */
    @GetMapping("/profile")
    @Operation(summary = "Get user profile")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok()
                .header("Cache-Control", "no-cache, no-store, must-revalidate")
                .header("Pragma", "no-cache")
                .header("Expires", "0")
                .body(authService.getUserProfile(username));
    }

    /**
     * Refreshes the JWT token for the currently authenticated user.
     * @param principal the currently authenticated user
     * @return a new JWT token
     */
    @GetMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<Map<String, String>> refresh(java.security.Principal principal) {
        String newToken = authService.refreshToken(principal.getName());
        return ResponseEntity.ok(Map.of("token", newToken));
    }

    /**
     * Validates a given JWT token to check its authenticity and retrieve the username.
     * @param token the JWT token to validate
     * @return the username associated with the token and its validity status
     */
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
