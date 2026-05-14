-- V1__Initial_Schema.sql
-- Initial database schema for Auth Service

-- 1. Roles table
CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    age INT,
    mobile_number VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    enabled BOOLEAN DEFAULT TRUE,
    subscription_plan VARCHAR(20) DEFAULT 'FREE',
    provider VARCHAR(20) DEFAULT 'LOCAL',
    created_at DATETIME,
    updated_at DATETIME,
    premium_expires_at DATETIME
);

-- 3. User-Roles Join Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. User Quotas table
CREATE TABLE IF NOT EXISTS user_quotas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    ai_calls_used INT DEFAULT 0,
    ats_checks_used INT DEFAULT 0,
    last_reset_date DATETIME,
    CONSTRAINT fk_quota_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 5. Verification OTPs table
CREATE TABLE IF NOT EXISTS verification_otps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    otp_code VARCHAR(10) NOT NULL,
    type VARCHAR(30) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date DATETIME NOT NULL,
    CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Seed initial roles
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');
