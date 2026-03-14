CREATE TABLE IF NOT EXISTS master_data.currencies (
    id UUID PRIMARY KEY,
    currency_code VARCHAR(16) NOT NULL,
    currency_name VARCHAR(128) NOT NULL,
    currency_symbol VARCHAR(16),
    decimal_places SMALLINT NOT NULL DEFAULT 2,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_currencies_code UNIQUE (currency_code),
    CONSTRAINT ck_currencies_decimal_places CHECK (decimal_places >= 0)
);

CREATE INDEX IF NOT EXISTS idx_currencies_name ON master_data.currencies (currency_name);
CREATE INDEX IF NOT EXISTS idx_currencies_active ON master_data.currencies (active);

CREATE TABLE IF NOT EXISTS master_data.countries (
    id UUID PRIMARY KEY,
    country_code VARCHAR(16) NOT NULL,
    country_name VARCHAR(128) NOT NULL,
    short_name VARCHAR(64),
    iso2_code VARCHAR(2) NOT NULL,
    iso3_code VARCHAR(3) NOT NULL,
    phone_code VARCHAR(16),
    nationality_name VARCHAR(128),
    default_currency_code VARCHAR(16),
    default_timezone VARCHAR(64),
    gcc_flag BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_countries_code UNIQUE (country_code),
    CONSTRAINT uq_countries_iso2 UNIQUE (iso2_code),
    CONSTRAINT uq_countries_iso3 UNIQUE (iso3_code),
    CONSTRAINT fk_countries_default_currency FOREIGN KEY (default_currency_code) REFERENCES master_data.currencies(currency_code)
);

CREATE INDEX IF NOT EXISTS idx_countries_name ON master_data.countries (country_name);
CREATE INDEX IF NOT EXISTS idx_countries_active ON master_data.countries (active);

CREATE TABLE IF NOT EXISTS master_data.languages (
    id UUID PRIMARY KEY,
    language_code VARCHAR(16) NOT NULL,
    language_name VARCHAR(128) NOT NULL,
    native_name VARCHAR(128),
    rtl_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_languages_code UNIQUE (language_code)
);

CREATE INDEX IF NOT EXISTS idx_languages_name ON master_data.languages (language_name);
CREATE INDEX IF NOT EXISTS idx_languages_active ON master_data.languages (active);

CREATE TABLE IF NOT EXISTS master_data.nationalities (
    id UUID PRIMARY KEY,
    nationality_code VARCHAR(16) NOT NULL,
    nationality_name VARCHAR(128) NOT NULL,
    country_code VARCHAR(16),
    gcc_national_flag BOOLEAN NOT NULL DEFAULT FALSE,
    omani_flag BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_nationalities_code UNIQUE (nationality_code),
    CONSTRAINT uq_nationalities_name UNIQUE (nationality_name),
    CONSTRAINT fk_nationalities_country FOREIGN KEY (country_code) REFERENCES master_data.countries(country_code)
);

CREATE INDEX IF NOT EXISTS idx_nationalities_active ON master_data.nationalities (active);
CREATE INDEX IF NOT EXISTS idx_nationalities_country_code ON master_data.nationalities (country_code);

CREATE TABLE IF NOT EXISTS master_data.religions (
    id UUID PRIMARY KEY,
    religion_code VARCHAR(16) NOT NULL,
    religion_name VARCHAR(128) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_religions_code UNIQUE (religion_code),
    CONSTRAINT uq_religions_name UNIQUE (religion_name)
);

CREATE INDEX IF NOT EXISTS idx_religions_active ON master_data.religions (active);

CREATE TABLE IF NOT EXISTS master_data.genders (
    id UUID PRIMARY KEY,
    gender_code VARCHAR(16) NOT NULL,
    gender_name VARCHAR(128) NOT NULL,
    display_order INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_genders_code UNIQUE (gender_code),
    CONSTRAINT ck_genders_display_order CHECK (display_order IS NULL OR display_order >= 0)
);

CREATE INDEX IF NOT EXISTS idx_genders_active ON master_data.genders (active);

CREATE TABLE IF NOT EXISTS master_data.marital_statuses (
    id UUID PRIMARY KEY,
    marital_status_code VARCHAR(32) NOT NULL,
    marital_status_name VARCHAR(128) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_marital_statuses_code UNIQUE (marital_status_code),
    CONSTRAINT uq_marital_statuses_name UNIQUE (marital_status_name)
);

