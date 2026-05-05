package com.airesume.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for verifying an OTP code.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {
    @NotBlank(message = "Identifier is compulsory")
    private String identifier; // email or username

    @NotBlank(message = "OTP code is compulsory")
    @Size(min = 6, max = 6, message = "OTP must be exactly 6 digits")
    private String otp;

    // Optional for password reset
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "New password does not meet complexity requirements"
    )
    private String newPassword;
}
