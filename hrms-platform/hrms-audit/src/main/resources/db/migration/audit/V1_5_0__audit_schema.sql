CREATE SCHEMA IF NOT EXISTS audit;

CREATE TABLE IF NOT EXISTS audit.audit_events (
    id BIGSERIAL PRIMARY KEY,
    actor VARCHAR(128) NOT NULL,
    tenant_id VARCHAR(64) NOT NULL,
    action VARCHAR(128) NOT NULL,
    target_type VARCHAR(128) NOT NULL,
    target_id VARCHAR(128) NOT NULL,
    event_timestamp TIMESTAMPTZ NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_events_tenant_timestamp
    ON audit.audit_events (tenant_id, event_timestamp DESC);

CREATE INDEX IF NOT EXISTS idx_audit_events_target
    ON audit.audit_events (tenant_id, target_type, target_id);
