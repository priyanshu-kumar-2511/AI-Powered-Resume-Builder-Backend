-- V1__Initial_Schema.sql
-- Initial database schema for Resume Service

CREATE TABLE IF NOT EXISTS resumes (
    resume_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    target_job_title VARCHAR(255),
    template_id BIGINT,
    ats_score INT DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    language VARCHAR(10) DEFAULT 'en',
    is_public BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    customizations TEXT,
    created_at DATETIME,
    updated_at DATETIME
);

-- Index for fast user-specific resume lookups
CREATE INDEX idx_resume_userid ON resumes(user_id);
