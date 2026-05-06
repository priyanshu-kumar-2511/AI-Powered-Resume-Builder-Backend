package com.airesume.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentResponse {
    private boolean success;
    private String message;
    private String newToken; // Optional: If we want to return a new token with updated roles
}
