package com.airesume.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for initiating the password reset process.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetInitiateRequest {
    @NotBlank(message = "Username is compulsory")
    private String username;

    @NotBlank(message = "Email is compulsory")
    @Email(message = "Invalid email format")
    private String email;
}
