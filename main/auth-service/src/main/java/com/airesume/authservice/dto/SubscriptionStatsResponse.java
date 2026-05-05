package com.airesume.authservice.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class SubscriptionStatsResponse {
    private long totalActiveSubscriptions;
    private long totalExpiredSubscriptions;
    private long totalCancelledSubscriptions;
    private long totalRevenueInPaise;
    private Map<String, Long> planDistribution; // e.g. {"MONTHLY": 10, "YEARLY": 5}
}
