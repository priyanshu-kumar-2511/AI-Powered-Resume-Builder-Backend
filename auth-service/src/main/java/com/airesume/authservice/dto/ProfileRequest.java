package com.airesume.authservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequest {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 120, message = "Age must be 120 or below")
    private Integer age;

    @Pattern(
            regexp = "^(\\+91\\d{10})?$",
            message = "Mobile number must be in the format +91 followed by 10 digits"
    )
    private String mobileNumber;
}
