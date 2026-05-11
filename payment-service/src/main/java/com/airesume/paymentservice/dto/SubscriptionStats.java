package com.airesume.paymentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStats {
    private long totalActiveSubscriptions;
    private long totalExpiredSubscriptions;
    private long totalCancelledSubscriptions;
    private long totalRevenueInPaise;
    private Map<String, Long> planDistribution;
}
