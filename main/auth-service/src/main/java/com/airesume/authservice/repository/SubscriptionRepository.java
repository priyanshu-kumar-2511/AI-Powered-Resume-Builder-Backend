package com.airesume.authservice.repository;

import com.airesume.authservice.model.Subscription;
import com.airesume.authservice.model.SubscriptionStatus;
import com.airesume.authservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /** Find the most recent active subscription for a user. */
    Optional<Subscription> findTopByUserAndStatusOrderByStartDateDesc(User user, SubscriptionStatus status);

    /** Find all subscriptions that have expired but are still ACTIVE or CANCELLED. */
    @Query("SELECT s FROM Subscription s WHERE s.endDate < :now AND s.status IN ('ACTIVE', 'CANCELLED')")
    List<Subscription> findExpired(LocalDateTime now);

    /** Paginated list of all subscriptions (for admin). */
    Page<Subscription> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** Count of subscriptions by status. */
    long countByStatus(SubscriptionStatus status);

    /** Total revenue query (sum of amount is tracked via billing cycle). */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status != 'EXPIRED'")
    long countActiveAndCancelledSubscriptions();
}
