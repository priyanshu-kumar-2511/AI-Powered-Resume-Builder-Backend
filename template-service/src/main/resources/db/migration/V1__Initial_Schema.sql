-- V1__Initial_Schema.sql
-- Initial database schema for Template Service

CREATE TABLE IF NOT EXISTS templates (
    template_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    thumbnail_url LONGTEXT,
    html_layout LONGTEXT,
    css_styles LONGTEXT,
    category VARCHAR(50) NOT NULL,
    tier VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    usage_count BIGINT DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME
);
