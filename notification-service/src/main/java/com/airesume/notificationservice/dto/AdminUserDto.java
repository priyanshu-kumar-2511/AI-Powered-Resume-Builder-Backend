package com.airesume.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AdminUserDto {
    private Long userId;
    private String subscriptionPlan;

    @JsonProperty("isActive")
    private boolean active;
}
