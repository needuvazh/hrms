CREATE TABLE IF NOT EXISTS master_data.document_categories (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    document_category_code VARCHAR(64) NOT NULL,
    document_category_name VARCHAR(255) NOT NULL,
    description TEXT,
    display_order INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_document_categories_tenant_code UNIQUE (tenant_id, document_category_code),
    CONSTRAINT ck_document_categories_display_order CHECK (display_order IS NULL OR display_order >= 0)
);

CREATE TABLE IF NOT EXISTS master_data.document_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    document_type_code VARCHAR(64) NOT NULL,
    document_type_name VARCHAR(255) NOT NULL,
    short_description VARCHAR(255),
    document_for VARCHAR(32) NOT NULL,
    document_category_id UUID,
    attachment_required BOOLEAN,
    issue_date_required BOOLEAN,
    expiry_date_required BOOLEAN,
    reference_no_required BOOLEAN,
    multiple_allowed BOOLEAN,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_document_types_tenant_code UNIQUE (tenant_id, document_type_code),
    CONSTRAINT ck_document_types_document_for CHECK (document_for IN ('EMPLOYEE', 'EMPLOYER', 'DEPENDENT', 'BOTH')),
    CONSTRAINT fk_document_types_category FOREIGN KEY (document_category_id) REFERENCES master_data.document_categories(id)
);

ALTER TABLE master_data.document_types
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(64),
    ADD COLUMN IF NOT EXISTS document_category_id UUID,
    ADD COLUMN IF NOT EXISTS attachment_required BOOLEAN,
    ADD COLUMN IF NOT EXISTS reference_no_required BOOLEAN,
    ADD COLUMN IF NOT EXISTS multiple_allowed BOOLEAN;

UPDATE master_data.document_types
SET tenant_id = 'default'
WHERE tenant_id IS NULL;

ALTER TABLE master_data.document_types
    ALTER COLUMN tenant_id SET NOT NULL;

ALTER TABLE master_data.document_types
    DROP CONSTRAINT IF EXISTS uq_document_types_code;

ALTER TABLE master_data.document_types
    DROP CONSTRAINT IF EXISTS ck_document_types_document_for;

ALTER TABLE master_data.document_types
    ADD CONSTRAINT ck_document_types_document_for CHECK (document_for IN ('EMPLOYEE', 'EMPLOYER', 'DEPENDENT', 'BOTH'));

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uq_document_types_tenant_code'
          AND conrelid = 'master_data.document_types'::regclass
    ) THEN
        ALTER TABLE master_data.document_types
            ADD CONSTRAINT uq_document_types_tenant_code UNIQUE (tenant_id, document_type_code);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_document_types_category'
          AND conrelid = 'master_data.document_types'::regclass
    ) THEN
        ALTER TABLE master_data.document_types
            ADD CONSTRAINT fk_document_types_category
            FOREIGN KEY (document_category_id) REFERENCES master_data.document_categories(id);
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS master_data.document_applicability_rules (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    applicability_rule_code VARCHAR(64) NOT NULL,
    document_type_id UUID NOT NULL,
    worker_type_id UUID,
    employee_category_id UUID,
    nationalisation_category_id UUID,
    legal_entity_id UUID,
    job_family_id UUID,
    designation_id UUID,
    dependent_type_id UUID,
    mandatory_flag BOOLEAN,
    onboarding_required_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_doc_app_rules_tenant_code UNIQUE (tenant_id, applicability_rule_code),
    CONSTRAINT fk_doc_app_rules_document_type FOREIGN KEY (document_type_id) REFERENCES master_data.document_types(id),
    CONSTRAINT fk_doc_app_rules_worker_type FOREIGN KEY (worker_type_id) REFERENCES job_architecture.worker_types(id),
    CONSTRAINT fk_doc_app_rules_employee_category FOREIGN KEY (employee_category_id) REFERENCES job_architecture.employee_categories(id),
    CONSTRAINT fk_doc_app_rules_nationalisation_category FOREIGN KEY (nationalisation_category_id) REFERENCES master_data.nationalisation_categories(id),
    CONSTRAINT fk_doc_app_rules_legal_entity FOREIGN KEY (legal_entity_id) REFERENCES organization.legal_entities(id),
    CONSTRAINT fk_doc_app_rules_job_family FOREIGN KEY (job_family_id) REFERENCES job_architecture.job_families(id),
    CONSTRAINT fk_doc_app_rules_designation FOREIGN KEY (designation_id) REFERENCES job_architecture.designations(id),
    CONSTRAINT fk_doc_app_rules_dependent_type FOREIGN KEY (dependent_type_id) REFERENCES master_data.dependent_types(id)
);

