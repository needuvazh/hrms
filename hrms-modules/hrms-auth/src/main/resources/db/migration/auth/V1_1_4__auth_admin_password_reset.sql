UPDATE auth.users
SET password_hash = '{noop}Admin@01',
    updated_by = 'SYSTEM',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = 'default'
  AND username = 'admin';

UPDATE auth.users
SET password_hash = '{bcrypt}$2y$10$bpSTSvBxPRaWKtavkLMAlum1Va0CcuFOrso91PstUkEO8/htfciU2',
    updated_by = 'SYSTEM',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = 'platform'
  AND username = 'admin';
