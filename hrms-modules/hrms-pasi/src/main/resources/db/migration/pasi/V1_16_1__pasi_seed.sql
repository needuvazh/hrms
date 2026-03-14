INSERT INTO pasi.pasi_contribution_rules (
    id,
    tenant_id,
    rule_code,
    name,
    employee_rate_percent,
    employer_rate_percent,
    salary_cap,
    is_active,
    created_at,
    updated_at
)
VALUES (
    '1516d7fe-cf19-4432-87cd-8a153f71f001',
    'default',
    'PASI-STD',
    'PASI Standard Rule',
    7.000,
    10.500,
    3000.000,
    true,
    NOW(),
    NOW()
)
ON CONFLICT (tenant_id, rule_code) DO NOTHING;
