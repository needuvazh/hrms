CREATE SCHEMA IF NOT EXISTS person;

CREATE TABLE IF NOT EXISTS person.persons (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    person_code VARCHAR(64) NOT NULL,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128),
    email VARCHAR(255) NOT NULL,
    mobile VARCHAR(32),
    country_code VARCHAR(8) NOT NULL,
    nationality_code VARCHAR(8),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_person_tenant_code UNIQUE (tenant_id, person_code),
    CONSTRAINT uq_person_tenant_email UNIQUE (tenant_id, email)
);

CREATE TABLE IF NOT EXISTS person.person_identifiers (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    person_id UUID NOT NULL,
    identifier_type VARCHAR(32) NOT NULL,
    identifier_value_masked VARCHAR(255) NOT NULL,
    identifier_hash VARCHAR(255),
    country_code VARCHAR(8),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_person_identifiers_person FOREIGN KEY (person_id) REFERENCES person.persons(id),
    CONSTRAINT uq_person_identifier UNIQUE (tenant_id, identifier_type, identifier_hash)
);

CREATE TABLE IF NOT EXISTS person.person_lifecycle_history (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    person_id UUID NOT NULL,
    lifecycle_type VARCHAR(64) NOT NULL,
    effective_at TIMESTAMPTZ NOT NULL,
    details_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_person_lifecycle_person FOREIGN KEY (person_id) REFERENCES person.persons(id)
);

CREATE INDEX IF NOT EXISTS idx_persons_tenant_created_at ON person.persons (tenant_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_person_identifiers_person ON person.person_identifiers (tenant_id, person_id);
CREATE INDEX IF NOT EXISTS idx_person_lifecycle_person ON person.person_lifecycle_history (tenant_id, person_id, effective_at DESC);
