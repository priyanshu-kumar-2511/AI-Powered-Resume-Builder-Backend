-- Format Executive Navy work experience as "Company - Role" on one line
UPDATE templates
SET
  html_layout = REPLACE(
    html_layout,
    '<div class="exp-header">
        <strong class="exp-role">{{role}}</strong>
        <span class="exp-dates">{{startDate}} â€“ {{endDate}}</span>
      </div>
      <div class="exp-company">{{company}}</div>',
    '<div class="exp-header">
        <strong class="exp-role">{{company}}{{#role}} - {{role}}{{/role}}</strong>
        <span class="exp-dates">{{startDate}} â€“ {{endDate}}</span>
      </div>'
  ),
  updated_at = NOW()
WHERE name = 'Executive Navy';