CREATE INDEX IF NOT EXISTS idx_marital_statuses_active ON master_data.marital_statuses (active);

CREATE TABLE IF NOT EXISTS master_data.relationship_types (
    id UUID PRIMARY KEY,
    relationship_type_code VARCHAR(32) NOT NULL,
    relationship_type_name VARCHAR(128) NOT NULL,
    dependent_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    emergency_contact_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    beneficiary_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_relationship_types_code UNIQUE (relationship_type_code)
);

CREATE INDEX IF NOT EXISTS idx_relationship_types_active ON master_data.relationship_types (active);

CREATE TABLE IF NOT EXISTS master_data.document_types (
    id UUID PRIMARY KEY,
    document_type_code VARCHAR(32) NOT NULL,
    document_type_name VARCHAR(128) NOT NULL,
    short_description VARCHAR(255),
    document_for VARCHAR(16) NOT NULL,
    issue_date_required BOOLEAN NOT NULL DEFAULT FALSE,
    expiry_date_required BOOLEAN NOT NULL DEFAULT FALSE,
    alert_required BOOLEAN NOT NULL DEFAULT FALSE,
    alert_days_before INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_document_types_code UNIQUE (document_type_code),
    CONSTRAINT ck_document_types_document_for CHECK (document_for IN ('EMPLOYEE', 'EMPLOYER', 'BOTH')),
    CONSTRAINT ck_document_types_alert_days CHECK (alert_days_before IS NULL OR alert_days_before >= 0),
    CONSTRAINT ck_document_types_alert_behavior CHECK (alert_required OR alert_days_before IS NULL OR alert_days_before = 0)
);

CREATE INDEX IF NOT EXISTS idx_document_types_active ON master_data.document_types (active);

CREATE TABLE IF NOT EXISTS master_data.education_levels (
    id UUID PRIMARY KEY,
    education_level_code VARCHAR(32) NOT NULL,
    education_level_name VARCHAR(128) NOT NULL,
    ranking_order INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_education_levels_code UNIQUE (education_level_code),
    CONSTRAINT ck_education_levels_ranking CHECK (ranking_order IS NULL OR ranking_order >= 0)
);

CREATE INDEX IF NOT EXISTS idx_education_levels_active ON master_data.education_levels (active);

CREATE TABLE IF NOT EXISTS master_data.certification_types (
    id UUID PRIMARY KEY,
    certification_type_code VARCHAR(32) NOT NULL,
    certification_type_name VARCHAR(128) NOT NULL,
    expiry_tracking_required BOOLEAN NOT NULL DEFAULT FALSE,
    issuing_body_required BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_certification_types_code UNIQUE (certification_type_code),
    CONSTRAINT uq_certification_types_name UNIQUE (certification_type_name)
);

CREATE INDEX IF NOT EXISTS idx_certification_types_active ON master_data.certification_types (active);

CREATE TABLE IF NOT EXISTS master_data.skill_categories (
    id UUID PRIMARY KEY,
    skill_category_code VARCHAR(32) NOT NULL,
    skill_category_name VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_skill_categories_code UNIQUE (skill_category_code),
    CONSTRAINT uq_skill_categories_name UNIQUE (skill_category_name)
);

CREATE INDEX IF NOT EXISTS idx_skill_categories_active ON master_data.skill_categories (active);

CREATE TABLE IF NOT EXISTS master_data.skills (
    id UUID PRIMARY KEY,
    skill_code VARCHAR(32) NOT NULL,
    skill_name VARCHAR(128) NOT NULL,
    skill_category_id UUID NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    updated_by VARCHAR(128) NOT NULL DEFAULT 'SYSTEM',
    CONSTRAINT uq_skills_code UNIQUE (skill_code),
    CONSTRAINT uq_skills_name_per_category UNIQUE (skill_category_id, skill_name),
    CONSTRAINT fk_skills_skill_category FOREIGN KEY (skill_category_id) REFERENCES master_data.skill_categories(id)
);

CREATE INDEX IF NOT EXISTS idx_skills_active ON master_data.skills (active);
CREATE INDEX IF NOT EXISTS idx_skills_skill_category ON master_data.skills (skill_category_id);
