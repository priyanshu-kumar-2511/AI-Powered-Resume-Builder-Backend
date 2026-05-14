-- V1__Initial_Schema.sql
-- Initial database schema for AI Service

CREATE TABLE IF NOT EXISTS ai_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    prompt_used TEXT,
    response_content TEXT,
    model_used VARCHAR(100),
    tokens_used INT,
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS user_quotas (
    user_id VARCHAR(100) PRIMARY KEY,
    remaining_summary_count INT NOT NULL,
    remaining_ats_count INT NOT NULL,
    is_premium BOOLEAN NOT NULL DEFAULT FALSE,
    last_reset_date DATETIME
);

-- Index for fast user-specific history lookups
CREATE INDEX idx_ai_history_userid ON ai_history(user_id);
