-- ============================================================
-- AI Resume Builder — Template Seed Data
-- 6 Templates: 3 FREE + 3 PREMIUM
-- Based on uploaded resume design samples
-- ============================================================

-- ── TEMPLATE 1 (FREE): Classic ATS ─────────────────────────
-- Based on: White and Blue Minimalist ATS Resume (Priyanshu Kumar style)
INSERT INTO templates (name, description, thumbnail_url, category, tier, html_layout, css_styles, is_active, usage_count, created_at, updated_at)
SELECT
  'Classic ATS',
  'Clean single-column ATS-optimised layout with horizontal contact bar and bold section dividers. Perfect for freshers and IT roles.',
  'assets/templates/classic-ats.svg',
  'ATS_OPTIMISED',
  'FREE',
'<div class="resume">
  <header class="header">
    <h1 class="name">{{fullName}}</h1>
    <div class="contact-bar">
      <span>📞 {{phone}}</span>
      <span>✉️ {{email}}</span>
      <span>🔗 {{linkedin}}</span>
      <span>💻 {{github}}</span>
    </div>
  </header>

  <section class="section">
    <h2 class="section-title">Career Objective</h2>
    <p class="summary-text">{{summary}}</p>
  </section>

  <section class="section">
    <h2 class="section-title">Education</h2>
    {{#education}}
    <div class="edu-item">
      <div class="edu-row">
        <strong class="edu-degree">{{degree}}</strong>
        <span class="edu-dates">{{startYear}} – {{endYear}}</span>
      </div>
      <div class="edu-row">
        <span class="edu-institution">{{institution}}</span>
        <span class="edu-grade">{{grade}}</span>
      </div>
    </div>
    {{/education}}
  </section>

  <section class="section two-col-section">
    <div class="col">
      <h2 class="section-title">Technical Skills</h2>
      <ul class="bullet-list">
        {{#technicalSkills}}<li>{{name}}</li>{{/technicalSkills}}
      </ul>
    </div>
    <div class="col">
      <h2 class="section-title">Soft Skills</h2>
      <ul class="bullet-list">
        {{#softSkills}}<li>{{name}}</li>{{/softSkills}}
      </ul>
    </div>
  </section>

  <section class="section">
    <h2 class="section-title">Experience</h2>
    {{#experience}}
    <div class="exp-item">
      <div class="exp-header">
        <strong class="exp-company">{{company}}</strong>
        <span class="exp-dates">{{startDate}} – {{endDate}}</span>
      </div>
      <div class="exp-role">{{role}}</div>
      <ul class="bullet-list">
        {{#bullets}}<li>{{text}}</li>{{/bullets}}
      </ul>
    </div>
    {{/experience}}
  </section>

  <section class="section">
    <h2 class="section-title">Projects</h2>
    {{#projects}}
    <div class="project-item">
      <div class="project-header">
        <strong>{{title}}</strong>
        <span class="exp-dates">{{dates}}</span>
      </div>
      <ul class="bullet-list">
        {{#bullets}}<li>{{text}}</li>{{/bullets}}
      </ul>
    </div>
    {{/projects}}
  </section>

  <section class="section">
    <h2 class="section-title">Certifications</h2>
    {{#certifications}}
    <div class="cert-row">
      <span>• {{name}}</span>
      <span class="exp-dates">{{date}}</span>
    </div>
    {{/certifications}}
  </section>
</div>',
'@import url("https://fonts.googleapis.com/css2?family=Times+New+Roman&family=Garamond&display=swap");

* { box-sizing: border-box; margin: 0; padding: 0; }

body { background: #fff; }

.resume {
  font-family: "Times New Roman", Times, serif;
  max-width: 780px;
  margin: 0 auto;
  padding: 36px 40px;
  color: #111;
  font-size: 13px;
  line-height: 1.55;
  background: #fff;
}

.header {
  text-align: center;
  margin-bottom: 14px;
}

.name {
  font-size: 26px;
  font-weight: 700;
  font-family: "Times New Roman", serif;
  letter-spacing: 0.02em;
  margin-bottom: 8px;
}

.contact-bar {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 12px;
  color: #333;
  border-top: 1.5px solid #111;
  border-bottom: 1.5px solid #111;
  padding: 6px 0;
}

.section { margin-bottom: 16px; }

.section-title {
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  border-bottom: 1.5px solid #111;
  padding-bottom: 3px;
  margin-bottom: 10px;
}

.two-col-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.col .section-title { margin-top: 0; }

.edu-item { margin-bottom: 8px; }
.edu-row { display: flex; justify-content: space-between; }
.edu-degree { font-weight: 600; }
.edu-institution { color: #333; }
.edu-dates, .edu-grade { font-size: 12px; color: #444; }

.exp-item, .project-item { margin-bottom: 12px; }

.exp-header, .project-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.exp-company { font-weight: 700; }
.exp-role { font-style: italic; font-size: 12.5px; margin: 2px 0 5px; }
.exp-dates { font-size: 12px; color: #444; }

.bullet-list {
  padding-left: 18px;
  margin-top: 4px;
}
.bullet-list li { margin-bottom: 3px; font-size: 12.5px; }

.cert-row {
  display: flex;
  justify-content: space-between;
  font-size: 12.5px;
  margin-bottom: 4px;
}',
true, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'Classic ATS');

-- ── TEMPLATE 2 (FREE): Corporate Clean ──────────────────────
-- Based on: Black and White Professional Corporate Resume (Olivia Sanchez / Estelle Darcy style)
INSERT INTO templates (name, description, thumbnail_url, category, tier, html_layout, css_styles, is_active, usage_count, created_at, updated_at)
SELECT
  'Corporate Clean',
  'Elegant black and white corporate layout with shaded section headers. Great for management, business and admin roles.',
  'assets/templates/corporate-clean.svg',
  'PROFESSIONAL',
  'FREE',
'<div class="resume">
  <header class="header">
    <h1 class="name">{{fullName}}</h1>
    <p class="job-title">{{jobTitle}}</p>
    <div class="contact-line">{{email}} | {{phone}} | {{location}}</div>
  </header>

  <section class="section">
    <div class="section-title-bar"><h2>SUMMARY</h2></div>
    <p class="summary-text">{{summary}}</p>
  </section>

  <section class="section">
    <div class="section-title-bar"><h2>WORK EXPERIENCE</h2></div>
    {{#experience}}
    <div class="exp-item">
      <div class="exp-header">
        <strong class="exp-role">{{role}}, {{company}}</strong>
        <span class="exp-dates">{{startDate}} – {{endDate}}</span>
      </div>
      <ul class="bullet-list">
        {{#bullets}}<li>{{text}}</li>{{/bullets}}
      </ul>
    </div>
    {{/experience}}
  </section>

  <section class="section">
    <div class="section-title-bar"><h2>PROJECTS</h2></div>
    {{#projects}}
    <div class="exp-item">
      <div class="exp-header">
        <strong class="exp-role">{{title}}</strong>
        <span class="exp-dates">{{dates}}</span>
      </div>
      <ul class="bullet-list">
        {{#bullets}}<li>{{text}}</li>{{/bullets}}
      </ul>
    </div>
    {{/projects}}
  </section>

  <section class="section">
    <div class="section-title-bar"><h2>CERTIFICATIONS</h2></div>
    {{#certifications}}
    <div class="exp-item" style="margin-bottom: 8px;">
      <div class="exp-header">
        <strong class="exp-role">{{name}}</strong>
        <span class="exp-dates">{{date}}</span>
      </div>
    </div>
    {{/certifications}}
  </section>

  <section class="section">
    <div class="section-title-bar"><h2>EDUCATION</h2></div>
    {{#education}}
    <div class="edu-item">
      <div class="edu-header">
        <strong>{{degree}}</strong>
        <span class="exp-dates">{{startYear}} – {{endYear}}</span>
      </div>
      <div class="edu-institution">{{institution}}</div>
      <ul class="bullet-list">
        {{#highlights}}<li>{{text}}</li>{{/highlights}}
      </ul>
    </div>
    {{/education}}
  </section>

  <section class="section">
    <div class="section-title-bar"><h2>KEY SKILLS</h2></div>
    <div class="skills-grid">
      {{#skills}}<span class="skill-item">• {{name}}</span>{{/skills}}
    </div>
  </section>
</div>',
'@import url("https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600;700&display=swap");

* { box-sizing: border-box; margin: 0; padding: 0; }
body { background: #fff; }

.resume {
  font-family: "Open Sans", Arial, sans-serif;
  max-width: 780px;
  margin: 0 auto;
  padding: 40px;
  color: #111;
  font-size: 13px;
  line-height: 1.6;
  background: #fff;
}

.header {
  text-align: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #111;
}

.name {
  font-size: 28px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 6px;
}

.job-title {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #444;
  margin-bottom: 8px;
}

.contact-line {
  font-size: 12.5px;
  color: #555;
}

.section { margin-bottom: 20px; }

.section-title-bar {
  background: #f0f0f0;
  padding: 5px 10px;
  margin-bottom: 12px;
}

.section-title-bar h2 {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #111;
}

.exp-item, .edu-item { margin-bottom: 14px; }

.exp-header, .edu-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.exp-role { font-weight: 700; font-size: 13px; }
.exp-dates { font-size: 12px; color: #555; }
.edu-institution { font-size: 12.5px; color: #333; margin: 3px 0; }

.bullet-list {
  padding-left: 18px;
  margin-top: 6px;
}
.bullet-list li {
  margin-bottom: 4px;
  font-size: 12.5px;
  text-align: justify;
}

.skills-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px 12px;
}

.skill-item { font-size: 12.5px; }',
true, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'Corporate Clean');

-- ── TEMPLATE 3 (FREE): Modern Sidebar ────────────────────────
-- Based on: Creative Portfolio dark sidebar style
INSERT INTO templates (name, description, thumbnail_url, category, tier, html_layout, css_styles, is_active, usage_count, created_at, updated_at)
SELECT
  'Modern Sidebar',
  'Stylish two-column layout with dark sidebar for skills and contact info. Ideal for creative and modern tech roles.',
  'assets/templates/modern-sidebar.svg',
  'MODERN',
  'FREE',
'<div class="creative-resume">
  <div class="sidebar">
    <div class="profile-section">
      <h1 class="name">{{fullName}}</h1>
      <p class="title">{{jobTitle}}</p>
    </div>

    <div class="side-section">
      <h3>Contact</h3>
      <p>📧 {{email}}</p>
      <p>📞 {{phone}}</p>
      <p>📍 {{location}}</p>
      <p>🔗 {{linkedin}}</p>
    </div>

    <div class="side-section">
      <h3>Skills</h3>
      <ul class="skills-list">
        {{#skills}}<li>{{name}}</li>{{/skills}}
      </ul>
    </div>

    <div class="side-section">
      <h3>Education</h3>
      {{#education}}
      <div class="side-edu">
        <strong class="side-degree">{{degree}}</strong>
        <span class="side-inst">{{institution}}</span>
        <span class="side-dates">{{startYear}} – {{endYear}}</span>
      </div>
      {{/education}}
    </div>

    <div class="side-section">
      <h3>Certifications</h3>
      {{#certifications}}
      <div class="side-edu">
        <strong class="side-degree">{{name}}</strong>
        <span class="side-dates">{{date}}</span>
      </div>
      {{/certifications}}
    </div>
  </div>

  <div class="main-content">
    <section class="main-section">
      <h2>About Me</h2>
      <p>{{summary}}</p>
    </section>

    <section class="main-section">
      <h2>Area of Expertise</h2>
      <div class="expertise-grid">
        {{#expertise}}<span>{{name}}</span>{{/expertise}}
      </div>
    </section>

    <section class="main-section">
      <h2>Professional Experience</h2>
      {{#experience}}
      <div class="exp-block">
        <div class="exp-meta">
          <strong>{{role}}, {{company}}</strong>
          <span class="exp-dates">{{startDate}} – {{endDate}}</span>
        </div>
        <ul class="main-bullets">
          {{#bullets}}<li>{{text}}</li>{{/bullets}}
        </ul>
      </div>
      {{/experience}}
    </section>

    <section class="main-section">
      <h2>Projects</h2>
      {{#projects}}
      <div class="exp-block">
        <div class="exp-meta">
          <strong>{{title}}</strong>
          <span class="exp-dates">{{dates}}</span>
        </div>
        <ul class="main-bullets">
          {{#bullets}}<li>{{text}}</li>{{/bullets}}
        </ul>
      </div>
      {{/projects}}
    </section>

    <section class="main-section">
      <h2>Additional Information</h2>
      <ul class="main-bullets">
        {{#additionalInfo}}<li><strong>{{label}}:</strong> {{value}}</li>{{/additionalInfo}}
      </ul>
    </section>
  </div>
</div>',
'@import url("https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700&display=swap");

* { box-sizing: border-box; margin: 0; padding: 0; }
body { background: #fff; }

.creative-resume {
  display: grid;
  grid-template-columns: 260px 1fr;
  min-height: 100vh;
  font-family: "Outfit", sans-serif;
  font-size: 13px;
  background: #fff;
}

.sidebar {
  background: #111827;
  color: #fff;
  padding: 36px 24px;
}

.name {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 5px;
  line-height: 1.2;
}

.title {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #9ca3af;
  margin-bottom: 28px;
}

.side-section { margin-bottom: 24px; }

.side-section h3 {
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: #60a5fa;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  padding-bottom: 5px;
  margin-bottom: 10px;
}

.side-section p {
  font-size: 12px;
  color: #d1d5db;
  margin-bottom: 6px;
}

.skills-list { list-style: none; }
.skills-list li {
  background: rgba(255,255,255,0.06);
  margin-bottom: 5px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  color: #e5e7eb;
}

.side-edu { margin-bottom: 10px; }
.side-degree { display: block; font-size: 12px; font-weight: 600; color: #fff; }
.side-inst { display: block; font-size: 11px; color: #9ca3af; }
.side-dates { font-size: 11px; color: #6b7280; }

.main-content { padding: 36px 36px; background: #fff; }

.main-section { margin-bottom: 22px; }

.main-section h2 {
  font-size: 14px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 5px;
  margin-bottom: 12px;
  color: #111;
}

.expertise-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  font-size: 12.5px;
  color: #374151;
}

.expertise-grid span {
  background: rgba(96,165,250,0.08);
  border: 1px solid rgba(96,165,250,0.35);
  border-radius: 5px;
  padding: 4px 8px;
  font-size: 11px;
  font-weight: 600;
  color: #1e40af;
  text-align: center;
  display: block;
}

.exp-block { margin-bottom: 14px; }

.exp-meta {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 5px;
}

.exp-meta strong { font-size: 13px; color: #111; }
.exp-dates { font-size: 12px; color: #6b7280; }

.main-bullets { padding-left: 16px; }
.main-bullets li { margin-bottom: 4px; font-size: 12.5px; color: #374151; }',
true, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'Modern Sidebar');

-- ── TEMPLATE 4 (PREMIUM): Executive Navy ─────────────────────
-- Based on: Black and White Clean Professional A4 (Emaa Warner / dark header style)
INSERT INTO templates (name, description, thumbnail_url, category, tier, html_layout, css_styles, is_active, usage_count, created_at, updated_at)
SELECT
  'Executive Navy',
  'Bold dark-header executive template with teal accent section titles. Premium look for senior professionals and executives.',
  'assets/templates/executive-navy.svg',
  'PROFESSIONAL',
  'PREMIUM',
'<div class="resume">
  <header class="header">
    <h1 class="name">{{fullName}}</h1>
    <div class="contact-bar">{{email}} | {{phone}} | {{location}}</div>
  </header>

  <section class="section">
    <h2 class="section-title">Summary</h2>
    <p class="summary-text">{{summary}}</p>
  </section>

  <section class="section">
    <h2 class="section-title">Work Experience</h2>
    {{#experience}}
    <div class="exp-item">
      <div class="exp-header">
        <strong class="exp-role">{{role}}</strong>
        <span class="exp-dates">{{startDate}} – {{endDate}}</span>
      </div>
      <div class="exp-company">{{company}}</div>
      <ul class="bullet-list">
        {{#bullets}}<li>{{text}}</li>{{/bullets}}
      </ul>
    </div>
    {{/experience}}
  </section>

  <section class="section">
    <h2 class="section-title">Projects</h2>
    {{#projects}}
    <div class="exp-item">
      <div class="exp-header">
        <strong class="exp-role">{{title}}</strong>
        <span class="exp-dates">{{dates}}</span>
      </div>
      <ul class="bullet-list">
        {{#bullets}}<li>{{text}}</li>{{/bullets}}
      </ul>
    </div>
    {{/projects}}
  </section>

  <section class="section">
    <h2 class="section-title">Education</h2>
    {{#education}}
    <div class="edu-item">
      <strong>{{degree}}</strong>
      <div class="edu-inst">{{institution}}</div>
      <div class="edu-dates">{{startYear}} – {{endYear}}</div>
      <ul class="bullet-list">
        {{#highlights}}<li>{{text}}</li>{{/highlights}}
      </ul>
    </div>
    {{/education}}
  </section>

  <section class="section">
    <h2 class="section-title">Skills</h2>
    <ul class="bullet-list">
      {{#skills}}<li>{{name}}</li>{{/skills}}
    </ul>
  </section>

  <section class="section">
    <h2 class="section-title">Awards</h2>
    <ul class="bullet-list">
      {{#awards}}<li>{{name}}</li>{{/awards}}
    </ul>
  </section>

  <section class="section">
    <h2 class="section-title">Certifications</h2>
    <ul class="bullet-list">
      {{#certifications}}<li><strong>{{name}}</strong> <span style="font-size: 11.5px; color: #64748b;">{{date}}</span></li>{{/certifications}}
    </ul>
  </section>
</div>',
'@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap");

* { box-sizing: border-box; margin: 0; padding: 0; }
body { background: #fff; }

.resume {
  font-family: "Inter", sans-serif;
  max-width: 780px;
  margin: 0 auto;
  padding: 0 0 40px;
  color: #111;
  font-size: 13px;
  line-height: 1.6;
  background: #fff;
}

.header {
  background: #0f172a;
  color: #fff;
  text-align: center;
  padding: 36px 40px 28px;
  margin-bottom: 28px;
}

.name {
  font-size: 36px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin-bottom: 10px;
  color: #fff;
}

.contact-bar {
  font-size: 12.5px;
  color: #94a3b8;
  letter-spacing: 0.03em;
}

.section {
  padding: 0 40px;
  margin-bottom: 22px;
}

.section-title {
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #0891b2;
  border-bottom: 1.5px solid #e2e8f0;
  padding-bottom: 4px;
  margin-bottom: 12px;
}

.summary-text { font-size: 12.5px; color: #334155; text-align: justify; }

.exp-item { margin-bottom: 14px; }
.exp-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.exp-role { font-weight: 700; font-size: 13px; }
.exp-company { font-size: 12px; color: #0891b2; font-weight: 600; margin: 3px 0 6px; }
.exp-dates { font-size: 12px; color: #64748b; }

.edu-item { margin-bottom: 12px; }
.edu-item strong { font-size: 13px; }
.edu-inst { font-size: 12px; color: #555; margin: 2px 0; }
.edu-dates { font-size: 11.5px; color: #64748b; margin-bottom: 4px; }

.bullet-list { padding-left: 16px; margin-top: 4px; }
.bullet-list li { margin-bottom: 4px; font-size: 12.5px; color: #334155; }',
true, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'Executive Navy');

-- ── TEMPLATE 5 (PREMIUM): Minimalist Timeline ────────────────
-- Based on: Anaisha Parvati clean white layout with timeline sidebar
INSERT INTO templates (name, description, thumbnail_url, category, tier, html_layout, css_styles, is_active, usage_count, created_at, updated_at)
SELECT
  'Minimalist Timeline',
  'Ultra-clean white layout with timeline-style dates on the left and content on the right. Elegant for senior management roles.',
  'assets/templates/minimalist-timeline.svg',
  'MINIMALIST',
  'PREMIUM',
'<div class="resume">
  <header class="header">
    <div class="header-text">
      <h1 class="name">{{fullName}}</h1>
      <p class="job-title">{{jobTitle}}</p>
    </div>
    <div class="contact-icons">
      <span>📞 {{phone}}</span>
      <span>📍 {{location}}</span>
      <span>✉️ {{email}}</span>
    </div>
  </header>

  <section class="section">
    <h2 class="section-title">About Me</h2>
    <p class="summary-text">{{summary}}</p>
  </section>

  <section class="section">
    <h2 class="section-title">Education</h2>
    {{#education}}
    <div class="timeline-item">
      <div class="timeline-left">
        <span class="tl-dates">{{startYear}} – {{endYear}}</span>
        <span class="tl-sub">{{institution}}</span>
      </div>
      <div class="timeline-right">
        <strong class="tl-title">{{degree}}</strong>
        <p class="tl-desc">{{description}}</p>
      </div>
    </div>
    {{/education}}
  </section>

  <section class="section">
    <h2 class="section-title">Experience</h2>
    {{#experience}}
    <div class="timeline-item">
      <div class="timeline-left">
        <span class="tl-dates">{{startDate}} – {{endDate}}</span>
        <span class="tl-sub">{{company}}</span>
      </div>
      <div class="timeline-right">
        <strong class="tl-title">{{role}}</strong>
        <p class="tl-desc">{{description}}</p>
      </div>
    </div>
    {{/experience}}
  </section>

  <section class="section">
    <h2 class="section-title">Certifications</h2>
    {{#certifications}}
    <div class="timeline-item">
      <div class="timeline-left">
        <span class="tl-dates">{{date}}</span>
      </div>
      <div class="timeline-right">
        <strong class="tl-title">{{name}}</strong>
      </div>
    </div>
    {{/certifications}}
  </section>

  <section class="section">
    <h2 class="section-title">Skills</h2>
    <div class="skills-row">
      {{#skills}}<span class="skill-pill">{{name}}</span>{{/skills}}
    </div>
  </section>

  <section class="section">
    <h2 class="section-title">References</h2>
    <div class="refs-grid">
      {{#references}}
      <div class="ref-card">
        <strong>{{name}}</strong>
        <span class="ref-title">{{title}}</span>
        <span class="ref-contact">📞 {{phone}}</span>
        <span class="ref-contact">🌐 {{social}}</span>
      </div>
      {{/references}}
    </div>
  </section>
</div>',
'@import url("https://fonts.googleapis.com/css2?family=Raleway:wght@400;600;700&display=swap");

* { box-sizing: border-box; margin: 0; padding: 0; }
body { background: #fff; }

.resume {
  font-family: "Raleway", sans-serif;
  max-width: 780px;
  margin: 0 auto;
  padding: 40px;
  color: #1e293b;
  font-size: 13px;
  line-height: 1.6;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding-bottom: 20px;
  border-bottom: 2px solid #0f172a;
  margin-bottom: 24px;
}

.name {
  font-size: 30px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #0f172a;
  margin-bottom: 5px;
}

.job-title {
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.18em;
  color: #64748b;
  margin-bottom: 12px;
}

.contact-icons {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #475569;
}

.section { margin-bottom: 22px; }

.section-title {
  font-size: 13px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #0f172a;
  border-bottom: 1.5px solid #cbd5e1;
  padding-bottom: 4px;
  margin-bottom: 14px;
}

.summary-text { font-size: 12.5px; color: #475569; }

.timeline-item {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 20px;
  margin-bottom: 14px;
}

.timeline-left {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.tl-dates { font-size: 12px; font-weight: 700; color: #0f172a; }
.tl-sub   { font-size: 11.5px; color: #64748b; }

.tl-title { font-size: 13px; font-weight: 700; display: block; margin-bottom: 4px; }
.tl-desc  { font-size: 12px; color: #475569; }

.skills-row { display: flex; flex-wrap: wrap; gap: 8px; }

.skill-pill {
  background: #f1f5f9;
  border: 1px solid #cbd5e1;
  border-radius: 20px;
  padding: 4px 14px;
  font-size: 12px;
  color: #334155;
}

.refs-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.ref-card {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.ref-card strong { font-size: 13px; color: #0f172a; }
.ref-title   { font-size: 12px; color: #64748b; }
.ref-contact { font-size: 11.5px; color: #475569; }',
true, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'Minimalist Timeline');

-- ── TEMPLATE 6 (PREMIUM): Creative Teal ──────────────────────
-- Based on: UX Designer / Estelle Darcy layout — bold left border accents
INSERT INTO templates (name, description, thumbnail_url, category, tier, html_layout, css_styles, is_active, usage_count, created_at, updated_at)
SELECT
  'Creative Teal',
  'Modern creative resume with teal accent borders, bold section headers and clean typography. Perfect for designers, UX and creative roles.',
  'assets/templates/creative-teal.svg',
  'CREATIVE',
  'PREMIUM',
'<div class="resume">
  <header class="header">
    <div class="header-main">
      <h1 class="name">{{fullName}}</h1>
      <p class="job-title">{{jobTitle}}</p>
    </div>
    <div class="contact-row">{{location}} | {{email}} | {{website}}</div>
  </header>

  <p class="intro">{{summary}}</p>

  <div class="body-grid">
    <main class="main-col">

      <section class="section">
        <h2 class="section-title">Area of Expertise</h2>
        <div class="expertise-grid">
          {{#expertise}}<span>{{name}}</span>{{/expertise}}
        </div>
      </section>

      <section class="section">
        <h2 class="section-title">Key Achievements</h2>
        <ul class="achievement-list">
          {{#achievements}}
          <li><strong>{{title}}.</strong> {{description}}</li>
          {{/achievements}}
        </ul>
      </section>

      <section class="section">
        <h2 class="section-title">Professional Experience</h2>
        {{#experience}}
        <div class="exp-item">
          <div class="exp-header">
            <strong class="exp-role">{{role}}, {{company}}</strong>
            <span class="exp-dates">{{startDate}} – {{endDate}}</span>
          </div>
          <ul class="bullet-list">
            {{#bullets}}<li>{{text}}</li>{{/bullets}}
          </ul>
        </div>
        {{/experience}}
      </section>

      <section class="section">
        <h2 class="section-title">Education</h2>
        {{#education}}
        <div class="edu-item">
          <div class="edu-header">
            <strong>{{degree}}</strong>
            <span class="exp-dates">{{startYear}} – {{endYear}}</span>
          </div>
          <div class="edu-inst">{{institution}}</div>
          <ul class="bullet-list">
            {{#highlights}}<li>{{text}}</li>{{/highlights}}
          </ul>
        </div>
        {{/education}}
      </section>

    </main>

    <aside class="side-col">
      <section class="section">
        <h2 class="section-title-sm">Certifications</h2>
        <ul class="side-list">
          {{#certifications}}<li><strong>{{name}}</strong> <br><span style="font-size: 11px; color: #475569;">{{date}}</span></li>{{/certifications}}
        </ul>
      </section>

      <section class="section">
        <h2 class="section-title-sm">Additional Information</h2>
        <ul class="side-list">
          {{#additionalInfo}}<li><strong>{{label}}:</strong> {{value}}</li>{{/additionalInfo}}
        </ul>
      </section>
    </aside>
  </div>
</div>',
'@import url("https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;700&display=swap");

* { box-sizing: border-box; margin: 0; padding: 0; }
body { background: #fff; }

.resume {
  font-family: "DM Sans", sans-serif;
  max-width: 780px;
  margin: 0 auto;
  padding: 40px;
  color: #111;
  font-size: 13px;
  line-height: 1.6;
  background: #fff;
}

.header {
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 2px solid #111;
}

.name {
  font-size: 26px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 3px;
}

.job-title {
  font-size: 13px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #555;
  margin-bottom: 8px;
}

.contact-row {
  font-size: 12px;
  color: #555;
}

.intro {
  font-size: 12.5px;
  color: #374151;
  margin-bottom: 20px;
  text-align: justify;
}

.body-grid {
  display: grid;
  grid-template-columns: 1fr 220px;
  gap: 28px;
}

.section { margin-bottom: 20px; }

.section-title {
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  border-bottom: 1.5px solid #111;
  padding-bottom: 4px;
  margin-bottom: 12px;
}

.section-title-sm {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  border-bottom: 1.5px solid #0d9488;
  padding-bottom: 3px;
  margin-bottom: 10px;
  color: #0d9488;
}

.expertise-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 4px 16px;
  font-size: 12.5px;
  color: #374151;
}

.achievement-list { padding-left: 16px; }
.achievement-list li { margin-bottom: 8px; font-size: 12.5px; color: #374151; }

.exp-item, .edu-item { margin-bottom: 14px; }
.exp-header, .edu-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}
.exp-role { font-weight: 700; font-size: 13px; }
.exp-dates { font-size: 12px; color: #6b7280; }
.edu-inst { font-size: 12px; color: #374151; margin: 3px 0; }

.bullet-list { padding-left: 16px; margin-top: 6px; }
.bullet-list li { margin-bottom: 4px; font-size: 12.5px; color: #374151; }

.side-col { padding-top: 0; }

.side-list { list-style: none; }
.side-list li { font-size: 12px; color: #374151; margin-bottom: 8px; }',
true, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'Creative Teal');

-- TEMPLATE 7 (FREE): Simple Ivory
INSERT INTO templates (name, description, thumbnail_url, category, tier, html_layout, css_styles, is_active, usage_count, created_at, updated_at)
SELECT
  'Simple Ivory',
  'Warm, elegant one-column template with classic serif styling and minimal accents. Easy to read and great for general professional roles.',
  'assets/templates/simple-ivory.svg',
  'PROFESSIONAL',
  'FREE',
'<div class="ivory-resume">
  <header class="ivory-header">
    <h1>{{fullName}}</h1>
    <p>{{jobTitle}}</p>
    <div class="ivory-contact">{{email}} | {{phone}} | {{location}}</div>
  </header>

  <section class="section">
    <h2>Professional Summary</h2>
    <p>{{{summary}}}</p>
  </section>

  <section class="section">
    <h2>Experience</h2>
    {{#experience}}
    <article class="ivory-block">
      <div class="ivory-row">
        <strong>{{role}}, {{company}}</strong>
        <span>{{startDate}} - {{endDate}}</span>
      </div>
      <ul>{{#bullets}}<li>{{text}}</li>{{/bullets}}</ul>
    </article>
    {{/experience}}
  </section>

  <div class="ivory-grid">
    <section class="section">
      <h2>Education</h2>
      {{#education}}
      <article class="ivory-block">
        <strong>{{degree}}</strong>
        <div>{{institution}}</div>
        <span>{{startYear}} - {{endYear}}</span>
      </article>
      {{/education}}
    </section>

    <section class="section">
      <h2>Skills</h2>
      <ul class="plain-list">{{#skills}}<li>{{name}}</li>{{/skills}}</ul>
    </section>
  </div>

  <section class="section">
    <h2>Projects</h2>
    {{#projects}}
    <article class="ivory-block">
      <div class="ivory-row">
        <strong>{{title}}</strong>
        <span>{{dates}}</span>
      </div>
      <ul>{{#bullets}}<li>{{text}}</li>{{/bullets}}</ul>
    </article>
    {{/projects}}
  </section>

  <section class="section">
    <h2>Certifications</h2>
    <ul class="plain-list">{{#certifications}}<li>{{name}} <span>{{date}}</span></li>{{/certifications}}</ul>
  </section>
</div>',
'* { box-sizing: border-box; margin: 0; padding: 0; } body { margin: 0; background: #fff; color: #1f2937; font-family: Georgia, "Times New Roman", serif; } .ivory-resume { max-width: 820px; margin: 0 auto; padding: 42px; background: #fffaf4; } .ivory-header { text-align: center; padding-bottom: 18px; margin-bottom: 22px; border-bottom: 1px solid #d6c8b8; } .ivory-header h1 { margin: 0; font-size: 31px; color: #2b2118; } .ivory-header p { margin: 6px 0 8px; font-size: 12px; letter-spacing: 0.18em; text-transform: uppercase; color: #7c6754; } .ivory-contact { font-size: 11px; color: #6b5b4d; } .section { margin-bottom: 22px; } .section h2 { margin: 0 0 12px; font-size: 12px; letter-spacing: 0.14em; text-transform: uppercase; color: #6c4f3d; border-bottom: 1px solid #d6c8b8; padding-bottom: 5px; } .section p, .section li, .section div, .section span { font-size: 12.2px; } .ivory-block { margin-bottom: 12px; } .ivory-row { display: flex; justify-content: space-between; gap: 12px; } .ivory-row span, .section span { color: #7c6b5b; font-size: 11px; } .ivory-block ul, .plain-list { margin: 6px 0 0; padding-left: 18px; } .ivory-grid { display: grid; grid-template-columns: 1.1fr 0.9fr; gap: 28px; }',
true, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'Simple Ivory');

-- TEMPLATE 8 (FREE): Clean Columns
INSERT INTO templates (name, description, thumbnail_url, category, tier, html_layout, css_styles, is_active, usage_count, created_at, updated_at)
SELECT
  'Clean Columns',
  'Balanced two-column resume with a light information sidebar and clean content flow. Simple, modern and interview-friendly.',
  'assets/templates/clean-columns.svg',
  'PROFESSIONAL',
  'FREE',
'<div class="columns-resume">
  <aside class="columns-side">
    <section class="side-card">
      <h1>{{fullName}}</h1>
      <p>{{jobTitle}}</p>
      <div>{{email}}</div>
      <div>{{phone}}</div>
      <div>{{location}}</div>
      <div>{{linkedin}}</div>
    </section>

    <section class="side-card">
      <h2>Skills</h2>
      <ul>{{#skills}}<li>{{name}}</li>{{/skills}}</ul>
    </section>

    <section class="side-card">
      <h2>Certifications</h2>
      <ul>{{#certifications}}<li>{{name}}</li>{{/certifications}}</ul>
    </section>
  </aside>

  <main class="columns-main">
    <section class="main-card">
      <h2>Summary</h2>
      <p>{{{summary}}}</p>
    </section>

    <section class="main-card">
      <h2>Experience</h2>
      {{#experience}}
      <article class="entry">
        <div class="entry-top">
          <strong>{{role}}</strong>
          <span>{{startDate}} - {{endDate}}</span>
        </div>
        <div class="entry-sub">{{company}}</div>
        <ul>{{#bullets}}<li>{{text}}</li>{{/bullets}}</ul>
      </article>
      {{/experience}}
    </section>

    <section class="main-card">
      <h2>Education</h2>
      {{#education}}
      <article class="entry">
        <div class="entry-top">
          <strong>{{degree}}</strong>
          <span>{{startYear}} - {{endYear}}</span>
        </div>
        <div class="entry-sub">{{institution}}</div>
      </article>
      {{/education}}
    </section>

    <section class="main-card">
      <h2>Projects</h2>
      {{#projects}}
      <article class="entry">
        <div class="entry-top">
          <strong>{{title}}</strong>
          <span>{{dates}}</span>
        </div>
        <ul>{{#bullets}}<li>{{text}}</li>{{/bullets}}</ul>
      </article>
      {{/projects}}
    </section>
  </main>
</div>',
'* { box-sizing: border-box; margin: 0; padding: 0; } body { margin: 0; background: #fff; color: #0f172a; font-family: "Segoe UI", Arial, sans-serif; } .columns-resume { display: grid; grid-template-columns: 220px 1fr; max-width: 860px; margin: 0 auto; background: #fff; } .columns-side { background: #f8fafc; border-right: 1px solid #dbe3ea; padding: 28px 20px; } .columns-main { padding: 28px 30px; } .side-card, .main-card { margin-bottom: 22px; } .side-card h1 { margin: 0 0 4px; font-size: 24px; } .side-card p { margin: 0 0 12px; font-size: 11px; letter-spacing: 0.14em; text-transform: uppercase; color: #475569; } .side-card h2, .main-card h2 { margin: 0 0 10px; font-size: 11px; text-transform: uppercase; letter-spacing: 0.14em; color: #0f766e; } .side-card ul, .main-card ul { margin: 6px 0 0; padding-left: 18px; } .side-card li, .main-card li, .side-card div, .main-card p, .entry-sub, .entry span { font-size: 11.5px; } .entry { margin-bottom: 12px; } .entry-top { display: flex; justify-content: space-between; gap: 12px; } .entry-sub { margin-top: 2px; color: #475569; } .entry span { color: #64748b; } .main-card { padding-bottom: 14px; border-bottom: 1px solid #e2e8f0; }',
true, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'Clean Columns');

-- TEMPLATE 9 (FREE): Minimal Mono
INSERT INTO templates (name, description, thumbnail_url, category, tier, html_layout, css_styles, is_active, usage_count, created_at, updated_at)
SELECT
  'Minimal Mono',
  'Compact mono-styled layout with crisp section labels and clean spacing. Great for ATS-first applications and technical resumes.',
  'assets/templates/minimal-mono.svg',
  'MINIMALIST',
  'FREE',
'<div class="mono-resume">
  <header class="mono-header">
    <div>
      <h1>{{fullName}}</h1>
      <p>{{jobTitle}}</p>
    </div>
    <div class="mono-contact">
      <span>{{email}}</span>
      <span>{{phone}}</span>
      <span>{{location}}</span>
    </div>
  </header>

  <section class="mono-section">
    <h2>Summary</h2>
    <p>{{{summary}}}</p>
  </section>

  <section class="mono-section">
    <h2>Skills</h2>
    <div class="mono-tags">{{#skills}}<span>{{name}}</span>{{/skills}}</div>
  </section>

  <section class="mono-section">
    <h2>Experience</h2>
    {{#experience}}
    <article class="mono-item">
      <div class="mono-top"><strong>{{role}} / {{company}}</strong><span>{{startDate}} - {{endDate}}</span></div>
      <ul>{{#bullets}}<li>{{text}}</li>{{/bullets}}</ul>
    </article>
    {{/experience}}
  </section>

  <div class="mono-grid">
    <section class="mono-section">
      <h2>Education</h2>
      {{#education}}
      <article class="mono-item">
        <div class="mono-top"><strong>{{degree}}</strong><span>{{startYear}} - {{endYear}}</span></div>
        <div>{{institution}}</div>
      </article>
      {{/education}}
    </section>

    <section class="mono-section">
      <h2>Projects</h2>
      {{#projects}}
      <article class="mono-item">
        <div class="mono-top"><strong>{{title}}</strong><span>{{dates}}</span></div>
        <ul>{{#bullets}}<li>{{text}}</li>{{/bullets}}</ul>
      </article>
      {{/projects}}
    </section>
  </div>
</div>',
'* { box-sizing: border-box; margin: 0; padding: 0; } body { margin: 0; background: #fff; color: #111827; font-family: "Courier New", monospace; } .mono-resume { max-width: 840px; margin: 0 auto; padding: 34px; background: #fff; } .mono-header { display: flex; justify-content: space-between; gap: 18px; padding-bottom: 16px; margin-bottom: 18px; border-bottom: 2px solid #111827; } .mono-header h1 { margin: 0; font-size: 28px; letter-spacing: 0.06em; } .mono-header p { margin: 6px 0 0; font-size: 11px; text-transform: uppercase; letter-spacing: 0.18em; color: #4b5563; } .mono-contact { display: flex; flex-direction: column; gap: 4px; font-size: 11px; text-align: right; color: #374151; } .mono-section { margin-bottom: 20px; } .mono-section h2 { margin: 0 0 10px; font-size: 11px; text-transform: uppercase; letter-spacing: 0.18em; color: #111827; background: #f3f4f6; padding: 6px 8px; } .mono-tags { display: flex; flex-wrap: wrap; gap: 8px; } .mono-tags span { border: 1px solid #d1d5db; padding: 4px 8px; font-size: 11px; } .mono-item { margin-bottom: 12px; } .mono-top { display: flex; justify-content: space-between; gap: 12px; margin-bottom: 4px; } .mono-top span { font-size: 11px; color: #6b7280; } .mono-item div, .mono-item li, .mono-section p { font-size: 11.5px; } .mono-item ul { margin: 4px 0 0; padding-left: 18px; } .mono-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }',
true, 0, NOW(), NOW()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM templates WHERE name = 'Minimal Mono');

-- ── UPDATE EXISTING DATABASE TEMPLATES TO CONSUME var(--primary) VARIABLE ──
UPDATE templates 
SET css_styles = '@import url("https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700&display=swap"); * { box-sizing: border-box; margin: 0; padding: 0; } body { background: #fff; } .creative-resume { display: grid; grid-template-columns: 260px 1fr; min-height: 100vh; font-family: "Outfit", sans-serif; font-size: 13px; background: #fff; } .sidebar { background: #111827; color: #fff; padding: 36px 24px; } .name { font-size: 22px; font-weight: 700; color: #fff; margin-bottom: 5px; line-height: 1.2; } .title { font-size: 12px; text-transform: uppercase; letter-spacing: 0.1em; color: #9ca3af; margin-bottom: 28px; } .side-section { margin-bottom: 24px; } .side-section h3 { font-size: 10px; text-transform: uppercase; letter-spacing: 0.12em; color: var(--primary, #60a5fa); border-bottom: 1px solid rgba(255,255,255,0.1); padding-bottom: 5px; margin-bottom: 10px; } .side-section p { font-size: 12px; color: #d1d5db; margin-bottom: 6px; } .skills-list { list-style: none; } .skills-list li { background: rgba(255,255,255,0.06); margin-bottom: 5px; padding: 4px 10px; border-radius: 4px; font-size: 12px; color: #e5e7eb; } .side-edu { margin-bottom: 10px; } .side-degree { display: block; font-size: 12px; font-weight: 600; color: #fff; } .side-inst { display: block; font-size: 11px; color: #9ca3af; } .side-dates { font-size: 11px; color: #6b7280; } .main-content { padding: 36px 36px; background: #fff; } .main-section { margin-bottom: 22px; } .main-section h2 { font-size: 14px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.07em; border-bottom: 2px solid #e5e7eb; padding-bottom: 5px; margin-bottom: 12px; color: #111; } .expertise-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px; font-size: 12.5px; color: #374151; } .expertise-grid span { background: color-mix(in srgb, var(--primary, #60a5fa) 8%, transparent); border: 1px solid color-mix(in srgb, var(--primary, #60a5fa) 35%, transparent); border-radius: 5px; padding: 4px 8px; font-size: 11px; font-weight: 600; color: var(--primary, #1e40af); text-align: center; display: block; } .exp-block { margin-bottom: 14px; } .exp-meta { display: flex; justify-content: space-between; align-items: baseline; margin-bottom: 5px; } .exp-meta strong { font-size: 13px; color: #111; } .exp-dates { font-size: 12px; color: #6b7280; } .main-bullets { padding-left: 16px; } .main-bullets li { margin-bottom: 4px; font-size: 12.5px; color: #374151; }'
WHERE name = 'Modern Sidebar';

UPDATE templates 
SET css_styles = '@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap"); * { box-sizing: border-box; margin: 0; padding: 0; } body { background: #fff; } .resume { font-family: "Inter", sans-serif; max-width: 780px; margin: 0 auto; padding: 0 0 40px; color: #111; font-size: 13px; line-height: 1.6; background: #fff; } .header { background: #0f172a; color: #fff; text-align: center; padding: 36px 40px 28px; margin-bottom: 28px; } .name { font-size: 36px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; margin-bottom: 10px; color: #fff; } .contact-bar { font-size: 12.5px; color: #94a3b8; letter-spacing: 0.03em; } .section { padding: 0 40px; margin-bottom: 22px; } .section-title { font-size: 13px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.08em; color: var(--primary, #0891b2); border-bottom: 1.5px solid #e2e8f0; padding-bottom: 4px; margin-bottom: 12px; } .summary-text { font-size: 12.5px; color: #334155; text-align: justify; } .exp-item { margin-bottom: 14px; } .exp-header { display: flex; justify-content: space-between; align-items: baseline; } .exp-role { font-weight: 700; font-size: 13px; } .exp-company { font-size: 12px; color: var(--primary, #0891b2); font-weight: 600; margin: 3px 0 6px; } .exp-dates { font-size: 12px; color: #64748b; } .edu-item { margin-bottom: 12px; } .edu-item strong { font-size: 13px; } .edu-inst { font-size: 12px; color: #555; margin: 2px 0; } .edu-dates { font-size: 11.5px; color: #64748b; margin-bottom: 4px; } .bullet-list { padding-left: 16px; margin-top: 4px; } .bullet-list li { margin-bottom: 4px; font-size: 12.5px; color: #334155; }'
WHERE name = 'Executive Navy';

UPDATE templates 
SET css_styles = '@import url("https://fonts.googleapis.com/css2?family=Raleway:wght@400;600;700&display=swap"); * { box-sizing: border-box; margin: 0; padding: 0; } body { background: #fff; } .resume { font-family: "Raleway", sans-serif; max-width: 780px; margin: 0 auto; padding: 40px; color: #1e293b; font-size: 13px; line-height: 1.6; background: #fff; border: 1px solid #e2e8f0; } .header { display: flex; flex-direction: column; align-items: center; text-align: center; padding-bottom: 20px; border-bottom: 2px solid var(--primary, #0f172a); margin-bottom: 24px; } .name { font-size: 30px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em; color: var(--primary, #0f172a); margin-bottom: 5px; } .job-title { font-size: 12px; text-transform: uppercase; letter-spacing: 0.18em; color: #64748b; margin-bottom: 12px; } .contact-icons { display: flex; gap: 20px; font-size: 12px; color: #475569; } .section { margin-bottom: 22px; } .section-title { font-size: 13px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em; color: var(--primary, #0f172a); border-bottom: 1.5px solid #cbd5e1; padding-bottom: 4px; margin-bottom: 14px; } .summary-text { font-size: 12.5px; color: #475569; } .timeline-item { display: grid; grid-template-columns: 160px 1fr; gap: 20px; margin-bottom: 14px; } .timeline-left { display: flex; flex-direction: column; gap: 3px; } .tl-dates { font-size: 12px; font-weight: 700; color: var(--primary, #0f172a); } .tl-sub { font-size: 11.5px; color: #64748b; } .tl-title { font-size: 13px; font-weight: 700; display: block; margin-bottom: 4px; } .tl-desc { font-size: 12px; color: #475569; } .skills-row { display: flex; flex-wrap: wrap; gap: 8px; } .skill-pill { background: color-mix(in srgb, var(--primary, #0f172a) 6%, #f8fafc); border: 1px solid color-mix(in srgb, var(--primary, #0f172a) 30%, #cbd5e1); border-radius: 20px; padding: 4px 14px; font-size: 12px; color: var(--primary, #334155); } .refs-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; } .ref-card { display: flex; flex-direction: column; gap: 3px; } .ref-card strong { font-size: 13px; color: var(--primary, #0f172a); } .ref-title { font-size: 12px; color: #64748b; } .ref-contact { font-size: 11.5px; color: #475569; }'
WHERE name = 'Minimalist Timeline';

UPDATE templates 
SET css_styles = '@import url("https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;700&display=swap"); * { box-sizing: border-box; margin: 0; padding: 0; } body { background: #fff; } .resume { font-family: "DM Sans", sans-serif; max-width: 780px; margin: 0 auto; padding: 40px; color: #111; font-size: 13px; line-height: 1.6; background: #fff; } .header { margin-bottom: 16px; padding-bottom: 14px; border-bottom: 2px solid var(--primary, #111); margin-bottom: 16px; } .name { font-size: 26px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; margin-bottom: 3px; color: var(--primary, #111); } .job-title { font-size: 13px; font-weight: 500; text-transform: uppercase; letter-spacing: 0.1em; color: #555; margin-bottom: 8px; } .contact-row { font-size: 12px; color: #555; } .intro { font-size: 12.5px; color: #374151; margin-bottom: 20px; text-align: justify; } .body-grid { display: grid; grid-template-columns: 1fr 220px; gap: 28px; } .section { margin-bottom: 20px; } .section-title { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em; border-bottom: 1.5px solid var(--primary, #111); padding-bottom: 4px; margin-bottom: 12px; } .section-title-sm { font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em; border-bottom: 1.5px solid var(--primary, #0d9488); padding-bottom: 3px; margin-bottom: 10px; color: var(--primary, #0d9488); } .expertise-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 4px 16px; font-size: 12.5px; color: #374151; } .achievement-list { padding-left: 16px; } .achievement-list li { margin-bottom: 8px; font-size: 12.5px; color: #374151; } .exp-item, .edu-item { margin-bottom: 14px; } .exp-header, .edu-header { display: flex; justify-content: space-between; align-items: baseline; } .exp-role { font-weight: 700; font-size: 13px; } .exp-dates { font-size: 12px; color: #6b7280; } .edu-inst { font-size: 12px; color: #374151; margin: 3px 0; } .bullet-list { padding-left: 16px; margin-top: 6px; } .bullet-list li { margin-bottom: 4px; font-size: 12.5px; color: #374151; } .side-col { padding-top: 0; } .side-list { list-style: none; } .side-col .section-title-sm { border-bottom: 1.5px solid var(--primary, #0d9488); color: var(--primary, #0d9488); } .side-list li { font-size: 12px; color: #374151; margin-bottom: 8px; }'
WHERE name = 'Creative Teal';

UPDATE templates 
SET css_styles = '* { box-sizing: border-box; margin: 0; padding: 0; } body { margin: 0; background: #fff; color: #0f172a; font-family: "Segoe UI", Arial, sans-serif; } .columns-resume { display: grid; grid-template-columns: 220px 1fr; max-width: 860px; margin: 0 auto; background: #fff; } .columns-side { background: #f8fafc; border-right: 1px solid #dbe3ea; padding: 28px 20px; } .columns-main { padding: 28px 30px; } .side-card, .main-card { margin-bottom: 22px; } .side-card h1 { margin: 0 0 4px; font-size: 24px; } .side-card p { margin: 0 0 12px; font-size: 11px; letter-spacing: 0.14em; text-transform: uppercase; color: #475569; } .side-card h2, .main-card h2 { margin: 0 0 10px; font-size: 11px; text-transform: uppercase; letter-spacing: 0.14em; color: var(--primary, #0f766e); } .side-card ul, .main-card ul { margin: 6px 0 0; padding-left: 18px; } .side-card li, .main-card li, .side-card div, .main-card p, .entry-sub, .entry span { font-size: 11.5px; } .entry { margin-bottom: 12px; } .entry-top { display: flex; justify-content: space-between; gap: 12px; } .entry-sub { margin-top: 2px; color: #475569; } .entry span { color: #64748b; } .main-card { padding-bottom: 14px; border-bottom: 1px solid #e2e8f0; }'
WHERE name = 'Clean Columns';

UPDATE templates 
SET css_styles = '@import url("https://fonts.googleapis.com/css2?family=Times+New+Roman&family=Garamond&display=swap"); * { box-sizing: border-box; margin: 0; padding: 0; } body { background: #fff; } .resume { font-family: "Times New Roman", Times, serif; max-width: 780px; margin: 0 auto; padding: 36px 40px; color: #111; font-size: 13px; line-height: 1.55; background: #fff; } .header { text-align: center; margin-bottom: 14px; } .name { font-size: 26px; font-weight: 700; font-family: "Times New Roman", serif; letter-spacing: 0.02em; margin-bottom: 8px; color: var(--primary, #111); } .contact-bar { display: flex; justify-content: center; flex-wrap: wrap; gap: 16px; font-size: 12px; color: #333; border-top: 1.5px solid var(--primary, #111); border-bottom: 1.5px solid var(--primary, #111); padding: 6px 0; } .section { margin-bottom: 16px; } .section-title { font-size: 13px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.06em; color: var(--primary, #111); border-bottom: 1.5px solid var(--primary, #111); padding-bottom: 3px; margin-bottom: 10px; } .two-col-section { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; } .col .section-title { margin-top: 0; } .edu-item { margin-bottom: 8px; } .edu-row { display: flex; justify-content: space-between; } .edu-degree { font-weight: 600; } .edu-institution { color: #333; } .edu-dates, .edu-grade { font-size: 12px; color: #444; } .exp-item, .project-item { margin-bottom: 12px; } .exp-header, .project-header { display: flex; justify-content: space-between; align-items: baseline; } .exp-company { font-weight: 700; } .exp-role { font-style: italic; font-size: 12.5px; margin: 2px 0 5px; } .exp-dates { font-size: 12px; color: #444; } .bullet-list { padding-left: 18px; margin-top: 4px; } .bullet-list li { margin-bottom: 3px; font-size: 12.5px; } .cert-row { display: flex; justify-content: space-between; font-size: 12.5px; margin-bottom: 4px; }'
WHERE name = 'Classic ATS';

UPDATE templates 
SET css_styles = '@import url("https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600;700&display=swap"); * { box-sizing: border-box; margin: 0; padding: 0; } body { background: #fff; } .resume { font-family: "Open Sans", Arial, sans-serif; max-width: 780px; margin: 0 auto; padding: 40px; color: #111; font-size: 13px; line-height: 1.6; background: #fff; } .header { text-align: center; margin-bottom: 24px; padding-bottom: 16px; border-bottom: 2px solid var(--primary, #111); } .name { font-size: 28px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.04em; margin-bottom: 6px; } .job-title { font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.12em; color: #444; margin-bottom: 8px; } .contact-line { font-size: 12.5px; color: #555; } .section { margin-bottom: 20px; } .section-title-bar { background: color-mix(in srgb, var(--primary, #111) 8%, transparent); padding: 5px 10px; margin-bottom: 12px; } .section-title-bar h2 { font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.1em; color: var(--primary, #111); } .exp-item, .edu-item { margin-bottom: 14px; } .exp-header, .edu-header { display: flex; justify-content: space-between; align-items: baseline; } .exp-role { font-weight: 700; font-size: 13px; } .exp-dates { font-size: 12px; color: #555; } .edu-institution { font-size: 12.5px; color: #333; margin: 3px 0; } .bullet-list { padding-left: 18px; margin-top: 6px; } .bullet-list li { margin-bottom: 4px; font-size: 12.5px; text-align: justify; } .skills-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 6px 12px; } .skill-item { font-size: 12.5px; }'
WHERE name = 'Corporate Clean';

UPDATE templates 
SET css_styles = '* { box-sizing: border-box; margin: 0; padding: 0; } body { margin: 0; background: #fff; color: #1f2937; font-family: Georgia, "Times New Roman", serif; } .ivory-resume { max-width: 820px; margin: 0 auto; padding: 42px; background: #fffaf4; } .ivory-header { text-align: center; padding-bottom: 18px; margin-bottom: 22px; border-bottom: 1px solid var(--primary, #d6c8b8); } .ivory-header h1 { margin: 0; font-size: 31px; color: var(--primary, #2b2118); } .ivory-header p { margin: 6px 0 8px; font-size: 12px; letter-spacing: 0.18em; text-transform: uppercase; color: color-mix(in srgb, var(--primary, #2b2118) 70%, #2b2118); } .ivory-contact { font-size: 11px; color: color-mix(in srgb, var(--primary, #6b5b4d) 60%, #2b2118); } .section { margin-bottom: 22px; } .section h2 { margin: 0 0 12px; font-size: 12px; letter-spacing: 0.14em; text-transform: uppercase; color: var(--primary, #6c4f3d); border-bottom: 1px solid color-mix(in srgb, var(--primary, #6c4f3d) 30%, transparent); padding-bottom: 5px; } .section p, .section li, .section div, .section span { font-size: 12.2px; } .ivory-block { margin-bottom: 12px; } .ivory-row { display: flex; justify-content: space-between; gap: 12px; } .ivory-row span, .section span { color: color-mix(in srgb, var(--primary, #7c6b5b) 50%, #2b2118); font-size: 11px; } .ivory-block ul, .plain-list { margin: 6px 0 0; padding-left: 18px; } .ivory-grid { display: grid; grid-template-columns: 1.1fr 0.9fr; gap: 28px; }'
WHERE name = 'Simple Ivory';

UPDATE templates 
SET css_styles = '* { box-sizing: border-box; margin: 0; padding: 0; } body { margin: 0; background: #fff; color: #111827; font-family: "Courier New", monospace; } .mono-resume { max-width: 840px; margin: 0 auto; padding: 34px; background: #fff; } .mono-header { display: flex; justify-content: space-between; gap: 18px; padding-bottom: 16px; margin-bottom: 18px; border-bottom: 2px solid var(--primary, #111827); } .mono-header h1 { margin: 0; font-size: 28px; letter-spacing: 0.06em; } .mono-header p { margin: 6px 0 0; font-size: 11px; text-transform: uppercase; letter-spacing: 0.18em; color: #4b5563; } .mono-contact { display: flex; flex-direction: column; gap: 4px; font-size: 11px; text-align: right; color: #374151; } .mono-section { margin-bottom: 20px; } .mono-section h2 { margin: 0 0 10px; font-size: 11px; text-transform: uppercase; letter-spacing: 0.18em; color: var(--primary, #111827); background: color-mix(in srgb, var(--primary, #111827) 8%, transparent); padding: 6px 8px; } .mono-tags { display: flex; flex-wrap: wrap; gap: 8px; } .mono-tags span { border: 1px solid color-mix(in srgb, var(--primary, #111827) 30%, transparent); padding: 4px 8px; font-size: 11px; } .mono-item { margin-bottom: 12px; } .mono-top { display: flex; justify-content: space-between; gap: 12px; margin-bottom: 4px; } .mono-top span { font-size: 11px; color: #6b7280; } .mono-item div, .mono-item li, .mono-section p { font-size: 11.5px; } .mono-item ul { margin: 4px 0 0; padding-left: 18px; } .mono-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }'
WHERE name = 'Minimal Mono';
