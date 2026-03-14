CREATE TABLE IF NOT EXISTS audit.audit_event_log (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(128) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    action VARCHAR(128) NOT NULL,
    target_type VARCHAR(128) NOT NULL,
    target_id VARCHAR(128) NOT NULL,
    module_name VARCHAR(64) NOT NULL,
    entity_version BIGINT,
    changed_fields JSONB NOT NULL DEFAULT '[]'::jsonb,
    old_values JSONB NOT NULL DEFAULT '{}'::jsonb,
    new_values JSONB NOT NULL DEFAULT '{}'::jsonb,
    changed_by_actor_type VARCHAR(64) NOT NULL,
    changed_by_actor_id VARCHAR(128) NOT NULL,
    approved_by_actor_id VARCHAR(128),
    change_reason TEXT,
    source_service VARCHAR(128) NOT NULL,
    source_event_id VARCHAR(128),
    request_id VARCHAR(128),
    transaction_id VARCHAR(128),
    workflow_id VARCHAR(128),
    correlation_id VARCHAR(128),
    event_timestamp TIMESTAMPTZ NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    legal_hold_flag BOOLEAN NOT NULL DEFAULT FALSE,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_event_log_tenant_timestamp
    ON audit.audit_event_log (tenant_id, event_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_audit_event_log_target
    ON audit.audit_event_log (tenant_id, target_type, target_id, event_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_audit_event_log_correlation
    ON audit.audit_event_log (tenant_id, correlation_id, event_timestamp DESC);

CREATE TABLE IF NOT EXISTS audit.person_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    person_id UUID NOT NULL,
    action_type VARCHAR(64) NOT NULL,
    effective_at TIMESTAMPTZ NOT NULL,
    old_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    new_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_person_audit_history_person
    ON audit.person_audit_history (tenant_id, person_id, effective_at DESC);

CREATE TABLE IF NOT EXISTS audit.recruitment_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    candidate_id UUID NOT NULL,
    application_id UUID,
    action_type VARCHAR(64) NOT NULL,
    status_from VARCHAR(64),
    status_to VARCHAR(64),
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_recruitment_audit_history_candidate
    ON audit.recruitment_audit_history (tenant_id, candidate_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS audit.employee_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    employee_id UUID NOT NULL,
    person_id UUID,
    action_type VARCHAR(64) NOT NULL,
    old_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    new_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_employee_audit_history_employee
    ON audit.employee_audit_history (tenant_id, employee_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS audit.attendance_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    employee_id UUID NOT NULL,
    attendance_date DATE,
    attendance_event_id UUID,
    action_type VARCHAR(64) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit.leave_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    leave_request_id UUID,
    employee_id UUID,
    action_type VARCHAR(64) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit.shift_assignment_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    assignment_id UUID,
    employee_id UUID,
    action_type VARCHAR(64) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit.payroll_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    payroll_run_id UUID,
    employee_id UUID,
    action_type VARCHAR(64) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit.workflow_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    workflow_instance_id UUID,
    action_type VARCHAR(64) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit.document_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    document_id UUID,
    entity_type VARCHAR(64),
    entity_id UUID,
    action_type VARCHAR(64) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit.notification_audit_history (
    id BIGSERIAL PRIMARY KEY,
    audit_event_id BIGINT NOT NULL REFERENCES audit.audit_event_log(id),
    tenant_id VARCHAR(64) NOT NULL,
    notification_id UUID,
    channel VARCHAR(64),
    action_type VARCHAR(64) NOT NULL,
    details JSONB NOT NULL DEFAULT '{}'::jsonb,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit.read_model_sync_audit (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    projector_name VARCHAR(128) NOT NULL,
    source_service VARCHAR(128) NOT NULL,
    source_stream VARCHAR(128) NOT NULL,
    source_event_id VARCHAR(128),
    source_offset VARCHAR(128),
    entity_type VARCHAR(128) NOT NULL,
    entity_id VARCHAR(128) NOT NULL,
    target_table VARCHAR(128) NOT NULL,
    target_pk VARCHAR(128) NOT NULL,
    projection_action VARCHAR(64) NOT NULL,
    projector_version VARCHAR(64) NOT NULL,
    sync_status VARCHAR(32) NOT NULL,
    sync_started_at TIMESTAMPTZ NOT NULL,
    sync_completed_at TIMESTAMPTZ,
    error_code VARCHAR(64),
    error_payload JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_read_model_sync_audit_tenant_time
    ON audit.read_model_sync_audit (tenant_id, sync_started_at DESC);

CREATE TABLE IF NOT EXISTS audit.pipeline_error_audit (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    source_service VARCHAR(128) NOT NULL,
    source_event_id VARCHAR(128),
    entity_type VARCHAR(128),
    entity_id VARCHAR(128),
    projector_name VARCHAR(128),
    failure_stage VARCHAR(64) NOT NULL,
    error_class VARCHAR(256) NOT NULL,
    error_message TEXT NOT NULL,
    retry_count INT NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMPTZ,
    payload_ref TEXT,
    first_failed_at TIMESTAMPTZ NOT NULL,
    last_failed_at TIMESTAMPTZ NOT NULL,
    resolved_at TIMESTAMPTZ,
    resolved_by VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_pipeline_error_audit_tenant_state
    ON audit.pipeline_error_audit (tenant_id, resolved_at, last_failed_at DESC);
