-- V1__Initial_Schema.sql
-- Initial database schema for Export Service

CREATE TABLE IF NOT EXISTS export_jobs (
    job_id VARCHAR(100) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resume_id BIGINT NOT NULL,
    format VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    file_url VARCHAR(255),
    file_size_kb BIGINT,
    requested_at DATETIME,
    processing_started_at DATETIME,
    completed_at DATETIME,
    expires_at DATETIME,
    template_id BIGINT,
    customizations TEXT,
    failure_reason TEXT
);

-- Index for fast user-specific export lookups
CREATE INDEX idx_export_userid ON export_jobs(user_id);
