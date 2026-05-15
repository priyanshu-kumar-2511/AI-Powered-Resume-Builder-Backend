-- Fix Modern Sidebar template: add distinct chip styling for Area of Expertise vs sidebar Skills
UPDATE templates
SET
  css_styles = CONCAT(
    css_styles,
    '.expertise-grid span{background:rgba(96,165,250,0.08);border:1px solid rgba(96,165,250,0.35);border-radius:5px;padding:4px 8px;font-size:11px;font-weight:600;color:#1e40af;text-align:center;display:block;}'
  ),
  updated_at = NOW()
WHERE name = 'Modern Sidebar';
