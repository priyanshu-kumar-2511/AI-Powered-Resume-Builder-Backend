package com.airesume.authservice.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class PasswordChangeRequest {
    @NotBlank
    private String currentPassword;
    @NotBlank
    private String newPassword;
}
