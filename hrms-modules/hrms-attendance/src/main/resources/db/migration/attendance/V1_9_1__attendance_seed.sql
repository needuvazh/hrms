INSERT INTO attendance.shifts (
    id,
    tenant_id,
    shift_code,
    shift_name,
    start_time,
    end_time,
    is_active,
    created_at,
    updated_at
)
VALUES
    ('61000000-0000-0000-0000-000000000001', 'default', 'GENERAL', 'General Shift', '09:00:00', '18:00:00', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('61000000-0000-0000-0000-000000000002', 'lite', 'GENERAL', 'General Shift', '09:00:00', '18:00:00', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, shift_code) DO NOTHING;
