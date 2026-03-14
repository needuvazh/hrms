INSERT INTO wps.wps_batches (
    id,
    tenant_id,
    payroll_run_id,
    status,
    validation_summary,
    created_by,
    exported_by,
    exported_at,
    created_at,
    updated_at
)
SELECT
    '15555555-5555-5555-5555-555555555551',
    'default',
    'b6db9c45-b18a-4a9a-8444-94b4ef930010',
    'VALIDATED',
    'Seed WPS batch for local development',
    'seed',
    NULL,
    NULL,
    NOW(),
    NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM wps.wps_batches
    WHERE id = '15555555-5555-5555-5555-555555555551'
);

INSERT INTO wps.wps_employee_entries (
    id,
    tenant_id,
    wps_batch_id,
    employee_id,
    net_amount,
    payment_reference,
    created_at
)
SELECT
    '15555555-5555-5555-5555-555555555552',
    'default',
    '15555555-5555-5555-5555-555555555551',
    '41111111-1111-1111-1111-111111111111',
    1250.00,
    'PAY-41111111-1111-1111-1111-111111111111',
    NOW()
WHERE NOT EXISTS (
    SELECT 1
    FROM wps.wps_employee_entries
    WHERE id = '15555555-5555-5555-5555-555555555552'
);
