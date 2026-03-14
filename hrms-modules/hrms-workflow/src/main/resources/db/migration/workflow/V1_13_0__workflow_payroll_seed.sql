INSERT INTO workflow.workflow_definitions (
    id,
    tenant_id,
    workflow_key,
    workflow_name,
    is_active,
    created_at,
    updated_at
)
VALUES (
    'f8f2d33f-745d-49b8-b5f5-0f62e7bc1300',
    'default',
    'payroll.approval',
    'Payroll Approval',
    true,
    NOW(),
    NOW()
)
ON CONFLICT (tenant_id, workflow_key) DO NOTHING;
