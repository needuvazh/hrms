CREATE TABLE IF NOT EXISTS master_data.visa_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    visa_type_code VARCHAR(64) NOT NULL,
    visa_type_name VARCHAR(255) NOT NULL,
    visa_category VARCHAR(128),
    applies_to VARCHAR(32) NOT NULL,
    renewable_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_visa_types_tenant_code UNIQUE (tenant_id, visa_type_code),
    CONSTRAINT ck_visa_types_applies_to CHECK (applies_to IN ('EMPLOYEE', 'DEPENDENT', 'BOTH'))
);

CREATE TABLE IF NOT EXISTS master_data.residence_statuses (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    residence_status_code VARCHAR(64) NOT NULL,
    residence_status_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_residence_statuses_tenant_code UNIQUE (tenant_id, residence_status_code)
);

CREATE TABLE IF NOT EXISTS master_data.labour_card_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    labour_card_type_code VARCHAR(64) NOT NULL,
    labour_card_type_name VARCHAR(255) NOT NULL,
    expiry_tracking_required BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_labour_card_types_tenant_code UNIQUE (tenant_id, labour_card_type_code)
);

CREATE TABLE IF NOT EXISTS master_data.civil_id_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    civil_id_type_code VARCHAR(64) NOT NULL,
    civil_id_type_name VARCHAR(255) NOT NULL,
    applies_to VARCHAR(32) NOT NULL,
    expiry_tracking_required BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_civil_id_types_tenant_code UNIQUE (tenant_id, civil_id_type_code),
    CONSTRAINT ck_civil_id_types_applies_to CHECK (applies_to IN ('OMANI', 'EXPATRIATE', 'BOTH'))
);

CREATE TABLE IF NOT EXISTS master_data.passport_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    passport_type_code VARCHAR(64) NOT NULL,
    passport_type_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_passport_types_tenant_code UNIQUE (tenant_id, passport_type_code)
);

CREATE TABLE IF NOT EXISTS master_data.sponsor_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    sponsor_type_code VARCHAR(64) NOT NULL,
    sponsor_type_name VARCHAR(255) NOT NULL,
    applies_to VARCHAR(32) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_sponsor_types_tenant_code UNIQUE (tenant_id, sponsor_type_code),
    CONSTRAINT ck_sponsor_types_applies_to CHECK (applies_to IN ('EMPLOYEE', 'DEPENDENT', 'BOTH'))
);

CREATE TABLE IF NOT EXISTS master_data.work_permit_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    work_permit_type_code VARCHAR(64) NOT NULL,
    work_permit_type_name VARCHAR(255) NOT NULL,
    renewable_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_work_permit_types_tenant_code UNIQUE (tenant_id, work_permit_type_code)
);

CREATE TABLE IF NOT EXISTS master_data.nationalisation_categories (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    nationalisation_category_code VARCHAR(64) NOT NULL,
    nationalisation_category_name VARCHAR(255) NOT NULL,
    omani_flag BOOLEAN,
    counts_for_omanisation_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_nationalisation_categories_tenant_code UNIQUE (tenant_id, nationalisation_category_code)
);

CREATE TABLE IF NOT EXISTS master_data.social_insurance_eligibility_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    social_insurance_type_code VARCHAR(64) NOT NULL,
    social_insurance_type_name VARCHAR(255) NOT NULL,
    pension_eligible_flag BOOLEAN,
    occupational_hazard_eligible_flag BOOLEAN,
    govt_contribution_applicable_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_social_insurance_types_tenant_code UNIQUE (tenant_id, social_insurance_type_code)
);

CREATE TABLE IF NOT EXISTS master_data.beneficiary_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    beneficiary_type_code VARCHAR(64) NOT NULL,
    beneficiary_type_name VARCHAR(255) NOT NULL,
    priority_order INTEGER,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_beneficiary_types_tenant_code UNIQUE (tenant_id, beneficiary_type_code),
    CONSTRAINT ck_beneficiary_types_priority_order CHECK (priority_order IS NULL OR priority_order >= 0)
);

CREATE TABLE IF NOT EXISTS master_data.dependent_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    dependent_type_code VARCHAR(64) NOT NULL,
    dependent_type_name VARCHAR(255) NOT NULL,
    insurance_eligible_flag BOOLEAN,
    family_visa_eligible_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_dependent_types_tenant_code UNIQUE (tenant_id, dependent_type_code)
);

CREATE TABLE IF NOT EXISTS master_data.audit_logs (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    entity_name VARCHAR(128) NOT NULL,
    entity_id VARCHAR(128) NOT NULL,
    action VARCHAR(64) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    changed_by VARCHAR(64),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(64),
    source VARCHAR(128),
    correlation_id VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_visa_types_tenant_active ON master_data.visa_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_residence_statuses_tenant_active ON master_data.residence_statuses (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_labour_card_types_tenant_active ON master_data.labour_card_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_civil_id_types_tenant_active ON master_data.civil_id_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_passport_types_tenant_active ON master_data.passport_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_sponsor_types_tenant_active ON master_data.sponsor_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_work_permit_types_tenant_active ON master_data.work_permit_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_nationalisation_categories_tenant_active ON master_data.nationalisation_categories (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_social_insurance_types_tenant_active ON master_data.social_insurance_eligibility_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_beneficiary_types_tenant_active ON master_data.beneficiary_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_dependent_types_tenant_active ON master_data.dependent_types (tenant_id, active);
