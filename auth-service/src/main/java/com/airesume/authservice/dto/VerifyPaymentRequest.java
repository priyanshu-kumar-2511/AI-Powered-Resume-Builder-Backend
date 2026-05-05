package com.airesume.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VerifyPaymentRequest {

    @NotBlank
    private String razorpayOrderId;

    @NotBlank
    private String razorpayPaymentId;

    @NotBlank
    private String razorpaySignature;

    @NotBlank
    @Pattern(regexp = "MONTHLY|YEARLY", message = "billingCycle must be MONTHLY or YEARLY")
    private String billingCycle;
}
