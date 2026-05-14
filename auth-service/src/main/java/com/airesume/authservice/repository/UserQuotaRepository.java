package com.airesume.authservice.repository;

import com.airesume.authservice.model.UserQuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for UserQuota entity.
 * Manages access to AI call and feature usage limits per user.
 */
public interface UserQuotaRepository extends JpaRepository<UserQuota, Long> {
    Optional<UserQuota> findByUserId(Long userId);
}
