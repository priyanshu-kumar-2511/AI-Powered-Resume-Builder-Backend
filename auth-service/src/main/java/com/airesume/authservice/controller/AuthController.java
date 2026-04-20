package com.airesume.authservice.controller;

import com.airesume.authservice.dto.*;
import com.airesume.authservice.service.AuthService;
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
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String result = authService.register(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "token", token
        ));
    }

    @PostMapping("/forgot-username/initiate")
    public ResponseEntity<Map<String, String>> initiateUsernameRecovery(@Valid @RequestBody UsernameRecoveryRequest request) {
        String result = authService.initiateUsernameRecovery(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/forgot-username/verify")
    public ResponseEntity<Map<String, String>> verifyUsernameRecovery(@Valid @RequestBody OtpVerificationRequest request) {
        String result = authService.verifyUsernameRecovery(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/forgot-password/initiate")
    public ResponseEntity<Map<String, String>> initiatePasswordReset(@Valid @RequestBody PasswordResetInitiateRequest request) {
        String result = authService.initiatePasswordReset(request);
        return ResponseEntity.ok(Map.of("message", result));
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody OtpVerificationRequest request) {
        String result = authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", result));
    }
}
