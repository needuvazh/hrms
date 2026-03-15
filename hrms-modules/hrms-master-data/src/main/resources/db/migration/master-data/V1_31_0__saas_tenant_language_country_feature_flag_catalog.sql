CREATE TABLE IF NOT EXISTS master_data.tenant_languages (
    id UUID PRIMARY KEY,
    tenant_code VARCHAR(64) NOT NULL,
    language_code VARCHAR(16) NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INTEGER,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    CONSTRAINT uq_tenant_languages_tenant_language UNIQUE (tenant_code, language_code),
    CONSTRAINT fk_tenant_languages_tenant FOREIGN KEY (tenant_code)
        REFERENCES tenant.tenants (tenant_code),
    CONSTRAINT ck_tenant_languages_display_order CHECK (display_order IS NULL OR display_order >= 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_languages_default_per_tenant
    ON master_data.tenant_languages (tenant_code)
    WHERE is_default = TRUE;

CREATE INDEX IF NOT EXISTS idx_tenant_languages_lookup
    ON master_data.tenant_languages (tenant_code, active, language_code);

CREATE TABLE IF NOT EXISTS master_data.tenant_countries (
    id UUID PRIMARY KEY,
    tenant_code VARCHAR(64) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    default_currency_code VARCHAR(8),
    default_timezone VARCHAR(64),
    is_home_country BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    CONSTRAINT uq_tenant_countries_tenant_country UNIQUE (tenant_code, country_code),
    CONSTRAINT fk_tenant_countries_tenant FOREIGN KEY (tenant_code)
        REFERENCES tenant.tenants (tenant_code)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_countries_home_per_tenant
    ON master_data.tenant_countries (tenant_code)
    WHERE is_home_country = TRUE;

CREATE INDEX IF NOT EXISTS idx_tenant_countries_lookup
    ON master_data.tenant_countries (tenant_code, active, country_code);

CREATE TABLE IF NOT EXISTS master_data.feature_flags (
    id UUID PRIMARY KEY,
    feature_key VARCHAR(128) NOT NULL,
    feature_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    CONSTRAINT uq_feature_flags_feature_key UNIQUE (feature_key)
);

CREATE INDEX IF NOT EXISTS idx_feature_flags_active_name
    ON master_data.feature_flags (active, feature_name);
