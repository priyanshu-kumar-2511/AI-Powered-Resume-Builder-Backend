package com.airesume.paymentservice.repository;

import com.airesume.paymentservice.model.Subscription;
import com.airesume.paymentservice.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findTopByUsernameAndStatusOrderByStartDateDesc(String username, SubscriptionStatus status);
    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
}
