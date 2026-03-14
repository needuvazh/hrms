CREATE SCHEMA IF NOT EXISTS document;

CREATE TABLE IF NOT EXISTS document.document_records (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    document_type VARCHAR(60) NOT NULL,
    entity_type VARCHAR(60) NOT NULL,
    entity_id VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    storage_provider VARCHAR(60) NOT NULL,
    storage_bucket VARCHAR(100),
    storage_object_key VARCHAR(300) NOT NULL,
    storage_checksum VARCHAR(120),
    content_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    expiry_date DATE,
    verification_status VARCHAR(40) NOT NULL,
    archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by VARCHAR(80) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by VARCHAR(80) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_document_records_tenant_entity
    ON document.document_records (tenant_id, entity_type, entity_id, archived, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_document_records_tenant_expiry
    ON document.document_records (tenant_id, expiry_date, archived);
