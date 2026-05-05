package com.airesume.authservice.dto;

import com.airesume.authservice.model.BillingCycle;
import com.airesume.authservice.model.PlanType;
import com.airesume.authservice.model.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionStatusResponse {
    private PlanType           plan;
    private BillingCycle       billingCycle;
    private SubscriptionStatus status;
    private LocalDateTime      startDate;
    private LocalDateTime      endDate;
    private String             razorpayPaymentId;
}
