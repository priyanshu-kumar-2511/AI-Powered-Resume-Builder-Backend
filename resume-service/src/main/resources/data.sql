-- Initial Seed Data for Resumes
-- Adds a sample resume for the first user

INSERT INTO resumes (user_id, title, target_job_title, template_id, ats_score, status, language, is_public, view_count, created_at, updated_at)
SELECT 1, 'Software Engineer Sample', 'Full Stack Developer', 1, 85, 'COMPLETE', 'en', true, 10, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM resumes WHERE title = 'Software Engineer Sample');
