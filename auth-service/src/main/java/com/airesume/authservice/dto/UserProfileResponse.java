package com.airesume.authservice.dto;

import com.airesume.authservice.model.PlanType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String mobileNumber;
    private Integer age;
    private PlanType subscriptionPlan;
    private Set<String> roles;
    
    @JsonProperty("isActive")
    private boolean isActive;
}
