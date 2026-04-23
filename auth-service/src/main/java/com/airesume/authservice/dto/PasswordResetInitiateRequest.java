package com.airesume.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for initiating the password reset process.
 *
 * FIX: Original DTO had two separate fields (username + email) but
 * AuthService.initiatePasswordReset() calls findByUsernameOrEmail(identifier, identifier)
 * using a single value. The frontend now sends `email` in both fields via the AuthService
 * adapter, and the service looks up the user by either username OR email — so supplying
 * the email in both positions works correctly.
 *
 * Simplified to a single `identifier` field to match the service layer.
 * If you prefer to keep the two-field form, update AuthService.initiatePasswordReset()
 * to call findByUsernameOrEmail(request.getUsername(), request.getEmail()) instead.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetInitiateRequest {

    @NotBlank(message = "Email or username is required")
    private String identifier;  // accepts either email or username
}