CREATE TABLE IF NOT EXISTS master_data.document_expiry_rules (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    expiry_rule_code VARCHAR(64) NOT NULL,
    document_type_id UUID NOT NULL,
    expiry_tracking_required BOOLEAN,
    renewal_required BOOLEAN,
    alert_days_before_json JSONB,
    grace_period_days INTEGER,
    block_transaction_on_expiry_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_doc_exp_rules_tenant_code UNIQUE (tenant_id, expiry_rule_code),
    CONSTRAINT uq_doc_exp_rules_tenant_document_type UNIQUE (tenant_id, document_type_id),
    CONSTRAINT fk_doc_exp_rules_document_type FOREIGN KEY (document_type_id) REFERENCES master_data.document_types(id),
    CONSTRAINT ck_doc_exp_rules_grace_period CHECK (grace_period_days IS NULL OR grace_period_days >= 0)
);

CREATE TABLE IF NOT EXISTS master_data.policy_document_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    policy_document_type_code VARCHAR(64) NOT NULL,
    policy_document_type_name VARCHAR(255) NOT NULL,
    description TEXT,
    version_required_flag BOOLEAN,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_policy_doc_types_tenant_code UNIQUE (tenant_id, policy_document_type_code)
);

CREATE TABLE IF NOT EXISTS master_data.policy_acknowledgement_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    policy_ack_type_code VARCHAR(64) NOT NULL,
    policy_ack_type_name VARCHAR(255) NOT NULL,
    e_signature_required_flag BOOLEAN,
    reack_on_version_change_flag BOOLEAN,
    annual_reack_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_policy_ack_types_tenant_code UNIQUE (tenant_id, policy_ack_type_code)
);

CREATE TABLE IF NOT EXISTS master_data.attachment_categories (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    attachment_category_code VARCHAR(64) NOT NULL,
    attachment_category_name VARCHAR(255) NOT NULL,
    mime_group VARCHAR(32) NOT NULL,
    max_file_size_mb INTEGER,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_attachment_categories_tenant_code UNIQUE (tenant_id, attachment_category_code),
    CONSTRAINT ck_attachment_categories_mime_group CHECK (mime_group IN ('PDF', 'IMAGE', 'OFFICE_DOC', 'ARCHIVE', 'OTHER')),
    CONSTRAINT ck_attachment_categories_max_file_size CHECK (max_file_size_mb IS NULL OR max_file_size_mb > 0)
);

CREATE INDEX IF NOT EXISTS idx_document_categories_tenant_active ON master_data.document_categories (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_document_types_tenant_active ON master_data.document_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_document_types_category ON master_data.document_types (tenant_id, document_category_id);
CREATE INDEX IF NOT EXISTS idx_doc_app_rules_tenant_active ON master_data.document_applicability_rules (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_doc_app_rules_document_type ON master_data.document_applicability_rules (tenant_id, document_type_id);
CREATE INDEX IF NOT EXISTS idx_doc_exp_rules_tenant_active ON master_data.document_expiry_rules (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_doc_exp_rules_document_type ON master_data.document_expiry_rules (tenant_id, document_type_id);
CREATE INDEX IF NOT EXISTS idx_policy_doc_types_tenant_active ON master_data.policy_document_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_policy_ack_types_tenant_active ON master_data.policy_acknowledgement_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_attachment_categories_tenant_active ON master_data.attachment_categories (tenant_id, active);
