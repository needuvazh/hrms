ALTER TABLE master_data.lookup_values
    ALTER COLUMN tenant_id DROP NOT NULL;

ALTER TABLE master_data.lookup_values
    ADD COLUMN IF NOT EXISTS country_code VARCHAR(8),
    ADD COLUMN IF NOT EXISTS effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    ADD COLUMN IF NOT EXISTS effective_to DATE,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS created_by VARCHAR(128),
    ADD COLUMN IF NOT EXISTS updated_by VARCHAR(128),
    ADD COLUMN IF NOT EXISTS change_reason TEXT;

CREATE INDEX IF NOT EXISTS idx_lookup_values_tenant_country_type
    ON master_data.lookup_values (tenant_id, country_code, lookup_type, is_active);

CREATE TABLE IF NOT EXISTS master_data.currency_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    currency_code VARCHAR(8) NOT NULL,
    currency_name VARCHAR(128) NOT NULL,
    symbol VARCHAR(16),
    minor_unit SMALLINT NOT NULL DEFAULT 2,
    rounding_mode VARCHAR(32) NOT NULL DEFAULT 'HALF_UP',
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
    CONSTRAINT ck_currency_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_currency_master_scope_code
    ON master_data.currency_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), currency_code, effective_from);

CREATE INDEX IF NOT EXISTS idx_currency_master_tenant_country_active
    ON master_data.currency_master (tenant_id, country_code, is_active);

CREATE TABLE IF NOT EXISTS master_data.language_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    language_code VARCHAR(16) NOT NULL,
    language_name VARCHAR(128) NOT NULL,
    native_name VARCHAR(128),
    is_rtl BOOLEAN NOT NULL DEFAULT FALSE,
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
    CONSTRAINT ck_language_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_language_master_scope_code
    ON master_data.language_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), language_code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.timezone_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    timezone_code VARCHAR(64) NOT NULL,
    timezone_name VARCHAR(128) NOT NULL,
    utc_offset_minutes INTEGER,
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
    CONSTRAINT ck_timezone_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_timezone_master_scope_code
    ON master_data.timezone_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), timezone_code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.country_locale_map (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8) NOT NULL,
    default_currency_code VARCHAR(8) NOT NULL,
    default_language_code VARCHAR(16) NOT NULL,
    default_timezone_code VARCHAR(64) NOT NULL,
    first_day_of_week SMALLINT NOT NULL DEFAULT 1,
    weekend_days VARCHAR(32) NOT NULL DEFAULT 'FRI,SAT',
    date_format VARCHAR(32) NOT NULL DEFAULT 'yyyy-MM-dd',
    time_format VARCHAR(32) NOT NULL DEFAULT 'HH:mm',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_country_locale_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_country_locale_map_scope
    ON master_data.country_locale_map (COALESCE(tenant_id, 'GLOBAL'), country_code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.nationality_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    country_code VARCHAR(8),
    nationality_code VARCHAR(16) NOT NULL,
    nationality_name VARCHAR(128) NOT NULL,
    iso_alpha2 VARCHAR(8),
    iso_alpha3 VARCHAR(8),
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
    CONSTRAINT ck_nationality_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_nationality_master_scope_code
    ON master_data.nationality_master (COALESCE(tenant_id, 'GLOBAL'), COALESCE(country_code, 'GLOBAL'), nationality_code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.business_unit_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
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
    CONSTRAINT ck_business_unit_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_business_unit_master_scope_code
    ON master_data.business_unit_master (tenant_id, COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE INDEX IF NOT EXISTS idx_business_unit_tenant_country_active
    ON master_data.business_unit_master (tenant_id, country_code, is_active);

CREATE TABLE IF NOT EXISTS master_data.department_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    country_code VARCHAR(8),
    business_unit_id UUID,
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
    CONSTRAINT fk_department_business_unit FOREIGN KEY (business_unit_id) REFERENCES master_data.business_unit_master(id),
    CONSTRAINT ck_department_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_department_master_scope_code
    ON master_data.department_master (tenant_id, COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE INDEX IF NOT EXISTS idx_department_tenant_country_active
    ON master_data.department_master (tenant_id, country_code, is_active);

CREATE TABLE IF NOT EXISTS master_data.designation_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
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
    CONSTRAINT ck_designation_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_designation_master_scope_code
    ON master_data.designation_master (tenant_id, COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.grade_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    level_order INTEGER NOT NULL DEFAULT 0,
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
    CONSTRAINT ck_grade_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_grade_master_scope_code
    ON master_data.grade_master (tenant_id, COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.job_family_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
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
    CONSTRAINT ck_job_family_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_job_family_master_scope_code
    ON master_data.job_family_master (tenant_id, COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.job_level_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    level_order INTEGER NOT NULL DEFAULT 0,
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
    CONSTRAINT ck_job_level_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_job_level_master_scope_code
    ON master_data.job_level_master (tenant_id, COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.location_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(128),
    state VARCHAR(128),
    postal_code VARCHAR(32),
    address_line_1 VARCHAR(255),
    address_line_2 VARCHAR(255),
    latitude NUMERIC(10, 6),
    longitude NUMERIC(10, 6),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_to DATE,
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128),
    updated_by VARCHAR(128),
    change_reason TEXT,
    CONSTRAINT ck_location_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_location_master_scope_code
    ON master_data.location_master (tenant_id, COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.cost_center_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    country_code VARCHAR(8),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
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
    CONSTRAINT ck_cost_center_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_cost_center_master_scope_code
    ON master_data.cost_center_master (tenant_id, COALESCE(country_code, 'GLOBAL'), code, effective_from);

CREATE TABLE IF NOT EXISTS master_data.org_unit_master (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    country_code VARCHAR(8),
    parent_org_unit_id UUID,
    org_unit_type VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    org_path VARCHAR(1024),
    depth SMALLINT NOT NULL DEFAULT 0,
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
    CONSTRAINT fk_org_unit_parent FOREIGN KEY (parent_org_unit_id) REFERENCES master_data.org_unit_master(id),
    CONSTRAINT ck_org_unit_effective_window CHECK (effective_to IS NULL OR effective_to >= effective_from)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_org_unit_master_scope_code
    ON master_data.org_unit_master (tenant_id, COALESCE(country_code, 'GLOBAL'), org_unit_type, code, effective_from);

CREATE INDEX IF NOT EXISTS idx_org_unit_master_parent
    ON master_data.org_unit_master (tenant_id, parent_org_unit_id);
