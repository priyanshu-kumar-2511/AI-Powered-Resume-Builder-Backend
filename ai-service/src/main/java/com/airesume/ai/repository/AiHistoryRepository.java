package com.airesume.ai.repository;

import com.airesume.ai.entity.AiHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiHistoryRepository extends JpaRepository<AiHistory, Long> {
    List<AiHistory> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT CAST(a.createdAt AS date) as date, COUNT(a) as count FROM AiHistory a GROUP BY CAST(a.createdAt AS date) ORDER BY date ASC")
    List<Object[]> getDailyStats();
}
