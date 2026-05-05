package com.airesume.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyPaymentResponse {
    private boolean success;
    private String  newToken;   // Fresh JWT with PREMIUM claim in subscriptionPlan
    private String  message;
}
