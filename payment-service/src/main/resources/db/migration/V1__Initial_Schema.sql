-- V1__Initial_Schema.sql
-- Initial database schema for Payment Service

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    plan VARCHAR(50) NOT NULL,
    billing_cycle VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    start_date DATETIME NOT NULL,
    end_date DATETIME,
    razorpay_order_id VARCHAR(255) UNIQUE,
    razorpay_payment_id VARCHAR(255) UNIQUE,
    created_at DATETIME
);

-- Index for fast username-specific lookups
CREATE INDEX idx_subscription_username ON subscriptions(username);
