CREATE TABLE IF NOT EXISTS master_data.roles (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(255) NOT NULL,
    role_type VARCHAR(32) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_roles_tenant_code UNIQUE (tenant_id, role_code),
    CONSTRAINT ck_roles_role_type CHECK (role_type IN ('SYSTEM', 'TENANT', 'CUSTOM'))
);

CREATE TABLE IF NOT EXISTS master_data.permissions (
    id UUID PRIMARY KEY,
    permission_code VARCHAR(128) NOT NULL UNIQUE,
    permission_name VARCHAR(255) NOT NULL,
    module_code VARCHAR(64) NOT NULL,
    action_type VARCHAR(32) NOT NULL,
    scope_type VARCHAR(32) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT ck_permissions_action_type CHECK (action_type IN ('VIEW', 'CREATE', 'EDIT', 'DELETE', 'APPROVE', 'REJECT', 'EXPORT', 'PUBLISH', 'ACKNOWLEDGE', 'INITIATE')),
    CONSTRAINT ck_permissions_scope_type CHECK (scope_type IN ('SELF', 'TEAM', 'DEPARTMENT', 'ENTITY', 'ALL'))
);

CREATE TABLE IF NOT EXISTS master_data.role_permission_mappings (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    role_id UUID NOT NULL,
    permission_id UUID NOT NULL,
    allow_flag BOOLEAN,
    data_scope_override VARCHAR(32),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_role_permission_map UNIQUE (tenant_id, role_id, permission_id),
    CONSTRAINT fk_role_permission_map_role FOREIGN KEY (role_id) REFERENCES master_data.roles(id),
    CONSTRAINT fk_role_permission_map_permission FOREIGN KEY (permission_id) REFERENCES master_data.permissions(id),
    CONSTRAINT ck_role_permission_data_scope CHECK (data_scope_override IS NULL OR data_scope_override IN ('SELF', 'TEAM', 'DEPARTMENT', 'ENTITY', 'ALL'))
);

CREATE TABLE IF NOT EXISTS master_data.workflow_types (
    id UUID PRIMARY KEY,
    workflow_type_code VARCHAR(128) NOT NULL UNIQUE,
    workflow_type_name VARCHAR(255) NOT NULL,
    module_name VARCHAR(128),
    initiation_channel VARCHAR(32) NOT NULL,
    approval_required_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT ck_workflow_types_channel CHECK (initiation_channel IN ('ESS', 'MSS', 'HR', 'SYSTEM', 'PAYROLL', 'ADMIN'))
);

CREATE TABLE IF NOT EXISTS master_data.service_request_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    service_request_type_code VARCHAR(128) NOT NULL,
    service_request_type_name VARCHAR(255) NOT NULL,
    category VARCHAR(128),
    workflow_type_id UUID,
    attachment_required_flag BOOLEAN,
    auto_close_allowed_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_service_request_types_tenant_code UNIQUE (tenant_id, service_request_type_code),
    CONSTRAINT fk_service_request_types_workflow_type FOREIGN KEY (workflow_type_id) REFERENCES master_data.workflow_types(id)
);

CREATE TABLE IF NOT EXISTS master_data.approval_action_types (
    id UUID PRIMARY KEY,
    approval_action_type_code VARCHAR(128) NOT NULL UNIQUE,
    approval_action_type_name VARCHAR(255) NOT NULL,
    final_action_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL
);

