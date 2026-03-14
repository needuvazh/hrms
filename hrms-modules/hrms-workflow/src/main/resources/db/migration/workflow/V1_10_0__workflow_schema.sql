CREATE SCHEMA IF NOT EXISTS workflow;

CREATE TABLE IF NOT EXISTS workflow.workflow_definitions (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    workflow_key VARCHAR(80) NOT NULL,
    workflow_name VARCHAR(120) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, workflow_key)
);

CREATE TABLE IF NOT EXISTS workflow.workflow_instances (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    workflow_definition_id UUID NOT NULL,
    workflow_key VARCHAR(80) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id VARCHAR(120) NOT NULL,
    approval_status VARCHAR(30) NOT NULL,
    requested_by VARCHAR(120) NOT NULL,
    decided_by VARCHAR(120),
    submitted_at TIMESTAMPTZ NOT NULL,
    decided_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_instances_definition FOREIGN KEY (workflow_definition_id)
        REFERENCES workflow.workflow_definitions (id)
);

CREATE TABLE IF NOT EXISTS workflow.workflow_steps (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    workflow_instance_id UUID NOT NULL,
    step_order INT NOT NULL,
    actor VARCHAR(120) NOT NULL,
    workflow_action VARCHAR(30) NOT NULL,
    comments TEXT,
    acted_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workflow_steps_instance FOREIGN KEY (workflow_instance_id)
        REFERENCES workflow.workflow_instances (id)
);

CREATE INDEX IF NOT EXISTS idx_workflow_instances_tenant_target
    ON workflow.workflow_instances (tenant_id, target_type, target_id);

CREATE INDEX IF NOT EXISTS idx_workflow_steps_instance_order
    ON workflow.workflow_steps (workflow_instance_id, step_order);
