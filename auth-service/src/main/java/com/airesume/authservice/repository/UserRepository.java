package com.airesume.authservice.repository;

import com.airesume.authservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Additional methods from PDF Requirements
    java.util.List<User> findBySubscriptionPlan(com.airesume.authservice.model.PlanType plan);
    java.util.List<User> findByIsActive(boolean isActive);
    java.util.List<User> findAllByRoles_Name(String roleName);
    void deleteById(Long userId);
}
