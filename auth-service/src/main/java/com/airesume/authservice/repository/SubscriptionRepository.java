package com.airesume.authservice.repository;

import com.airesume.authservice.model.Subscription;
import com.airesume.authservice.model.SubscriptionStatus;
import com.airesume.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findTopByUserAndStatusOrderByStartDateDesc(User user, SubscriptionStatus status);
}
