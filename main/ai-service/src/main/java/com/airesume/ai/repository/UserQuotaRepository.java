package com.airesume.ai.repository;

import com.airesume.ai.entity.UserQuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserQuotaRepository extends JpaRepository<UserQuota, String> {
}
