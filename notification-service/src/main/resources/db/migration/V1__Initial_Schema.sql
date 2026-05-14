-- V1__Initial_Schema.sql
-- Initial database schema for Notification Service

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50),
    tier VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME,
    read_at DATETIME
);

-- Index for fast recipient-specific notification lookups
CREATE INDEX idx_notification_recipient ON notifications(recipient_id);
