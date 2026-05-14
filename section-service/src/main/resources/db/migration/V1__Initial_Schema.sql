-- V1__Initial_Schema.sql
-- Initial database schema for Section Service

CREATE TABLE IF NOT EXISTS resume_sections (
    section_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resume_id BIGINT NOT NULL,
    section_type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content LONGTEXT,
    display_order INT NOT NULL,
    is_visible BOOLEAN DEFAULT TRUE,
    ai_generated BOOLEAN DEFAULT FALSE,
    created_at DATETIME,
    updated_at DATETIME
);

-- Index for fast resume-specific section lookups
CREATE INDEX idx_section_resumeid ON resume_sections(resume_id);
