INSERT INTO person.persons (
    id,
    tenant_id,
    person_code,
    first_name,
    last_name,
    email,
    mobile,
    country_code,
    nationality_code
) VALUES
    ('51111111-1111-1111-1111-111111111111', 'default', 'PER-0001', 'System', 'Admin', 'system.person@default.hrms', '+96890000001', 'OM', 'OM')
ON CONFLICT (tenant_id, person_code) DO NOTHING;

INSERT INTO person.person_lifecycle_history (
    id,
    tenant_id,
    person_id,
    lifecycle_type,
    effective_at,
    details_json
) VALUES
    (
        '51111111-2222-1111-1111-111111111111',
        'default',
        '51111111-1111-1111-1111-111111111111',
        'PERSON_REGISTERED',
        CURRENT_TIMESTAMP,
        '{"source":"seed"}'::jsonb
    )
ON CONFLICT (id) DO NOTHING;
