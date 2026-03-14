CREATE SCHEMA IF NOT EXISTS integration_hub;

CREATE TABLE IF NOT EXISTS integration_hub.integration_definitions (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    integration_key VARCHAR(80) NOT NULL,
    provider_type VARCHAR(40) NOT NULL,
    display_name VARCHAR(150) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_integration_definitions_tenant_key UNIQUE (tenant_id, integration_key)
);

CREATE TABLE IF NOT EXISTS integration_hub.integration_endpoints (
    id UUID PRIMARY KEY,
    definition_id UUID NOT NULL REFERENCES integration_hub.integration_definitions(id),
    tenant_id VARCHAR(64) NOT NULL,
    endpoint_key VARCHAR(80) NOT NULL,
    base_url VARCHAR(500) NOT NULL,
    auth_type VARCHAR(80),
    configuration_json TEXT,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_integration_endpoints_tenant_key UNIQUE (tenant_id, endpoint_key)
);

CREATE TABLE IF NOT EXISTS integration_hub.integration_executions (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    definition_id UUID NOT NULL REFERENCES integration_hub.integration_definitions(id),
    endpoint_id UUID NOT NULL REFERENCES integration_hub.integration_endpoints(id),
    operation VARCHAR(120) NOT NULL,
    payload_json TEXT,
    status VARCHAR(20) NOT NULL,
    external_reference VARCHAR(255),
    error_message TEXT,
    attempted_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_integration_executions_definition_attempted
    ON integration_hub.integration_executions (tenant_id, definition_id, attempted_at DESC);
