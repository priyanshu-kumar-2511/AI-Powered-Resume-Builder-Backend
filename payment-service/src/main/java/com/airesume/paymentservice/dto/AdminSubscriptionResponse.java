package com.airesume.paymentservice.dto;

import com.airesume.paymentservice.model.BillingCycle;
import com.airesume.paymentservice.model.PlanType;
import com.airesume.paymentservice.model.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSubscriptionResponse {
    private Long id;
    private String username;
    private String fullName; // Will be same as username for now
    private PlanType plan;
    private BillingCycle billingCycle;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;
}
