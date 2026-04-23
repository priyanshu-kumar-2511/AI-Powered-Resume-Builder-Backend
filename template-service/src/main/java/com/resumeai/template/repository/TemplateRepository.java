package com.resumeai.template.repository;

import com.resumeai.template.model.Category;
import com.resumeai.template.model.Template;
import com.resumeai.template.model.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Template entity operations.
 * Provides custom queries for filtering by category, tier, and popularity.
 */
@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    /**
     * Find all templates that are currently active.
     */
    List<Template> findByIsActiveTrue();

    /**
     * Filter active templates by category.
     */
    List<Template> findByIsActiveTrueAndCategory(Category category);

    /**
     * Filter active templates by tier (FREE/PREMIUM).
     */
    List<Template> findByIsActiveTrueAndTier(Tier tier);

    /**
     * Get active templates sorted by usage count in descending order.
     */
    List<Template> findByIsActiveTrueOrderByUsageCountDesc();

    /**
     * Atomic update to increment the usage count of a template.
     */
    @Modifying
    @Query("UPDATE Template t SET t.usageCount = t.usageCount + 1 WHERE t.templateId = :templateId")
    void incrementUsageCount(Long templateId);
}
