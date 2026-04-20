package com.airesume.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO for initiating the username recovery process.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsernameRecoveryRequest {
    @NotBlank(message = "Email is compulsory")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is compulsory")
    private String password;
}
