INSERT INTO recruitment.candidates (
    id,
    tenant_id,
    person_id,
    candidate_code,
    first_name,
    last_name,
    email,
    job_posting_code,
    status
) VALUES (
    '61111111-1111-1111-1111-111111111111',
    'default',
    '51111111-1111-1111-1111-111111111111',
    'CAN-0001',
    'System',
    'Admin',
    'system.person@default.hrms',
    'HR-ADMIN-001',
    'OFFER_ACCEPTED'
)
ON CONFLICT (tenant_id, candidate_code) DO NOTHING;

INSERT INTO recruitment.candidate_status_history (
    id,
    tenant_id,
    candidate_id,
    status,
    reason,
    changed_at
) VALUES (
    '61111111-2222-1111-1111-111111111111',
    'default',
    '61111111-1111-1111-1111-111111111111',
    'OFFER_ACCEPTED',
    'Seed candidate ready for hire flow',
    CURRENT_TIMESTAMP
)
ON CONFLICT (id) DO NOTHING;
