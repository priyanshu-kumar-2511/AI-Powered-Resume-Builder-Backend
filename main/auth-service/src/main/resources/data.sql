-- Initial Seed Data for Auth Service
-- Adds default roles and a sample admin/user

-- Roles
INSERT INTO roles (name)
SELECT 'ROLE_USER' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_USER');

INSERT INTO roles (name)
SELECT 'ROLE_ADMIN' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN');

-- Sample User (Password: password123)
INSERT INTO users (full_name, age, mobile_number, email, username, password, is_active, enabled, subscription_plan, provider, created_at, updated_at)
SELECT 'John Doe', 25, '1234567890', 'john@example.com', 'johndoe', '$2a$10$8.UnVuG9HHgffUDAlk8q6uy5akLPNndzqBzvBxWq31.7Z6ux8YZqy', true, true, 'FREE', 'LOCAL', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'johndoe');

-- Admin User (Password: password123)
INSERT INTO users (full_name, age, mobile_number, email, username, password, is_active, enabled, subscription_plan, provider, created_at, updated_at)
SELECT 'System Admin', 30, '0000000000', 'admin@airesume.com', 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8q6uy5akLPNndzqBzvBxWq31.7Z6ux8YZqy', true, true, 'PREMIUM', 'LOCAL', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

-- Link User to Role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'johndoe' AND r.name = 'ROLE_USER'
AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = u.id AND role_id = r.id);

-- Link Admin to Role
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
AND NOT EXISTS (SELECT 1 FROM user_roles WHERE user_id = u.id AND role_id = r.id);

-- Custom Updates requested by user
-- 1. Change 'priyanshu' role to ROLE_ADMIN
UPDATE user_roles SET role_id = (SELECT id FROM roles WHERE name = 'ROLE_ADMIN') 
WHERE user_id = (SELECT id FROM users WHERE username = 'priyanshu');

-- 2. Upgrade 'akshay' to PREMIUM (By username and email for reliability)
UPDATE users SET subscription_plan = 'PREMIUM' WHERE username = 'akshay' OR email = 'akshaymaapapa123@gmail.com';
