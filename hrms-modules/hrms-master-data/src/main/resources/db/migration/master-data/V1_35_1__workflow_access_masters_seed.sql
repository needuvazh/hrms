INSERT INTO master_data.permissions (
    id, permission_code, permission_name, module_code, action_type, scope_type, description, active, created_by, updated_by
)
VALUES
    ('d3501000-0000-0000-0000-000000000001', 'employee.view', 'Employee View', 'employee', 'VIEW', 'ALL', 'View employee profile data', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000002', 'employee.edit', 'Employee Edit', 'employee', 'EDIT', 'DEPARTMENT', 'Edit employee profile data', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000003', 'leave.approve', 'Leave Approve', 'leave', 'APPROVE', 'TEAM', 'Approve leave requests', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000004', 'document.approve', 'Document Approve', 'document', 'APPROVE', 'DEPARTMENT', 'Approve submitted documents', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000005', 'service-request.initiate', 'Service Request Initiate', 'service-request', 'INITIATE', 'SELF', 'Create service requests', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (permission_code) DO UPDATE
SET permission_name = EXCLUDED.permission_name,
    module_code = EXCLUDED.module_code,
    action_type = EXCLUDED.action_type,
    scope_type = EXCLUDED.scope_type,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.workflow_types (
    id, workflow_type_code, workflow_type_name, module_name, initiation_channel, approval_required_flag, description, active, created_by, updated_by
)
VALUES
    ('d3501000-0000-0000-0000-000000000011', 'ONBOARDING', 'Onboarding', 'employee', 'HR', TRUE, 'Employee onboarding workflow', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000012', 'LEAVE_REQUEST', 'Leave Request', 'leave', 'ESS', TRUE, 'Leave submission and approval workflow', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000013', 'DOCUMENT_APPROVAL', 'Document Approval', 'document', 'ESS', TRUE, 'Document verification workflow', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000014', 'SERVICE_REQUEST', 'Service Request', 'service-request', 'ESS', TRUE, 'ESS/MSS service request workflow', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (workflow_type_code) DO UPDATE
SET workflow_type_name = EXCLUDED.workflow_type_name,
    module_name = EXCLUDED.module_name,
    initiation_channel = EXCLUDED.initiation_channel,
    approval_required_flag = EXCLUDED.approval_required_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.approval_action_types (
    id, approval_action_type_code, approval_action_type_name, final_action_flag, description, active, created_by, updated_by
)
VALUES
    ('d3501000-0000-0000-0000-000000000021', 'SUBMIT', 'Submit', FALSE, 'Submit action for workflow start', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000022', 'APPROVE', 'Approve', TRUE, 'Approve workflow action', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000023', 'REJECT', 'Reject', TRUE, 'Reject workflow action', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000024', 'RETURN_FOR_CORRECTION', 'Return For Correction', FALSE, 'Return request for correction', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (approval_action_type_code) DO UPDATE
SET approval_action_type_name = EXCLUDED.approval_action_type_name,
    final_action_flag = EXCLUDED.final_action_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.delegation_types (
    id, delegation_type_code, delegation_type_name, approval_allowed_flag, action_allowed_flag, view_allowed_flag, temporary_only_flag, description, active, created_by, updated_by
)
VALUES
    ('d3501000-0000-0000-0000-000000000031', 'FULL_APPROVAL_DELEGATION', 'Full Approval Delegation', TRUE, TRUE, TRUE, FALSE, 'Full approval/action/view delegation', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000032', 'TEMPORARY_APPROVAL_DELEGATION', 'Temporary Approval Delegation', TRUE, FALSE, TRUE, TRUE, 'Temporary approval-only delegation', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000033', 'VIEW_ONLY_DELEGATION', 'View Only Delegation', FALSE, FALSE, TRUE, TRUE, 'Read-only delegation', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (delegation_type_code) DO UPDATE
SET delegation_type_name = EXCLUDED.delegation_type_name,
    approval_allowed_flag = EXCLUDED.approval_allowed_flag,
    action_allowed_flag = EXCLUDED.action_allowed_flag,
    view_allowed_flag = EXCLUDED.view_allowed_flag,
    temporary_only_flag = EXCLUDED.temporary_only_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.roles (
    id, tenant_id, role_code, role_name, role_type, description, active, created_by, updated_by
)
VALUES
    ('d3501000-0000-0000-0000-000000000041', 'default', 'HR_ADMIN', 'HR Admin', 'TENANT', 'Default tenant HR administrator role', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000042', 'default', 'MANAGER', 'Manager', 'TENANT', 'Default tenant manager role', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000043', 'default', 'EMPLOYEE', 'Employee', 'TENANT', 'Default tenant employee role', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000044', 'lite', 'HR_ADMIN', 'HR Admin', 'TENANT', 'Lite tenant HR administrator role', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000045', 'lite', 'MANAGER', 'Manager', 'TENANT', 'Lite tenant manager role', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000046', 'lite', 'EMPLOYEE', 'Employee', 'TENANT', 'Lite tenant employee role', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, role_code) DO UPDATE
SET role_name = EXCLUDED.role_name,
    role_type = EXCLUDED.role_type,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.role_permission_mappings (
    id, tenant_id, role_id, permission_id, allow_flag, data_scope_override, description, active, created_by, updated_by
)
SELECT
    'd3501000-0000-0000-0000-000000000051'::uuid,
    'default',
    r.id,
    p.id,
    TRUE,
    'ALL',
    'HR admin can view employees',
    TRUE,
    'SYSTEM_SEED',
    'SYSTEM_SEED'
FROM master_data.roles r, master_data.permissions p
WHERE r.tenant_id = 'default' AND r.role_code = 'HR_ADMIN' AND p.permission_code = 'employee.view'
ON CONFLICT (tenant_id, role_id, permission_id) DO UPDATE
SET allow_flag = EXCLUDED.allow_flag,
    data_scope_override = EXCLUDED.data_scope_override,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.role_permission_mappings (
    id, tenant_id, role_id, permission_id, allow_flag, data_scope_override, description, active, created_by, updated_by
)
SELECT
    'd3501000-0000-0000-0000-000000000052'::uuid,
    'default',
    r.id,
    p.id,
    TRUE,
    'DEPARTMENT',
    'Manager can approve leave',
    TRUE,
    'SYSTEM_SEED',
    'SYSTEM_SEED'
FROM master_data.roles r, master_data.permissions p
WHERE r.tenant_id = 'default' AND r.role_code = 'MANAGER' AND p.permission_code = 'leave.approve'
ON CONFLICT (tenant_id, role_id, permission_id) DO UPDATE
SET allow_flag = EXCLUDED.allow_flag,
    data_scope_override = EXCLUDED.data_scope_override,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.role_permission_mappings (
    id, tenant_id, role_id, permission_id, allow_flag, data_scope_override, description, active, created_by, updated_by
)
SELECT
    'd3501000-0000-0000-0000-000000000053'::uuid,
    'lite',
    r.id,
    p.id,
    TRUE,
    'ALL',
    'Lite HR admin can view employees',
    TRUE,
    'SYSTEM_SEED',
    'SYSTEM_SEED'
FROM master_data.roles r, master_data.permissions p
WHERE r.tenant_id = 'lite' AND r.role_code = 'HR_ADMIN' AND p.permission_code = 'employee.view'
ON CONFLICT (tenant_id, role_id, permission_id) DO UPDATE
SET allow_flag = EXCLUDED.allow_flag,
    data_scope_override = EXCLUDED.data_scope_override,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.service_request_types (
    id, tenant_id, service_request_type_code, service_request_type_name, category, workflow_type_id,
    attachment_required_flag, auto_close_allowed_flag, description, active, created_by, updated_by
)
SELECT
    'd3501000-0000-0000-0000-000000000061'::uuid,
    'default',
    'SALARY_CERTIFICATE',
    'Salary Certificate Request',
    'HR',
    w.id,
    FALSE,
    TRUE,
    'Salary certificate request from ESS',
    TRUE,
    'SYSTEM_SEED',
    'SYSTEM_SEED'
FROM master_data.workflow_types w
WHERE w.workflow_type_code = 'SERVICE_REQUEST'
ON CONFLICT (tenant_id, service_request_type_code) DO UPDATE
SET service_request_type_name = EXCLUDED.service_request_type_name,
    category = EXCLUDED.category,
    workflow_type_id = EXCLUDED.workflow_type_id,
    attachment_required_flag = EXCLUDED.attachment_required_flag,
    auto_close_allowed_flag = EXCLUDED.auto_close_allowed_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.service_request_types (
    id, tenant_id, service_request_type_code, service_request_type_name, category, workflow_type_id,
    attachment_required_flag, auto_close_allowed_flag, description, active, created_by, updated_by
)
SELECT
    'd3501000-0000-0000-0000-000000000062'::uuid,
    'default',
    'ATTENDANCE_CORRECTION',
    'Attendance Correction Request',
    'ATTENDANCE',
    w.id,
    TRUE,
    FALSE,
    'Attendance correction request',
    TRUE,
    'SYSTEM_SEED',
    'SYSTEM_SEED'
FROM master_data.workflow_types w
WHERE w.workflow_type_code = 'SERVICE_REQUEST'
ON CONFLICT (tenant_id, service_request_type_code) DO UPDATE
SET service_request_type_name = EXCLUDED.service_request_type_name,
    category = EXCLUDED.category,
    workflow_type_id = EXCLUDED.workflow_type_id,
    attachment_required_flag = EXCLUDED.attachment_required_flag,
    auto_close_allowed_flag = EXCLUDED.auto_close_allowed_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.service_request_types (
    id, tenant_id, service_request_type_code, service_request_type_name, category, workflow_type_id,
    attachment_required_flag, auto_close_allowed_flag, description, active, created_by, updated_by
)
SELECT
    'd3501000-0000-0000-0000-000000000063'::uuid,
    'lite',
    'SALARY_CERTIFICATE',
    'Salary Certificate Request',
    'HR',
    w.id,
    FALSE,
    TRUE,
    'Salary certificate request from ESS (lite)',
    TRUE,
    'SYSTEM_SEED',
    'SYSTEM_SEED'
FROM master_data.workflow_types w
WHERE w.workflow_type_code = 'SERVICE_REQUEST'
ON CONFLICT (tenant_id, service_request_type_code) DO UPDATE
SET service_request_type_name = EXCLUDED.service_request_type_name,
    category = EXCLUDED.category,
    workflow_type_id = EXCLUDED.workflow_type_id,
    attachment_required_flag = EXCLUDED.attachment_required_flag,
    auto_close_allowed_flag = EXCLUDED.auto_close_allowed_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.notification_templates (
    id, tenant_id, template_code, template_name, event_code, channel_type, subject_template, body_template, language_code, description, active, created_by, updated_by
)
VALUES
    ('d3501000-0000-0000-0000-000000000071', 'default', 'LEAVE_SUBMITTED_EMAIL', 'Leave Submitted (Email)', 'LEAVE_SUBMITTED', 'EMAIL', 'Leave submitted', 'Your leave request has been submitted successfully.', 'en', 'Default leave submitted email template', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000072', 'default', 'LEAVE_APPROVED_EMAIL', 'Leave Approved (Email)', 'LEAVE_APPROVED', 'EMAIL', 'Leave approved', 'Your leave request has been approved.', 'en', 'Default leave approved email template', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3501000-0000-0000-0000-000000000073', 'lite', 'LEAVE_SUBMITTED_EMAIL', 'Leave Submitted (Email)', 'LEAVE_SUBMITTED', 'EMAIL', 'Leave submitted', 'Your leave request has been submitted successfully.', 'en', 'Lite leave submitted email template', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, template_code) DO UPDATE
SET template_name = EXCLUDED.template_name,
    event_code = EXCLUDED.event_code,
    channel_type = EXCLUDED.channel_type,
    subject_template = EXCLUDED.subject_template,
    body_template = EXCLUDED.body_template,
    language_code = EXCLUDED.language_code,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.approval_matrices (
    id, tenant_id, approval_matrix_code, workflow_type_id, matrix_name, legal_entity_id, branch_id, department_id,
    employee_category_id, worker_type_id, service_request_type_id, min_amount, max_amount, level_no,
    approver_source_type, approver_role_id, approver_user_ref, approval_action_type_id, escalation_days,
    delegation_allowed_flag, description, active, created_by, updated_by
)
SELECT
    'd3501000-0000-0000-0000-000000000081'::uuid,
    'default',
    'LEAVE_STD_L1',
    wt.id,
    'Leave Standard Matrix Level 1',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    1,
    'ROLE',
    r.id,
    NULL,
    aat.id,
    2,
    TRUE,
    'Level 1 manager approval for leave requests',
    TRUE,
    'SYSTEM_SEED',
    'SYSTEM_SEED'
FROM master_data.workflow_types wt, master_data.roles r, master_data.approval_action_types aat
WHERE wt.workflow_type_code = 'LEAVE_REQUEST'
  AND r.tenant_id = 'default' AND r.role_code = 'MANAGER'
  AND aat.approval_action_type_code = 'APPROVE'
ON CONFLICT (tenant_id, approval_matrix_code) DO UPDATE
SET workflow_type_id = EXCLUDED.workflow_type_id,
    matrix_name = EXCLUDED.matrix_name,
    legal_entity_id = EXCLUDED.legal_entity_id,
    branch_id = EXCLUDED.branch_id,
    department_id = EXCLUDED.department_id,
    employee_category_id = EXCLUDED.employee_category_id,
    worker_type_id = EXCLUDED.worker_type_id,
    service_request_type_id = EXCLUDED.service_request_type_id,
    min_amount = EXCLUDED.min_amount,
    max_amount = EXCLUDED.max_amount,
    level_no = EXCLUDED.level_no,
    approver_source_type = EXCLUDED.approver_source_type,
    approver_role_id = EXCLUDED.approver_role_id,
    approver_user_ref = EXCLUDED.approver_user_ref,
    approval_action_type_id = EXCLUDED.approval_action_type_id,
    escalation_days = EXCLUDED.escalation_days,
    delegation_allowed_flag = EXCLUDED.delegation_allowed_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.approval_matrices (
    id, tenant_id, approval_matrix_code, workflow_type_id, matrix_name, legal_entity_id, branch_id, department_id,
    employee_category_id, worker_type_id, service_request_type_id, min_amount, max_amount, level_no,
    approver_source_type, approver_role_id, approver_user_ref, approval_action_type_id, escalation_days,
    delegation_allowed_flag, description, active, created_by, updated_by
)
SELECT
    'd3501000-0000-0000-0000-000000000082'::uuid,
    'lite',
    'LEAVE_STD_L1',
    wt.id,
    'Leave Standard Matrix Level 1',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    1,
    'ROLE',
    r.id,
    NULL,
    aat.id,
    2,
    TRUE,
    'Level 1 manager approval for leave requests (lite)',
    TRUE,
    'SYSTEM_SEED',
    'SYSTEM_SEED'
FROM master_data.workflow_types wt, master_data.roles r, master_data.approval_action_types aat
WHERE wt.workflow_type_code = 'LEAVE_REQUEST'
  AND r.tenant_id = 'lite' AND r.role_code = 'MANAGER'
  AND aat.approval_action_type_code = 'APPROVE'
ON CONFLICT (tenant_id, approval_matrix_code) DO UPDATE
SET workflow_type_id = EXCLUDED.workflow_type_id,
    matrix_name = EXCLUDED.matrix_name,
    legal_entity_id = EXCLUDED.legal_entity_id,
    branch_id = EXCLUDED.branch_id,
    department_id = EXCLUDED.department_id,
    employee_category_id = EXCLUDED.employee_category_id,
    worker_type_id = EXCLUDED.worker_type_id,
    service_request_type_id = EXCLUDED.service_request_type_id,
    min_amount = EXCLUDED.min_amount,
    max_amount = EXCLUDED.max_amount,
    level_no = EXCLUDED.level_no,
    approver_source_type = EXCLUDED.approver_source_type,
    approver_role_id = EXCLUDED.approver_role_id,
    approver_user_ref = EXCLUDED.approver_user_ref,
    approval_action_type_id = EXCLUDED.approval_action_type_id,
    escalation_days = EXCLUDED.escalation_days,
    delegation_allowed_flag = EXCLUDED.delegation_allowed_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
