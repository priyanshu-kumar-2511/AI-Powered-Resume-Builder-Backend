-- Fix Executive Navy template: separate Education, Skills, Awards, Certifications into individual sections
UPDATE templates
SET
  html_layout = '<div class="resume">
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
  css_styles = '@import url("https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap");

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
  updated_at = NOW()
WHERE name = 'Executive Navy';
