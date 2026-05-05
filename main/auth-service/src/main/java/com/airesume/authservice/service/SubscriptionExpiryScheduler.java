package com.airesume.authservice.service;

import com.airesume.authservice.model.PlanType;
import com.airesume.authservice.model.SubscriptionStatus;
import com.airesume.authservice.repository.SubscriptionRepository;
import com.airesume.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled job that runs every hour to downgrade expired subscriptions to FREE.
 *
 * A subscription is "expired" when:
 *   - endDate is in the past, AND
 *   - status is ACTIVE or CANCELLED (i.e. not already EXPIRED)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiryScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository         userRepository;

    /**
     * Runs every hour (3_600_000 ms).
     * Finds subscriptions that have passed their endDate and downgrades the user.
     */
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void expireSubscriptions() {
        List<com.airesume.authservice.model.Subscription> expired =
                subscriptionRepository.findExpired(LocalDateTime.now());

        if (expired.isEmpty()) return;

        log.info("SubscriptionExpiryScheduler: Found {} expired subscription(s) to process.", expired.size());

        for (var sub : expired) {
            try {
                sub.setStatus(SubscriptionStatus.EXPIRED);
                sub.getUser().setSubscriptionPlan(PlanType.FREE);
                userRepository.save(sub.getUser());
                subscriptionRepository.save(sub);
                log.info("Expired subscription for user: {} (subId: {})", sub.getUser().getUsername(), sub.getId());
            } catch (Exception e) {
                log.error("Failed to expire subscription id={}: {}", sub.getId(), e.getMessage());
            }
        }
    }
}
