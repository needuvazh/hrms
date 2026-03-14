INSERT INTO workflow.workflow_definitions (
    id,
    tenant_id,
    workflow_key,
    workflow_name,
    is_active,
    created_at,
    updated_at
)
VALUES
    ('71000000-0000-0000-0000-000000000001', 'default', 'leave.approval', 'Leave Approval Workflow', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('71000000-0000-0000-0000-000000000002', 'lite', 'leave.approval', 'Leave Approval Workflow', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('71000000-0000-0000-0000-000000000003', 'default', 'employee.onboarding', 'Employee Onboarding Workflow', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('71000000-0000-0000-0000-000000000004', 'lite', 'employee.onboarding', 'Employee Onboarding Workflow', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (tenant_id, workflow_key) DO NOTHING;
