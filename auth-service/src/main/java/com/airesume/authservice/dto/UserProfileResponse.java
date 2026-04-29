package com.airesume.authservice.dto;

import com.airesume.authservice.model.PlanType;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String mobileNumber;
    private Integer age;
    private PlanType subscriptionPlan;
    private Set<String> roles;
    private boolean isActive;
}
