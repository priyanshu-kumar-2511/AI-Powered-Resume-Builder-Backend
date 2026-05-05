package com.airesume.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateOrderRequest {
    @NotBlank
    @Pattern(regexp = "MONTHLY|YEARLY", message = "billingCycle must be MONTHLY or YEARLY")
    private String billingCycle;
}
