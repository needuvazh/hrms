INSERT INTO leave.leave_types (
    id,
    tenant_id,
    leave_code,
    leave_name,
    is_paid,
    annual_limit_days,
    is_active,
    created_at,
    updated_at
)
VALUES
    ('81000000-0000-0000-0000-000000000001', 'default', 'ANNUAL', 'Annual Leave', TRUE, 30, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('81000000-0000-0000-0000-000000000002', 'default', 'SICK', 'Sick Leave', TRUE, 12, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('81000000-0000-0000-0000-000000000003', 'lite', 'ANNUAL', 'Annual Leave', TRUE, 24, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, leave_code) DO NOTHING;
