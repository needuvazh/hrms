CREATE SCHEMA IF NOT EXISTS organization;

CREATE TABLE IF NOT EXISTS organization.legal_entities (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    legal_entity_code VARCHAR(64) NOT NULL,
    legal_entity_name VARCHAR(255) NOT NULL,
    short_name VARCHAR(120),
    registration_no VARCHAR(120),
    tax_no VARCHAR(120),
    country_code VARCHAR(16),
    base_currency_code VARCHAR(16),
    default_language_code VARCHAR(16),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(64),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(120),
    state VARCHAR(120),
    postal_code VARCHAR(32),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_organization_legal_entities_tenant_code UNIQUE (tenant_id, legal_entity_code)
);

CREATE TABLE IF NOT EXISTS organization.branches (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    legal_entity_id UUID NOT NULL,
    branch_code VARCHAR(64) NOT NULL,
    branch_name VARCHAR(255) NOT NULL,
    branch_short_name VARCHAR(120),
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(120),
    state VARCHAR(120),
    country_code VARCHAR(16),
    postal_code VARCHAR(32),
    phone VARCHAR(64),
    fax VARCHAR(64),
    email VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_organization_branches_legal_entity FOREIGN KEY (legal_entity_id) REFERENCES organization.legal_entities (id),
    CONSTRAINT uq_organization_branches_tenant_code UNIQUE (tenant_id, branch_code)
);

CREATE TABLE IF NOT EXISTS organization.business_units (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    legal_entity_id UUID,
    business_unit_code VARCHAR(64) NOT NULL,
    business_unit_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_organization_business_units_legal_entity FOREIGN KEY (legal_entity_id) REFERENCES organization.legal_entities (id),
    CONSTRAINT uq_organization_business_units_tenant_code UNIQUE (tenant_id, business_unit_code)
);

CREATE TABLE IF NOT EXISTS organization.divisions (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    legal_entity_id UUID,
    business_unit_id UUID,
    branch_id UUID,
    division_code VARCHAR(64) NOT NULL,
    division_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_organization_divisions_legal_entity FOREIGN KEY (legal_entity_id) REFERENCES organization.legal_entities (id),
    CONSTRAINT fk_organization_divisions_business_unit FOREIGN KEY (business_unit_id) REFERENCES organization.business_units (id),
    CONSTRAINT fk_organization_divisions_branch FOREIGN KEY (branch_id) REFERENCES organization.branches (id),
    CONSTRAINT uq_organization_divisions_tenant_code UNIQUE (tenant_id, division_code)
);

CREATE TABLE IF NOT EXISTS organization.departments (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    legal_entity_id UUID,
    business_unit_id UUID,
    division_id UUID,
    branch_id UUID,
    department_code VARCHAR(64) NOT NULL,
    department_name VARCHAR(255) NOT NULL,
    short_name VARCHAR(120),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_organization_departments_legal_entity FOREIGN KEY (legal_entity_id) REFERENCES organization.legal_entities (id),
    CONSTRAINT fk_organization_departments_business_unit FOREIGN KEY (business_unit_id) REFERENCES organization.business_units (id),
    CONSTRAINT fk_organization_departments_division FOREIGN KEY (division_id) REFERENCES organization.divisions (id),
    CONSTRAINT fk_organization_departments_branch FOREIGN KEY (branch_id) REFERENCES organization.branches (id),
    CONSTRAINT uq_organization_departments_tenant_code UNIQUE (tenant_id, department_code)
);

CREATE TABLE IF NOT EXISTS organization.sections (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    department_id UUID NOT NULL,
    section_code VARCHAR(64) NOT NULL,
    section_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_organization_sections_department FOREIGN KEY (department_id) REFERENCES organization.departments (id),
    CONSTRAINT uq_organization_sections_tenant_code UNIQUE (tenant_id, section_code)
);

CREATE TABLE IF NOT EXISTS organization.work_locations (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    legal_entity_id UUID,
    branch_id UUID,
    location_code VARCHAR(64) NOT NULL,
    location_name VARCHAR(255) NOT NULL,
    location_type VARCHAR(32) NOT NULL,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city VARCHAR(120),
    state VARCHAR(120),
    country_code VARCHAR(16),
    postal_code VARCHAR(32),
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    geofence_radius NUMERIC(10, 2),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_organization_work_locations_legal_entity FOREIGN KEY (legal_entity_id) REFERENCES organization.legal_entities (id),
    CONSTRAINT fk_organization_work_locations_branch FOREIGN KEY (branch_id) REFERENCES organization.branches (id),
    CONSTRAINT uq_organization_work_locations_tenant_code UNIQUE (tenant_id, location_code)
);

CREATE TABLE IF NOT EXISTS organization.cost_centers (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    legal_entity_id UUID,
    cost_center_code VARCHAR(64) NOT NULL,
    cost_center_name VARCHAR(255) NOT NULL,
    description TEXT,
    gl_account_code VARCHAR(64),
    parent_cost_center_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_organization_cost_centers_legal_entity FOREIGN KEY (legal_entity_id) REFERENCES organization.legal_entities (id),
    CONSTRAINT fk_organization_cost_centers_parent FOREIGN KEY (parent_cost_center_id) REFERENCES organization.cost_centers (id),
    CONSTRAINT uq_organization_cost_centers_tenant_code UNIQUE (tenant_id, cost_center_code)
);

CREATE TABLE IF NOT EXISTS organization.reporting_units (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    reporting_unit_code VARCHAR(64) NOT NULL,
    reporting_unit_name VARCHAR(255) NOT NULL,
    parent_reporting_unit_id UUID,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_organization_reporting_units_parent FOREIGN KEY (parent_reporting_unit_id) REFERENCES organization.reporting_units (id),
    CONSTRAINT uq_organization_reporting_units_tenant_code UNIQUE (tenant_id, reporting_unit_code)
);

CREATE TABLE IF NOT EXISTS organization.audit_logs (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(120) NOT NULL,
    target_id VARCHAR(120),
    metadata JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_organization_legal_entities_tenant_active ON organization.legal_entities (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_organization_branches_tenant_active ON organization.branches (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_organization_business_units_tenant_active ON organization.business_units (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_organization_divisions_tenant_active ON organization.divisions (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_organization_departments_tenant_active ON organization.departments (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_organization_sections_tenant_active ON organization.sections (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_organization_work_locations_tenant_active ON organization.work_locations (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_organization_cost_centers_tenant_active ON organization.cost_centers (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_organization_reporting_units_tenant_active ON organization.reporting_units (tenant_id, active);
