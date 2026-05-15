-- Initial Seed Data for Section Service
-- Adds sections for the sample resume (ID 1)

-- Summary Section
INSERT INTO resume_sections (resume_id, section_type, title, content, display_order, is_visible, ai_generated, created_at, updated_at)
SELECT 1, 'SUMMARY', 'Professional Summary', 'Passionate Full Stack Developer with 5+ years of experience in building scalable web applications using Spring Boot and Angular.', 1, true, true, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM resume_sections WHERE resume_id = 1 AND section_type = 'SUMMARY');

-- Experience Section
INSERT INTO resume_sections (resume_id, section_type, title, content, display_order, is_visible, ai_generated, created_at, updated_at)
SELECT 1, 'EXPERIENCE', 'Work Experience', '[{"company": "Tech Corp", "title": "Senior Developer", "duration": "2020 - Present", "description": "Led a team of 5 to develop a cloud-based CRM."}, {"company": "Web Solutions", "title": "Junior Developer", "duration": "2018 - 2020", "description": "Maintained and upgraded legacy JSP applications."}]', 2, true, false, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM resume_sections WHERE resume_id = 1 AND section_type = 'EXPERIENCE');

-- Skills Section
INSERT INTO resume_sections (resume_id, section_type, title, content, display_order, is_visible, ai_generated, created_at, updated_at)
SELECT 1, 'SKILLS', 'Technical Skills', 'Java, Spring Boot, Angular, MySQL, Docker, Kubernetes', 3, true, true, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM resume_sections WHERE resume_id = 1 AND section_type = 'SKILLS');
