UPDATE auth.users
SET username = 'admin@hrms.local',
    email = 'admin@hrms.local',
    password_hash = '{noop}Admin@01',
    updated_by = 'SYSTEM',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = 'default'
  AND id = '21111111-1111-1111-1111-111111111111';

UPDATE auth.users
SET username = 'admin@hrms.local',
    email = 'admin@hrms.local',
    password_hash = '{bcrypt}$2y$10$bpSTSvBxPRaWKtavkLMAlum1Va0CcuFOrso91PstUkEO8/htfciU2',
    updated_by = 'SYSTEM',
    updated_at = CURRENT_TIMESTAMP
WHERE tenant_id = 'platform'
  AND id = '8e2dffb8-26ad-4a89-bc84-f397f9772112';
