CREATE TABLE IF NOT EXISTS master_data.employment_type_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    full_time_equivalent NUMERIC(6, 3),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_employment_type_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_employment_type_master_scope_code
    ON master_data.employment_type_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.contract_type_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    max_duration_months INTEGER,
    renewable_flag BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_contract_type_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_contract_type_master_scope_code
    ON master_data.contract_type_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.worker_category_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_worker_category_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_worker_category_master_scope_code
    ON master_data.worker_category_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.probation_policy_class_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    default_days INTEGER,
    extension_allowed_flag BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_probation_policy_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_probation_policy_class_master_scope_code
    ON master_data.probation_policy_class_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.application_source_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    external_flag BOOLEAN NOT NULL DEFAULT TRUE,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_application_source_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_application_source_master_scope_code
    ON master_data.application_source_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.hiring_stage_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    stage_order INTEGER NOT NULL DEFAULT 0,
    terminal_flag BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_hiring_stage_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_hiring_stage_master_scope_code
    ON master_data.hiring_stage_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.interview_type_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    modality VARCHAR(32),
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_interview_type_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_interview_type_master_scope_code
    ON master_data.interview_type_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.offer_status_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    terminal_flag BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_offer_status_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_offer_status_master_scope_code
    ON master_data.offer_status_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.rejection_reason_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    reason_category VARCHAR(64),
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_rejection_reason_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_rejection_reason_master_scope_code
    ON master_data.rejection_reason_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.pay_component_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    component_type VARCHAR(32) NOT NULL,
    calculation_basis VARCHAR(32) NOT NULL,
    taxable_flag BOOLEAN NOT NULL DEFAULT FALSE,
    social_insurance_flag BOOLEAN NOT NULL DEFAULT FALSE,
    affects_net_pay_flag BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_pay_component_type CHECK (component_type IN ('EARNING', 'DEDUCTION', 'BENEFIT', 'TAX')),
    CONSTRAINT ck_pay_component_calculation_basis CHECK (calculation_basis IN ('FIXED', 'PERCENT', 'FORMULA_REF', 'SLAB')),
    CONSTRAINT ck_pay_component_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_pay_component_master_scope_code
    ON master_data.pay_component_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.deduction_type_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    mandatory_flag BOOLEAN NOT NULL DEFAULT FALSE,
    recurring_flag BOOLEAN NOT NULL DEFAULT FALSE,
    statutory_flag BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_deduction_type_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_deduction_type_master_scope_code
    ON master_data.deduction_type_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.benefit_type_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    employee_contribution_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    employer_contribution_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_benefit_type_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_benefit_type_master_scope_code
    ON master_data.benefit_type_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.tax_category_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category_type VARCHAR(64),
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_tax_category_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_tax_category_master_scope_code
    ON master_data.tax_category_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.status_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    status_domain VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    terminal_flag BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_status_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_status_master_scope_code
    ON master_data.status_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), status_domain, code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.reason_code_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    reason_domain VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    requires_comment BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_reason_code_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_reason_code_master_scope_code
    ON master_data.reason_code_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), reason_domain, code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.document_type_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(64),
    mandatory_flag BOOLEAN NOT NULL DEFAULT FALSE,
    expiry_required_flag BOOLEAN NOT NULL DEFAULT FALSE,
    retention_years INTEGER,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_document_type_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_document_type_master_scope_code
    ON master_data.document_type_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), code, effective_from);
