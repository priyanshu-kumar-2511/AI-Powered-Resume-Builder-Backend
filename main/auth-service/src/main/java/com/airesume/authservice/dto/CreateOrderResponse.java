package com.airesume.authservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateOrderResponse {
    private String orderId;
    private long   amountInPaise;
    private String currency;
    private String keyId;       // Razorpay public key — safe to send to frontend
    private String billingCycle;
}
