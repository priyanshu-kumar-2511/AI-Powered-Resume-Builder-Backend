package com.airesume.templateservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * JPA Entity representing a Resume Template.
 * Stores the layout (HTML/CSS) and metadata for different resume styles.
 */
@Entity
@Table(name = "templates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long templateId;

    @Column(nullable = false)
    private String name;

    private String description;

    private String thumbnailUrl;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String htmlLayout;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String cssStyles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Long usageCount = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