CREATE TABLE IF NOT EXISTS master_data.approval_matrices (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    approval_matrix_code VARCHAR(128) NOT NULL,
    workflow_type_id UUID NOT NULL,
    matrix_name VARCHAR(255),
    legal_entity_id UUID,
    branch_id UUID,
    department_id UUID,
    employee_category_id UUID,
    worker_type_id UUID,
    service_request_type_id UUID,
    min_amount NUMERIC(14,2),
    max_amount NUMERIC(14,2),
    level_no INTEGER NOT NULL,
    approver_source_type VARCHAR(64) NOT NULL,
    approver_role_id UUID,
    approver_user_ref VARCHAR(128),
    approval_action_type_id UUID,
    escalation_days INTEGER,
    delegation_allowed_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_approval_matrices_tenant_code UNIQUE (tenant_id, approval_matrix_code),
    CONSTRAINT fk_approval_matrices_workflow_type FOREIGN KEY (workflow_type_id) REFERENCES master_data.workflow_types(id),
    CONSTRAINT fk_approval_matrices_legal_entity FOREIGN KEY (legal_entity_id) REFERENCES organization.legal_entities(id),
    CONSTRAINT fk_approval_matrices_branch FOREIGN KEY (branch_id) REFERENCES organization.branches(id),
    CONSTRAINT fk_approval_matrices_department FOREIGN KEY (department_id) REFERENCES organization.departments(id),
    CONSTRAINT fk_approval_matrices_employee_category FOREIGN KEY (employee_category_id) REFERENCES job_architecture.employee_categories(id),
    CONSTRAINT fk_approval_matrices_worker_type FOREIGN KEY (worker_type_id) REFERENCES job_architecture.worker_types(id),
    CONSTRAINT fk_approval_matrices_service_request_type FOREIGN KEY (service_request_type_id) REFERENCES master_data.service_request_types(id),
    CONSTRAINT fk_approval_matrices_approver_role FOREIGN KEY (approver_role_id) REFERENCES master_data.roles(id),
    CONSTRAINT fk_approval_matrices_action_type FOREIGN KEY (approval_action_type_id) REFERENCES master_data.approval_action_types(id),
    CONSTRAINT ck_approval_matrices_level_no CHECK (level_no >= 1),
    CONSTRAINT ck_approval_matrices_escalation_days CHECK (escalation_days IS NULL OR escalation_days >= 0),
    CONSTRAINT ck_approval_matrices_amount_range CHECK (min_amount IS NULL OR max_amount IS NULL OR min_amount <= max_amount),
    CONSTRAINT ck_approval_matrices_source_type CHECK (approver_source_type IN ('ROLE', 'USER', 'REPORT_TO_POSITION', 'DEPARTMENT_HEAD', 'WORKFLOW_INITIATOR_MANAGER'))
);

CREATE TABLE IF NOT EXISTS master_data.notification_templates (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    template_code VARCHAR(128) NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    event_code VARCHAR(128) NOT NULL,
    channel_type VARCHAR(32) NOT NULL,
    subject_template TEXT,
    body_template TEXT NOT NULL,
    language_code VARCHAR(32),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_notification_templates_tenant_code UNIQUE (tenant_id, template_code),
    CONSTRAINT ck_notification_templates_channel_type CHECK (channel_type IN ('EMAIL', 'SMS', 'PUSH', 'IN_APP'))
);

CREATE TABLE IF NOT EXISTS master_data.delegation_types (
    id UUID PRIMARY KEY,
    delegation_type_code VARCHAR(128) NOT NULL UNIQUE,
    delegation_type_name VARCHAR(255) NOT NULL,
    approval_allowed_flag BOOLEAN,
    action_allowed_flag BOOLEAN,
    view_allowed_flag BOOLEAN,
    temporary_only_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_roles_tenant_active ON master_data.roles (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_permissions_active ON master_data.permissions (active);
CREATE INDEX IF NOT EXISTS idx_role_permission_mappings_tenant_active ON master_data.role_permission_mappings (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_workflow_types_active ON master_data.workflow_types (active);
CREATE INDEX IF NOT EXISTS idx_approval_matrices_tenant_active ON master_data.approval_matrices (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_notification_templates_tenant_active ON master_data.notification_templates (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_service_request_types_tenant_active ON master_data.service_request_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_delegation_types_active ON master_data.delegation_types (active);
CREATE INDEX IF NOT EXISTS idx_approval_action_types_active ON master_data.approval_action_types (active);
