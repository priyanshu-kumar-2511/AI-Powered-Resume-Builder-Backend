package com.airesume.authservice.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;

@Data
public class ProfileRequest {
    private String fullName;
    private Integer age;
    private String mobileNumber;
}
