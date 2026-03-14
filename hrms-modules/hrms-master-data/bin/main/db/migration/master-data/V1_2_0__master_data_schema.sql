CREATE SCHEMA IF NOT EXISTS master_data;

CREATE TABLE IF NOT EXISTS master_data.lookup_values (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    lookup_type VARCHAR(64) NOT NULL,
    lookup_code VARCHAR(64) NOT NULL,
    lookup_label VARCHAR(255) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_master_data_lookup UNIQUE (tenant_id, lookup_type, lookup_code)
);
