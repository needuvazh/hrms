CREATE TABLE IF NOT EXISTS tenant.countries (
    country_code VARCHAR(8) PRIMARY KEY,
    country_name VARCHAR(128) NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    timezone VARCHAR(64) NOT NULL,
    locale VARCHAR(32) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant.tenant_country_config (
    tenant_code VARCHAR(64) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    compliance_profile VARCHAR(64) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_code, country_code),
    CONSTRAINT fk_tenant_country_config_tenant FOREIGN KEY (tenant_code)
        REFERENCES tenant.tenants (tenant_code),
    CONSTRAINT fk_tenant_country_config_country FOREIGN KEY (country_code)
        REFERENCES tenant.countries (country_code)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_tenant_country_primary
    ON tenant.tenant_country_config (tenant_code)
    WHERE is_primary = TRUE;

CREATE INDEX IF NOT EXISTS idx_tenant_country_config_tenant_active
    ON tenant.tenant_country_config (tenant_code, is_active);
