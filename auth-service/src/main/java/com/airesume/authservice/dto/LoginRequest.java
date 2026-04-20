package com.airesume.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Data Transfer Object for login requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @NotBlank(message = "Username is compulsory")
    private String username;

    @NotBlank(message = "Password is compulsory")
    private String password;
}
