INSERT INTO payroll.payroll_periods (
    id,
    tenant_id,
    period_code,
    start_date,
    end_date,
    status,
    description,
    created_at,
    updated_at
)
VALUES (
    'b6db9c45-b18a-4a9a-8444-94b4ef930001',
    'default',
    '2026-03',
    DATE '2026-03-01',
    DATE '2026-03-31',
    'OPEN',
    'Seed payroll period for local development',
    NOW(),
    NOW()
)
ON CONFLICT (tenant_id, period_code) DO NOTHING;
