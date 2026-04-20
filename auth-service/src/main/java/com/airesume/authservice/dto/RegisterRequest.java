package com.airesume.authservice.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Data Transfer Object for user registration.
 * Includes strict validation for Mobile (Regex) and Password complexity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Full Name is compulsory")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotNull(message = "Age is compulsory")
    @Min(value = 18, message = "Minimum age must be 18")
    @Max(value = 120, message = "Please enter a valid age")
    private Integer age;

    @NotBlank(message = "Mobile number is compulsory")
    @Pattern(regexp = "^\\+91[0-9]{10}$", message = "Mobile number must start with +91 followed by 10 digits")
    private String mobileNumber;

    @NotBlank(message = "Email is compulsory")
    @Email(message = "Please enter a valid email address")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,6}$", message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Username is compulsory")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotBlank(message = "Password is compulsory")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must be at least 8 characters long and contain uppercase, lowercase, number, and special character"
    )
    private String password;
}
