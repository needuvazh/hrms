CREATE TABLE IF NOT EXISTS master_data.subscription_plans (
    id UUID PRIMARY KEY,
    plan_code VARCHAR(64) NOT NULL UNIQUE,
    plan_name VARCHAR(128) NOT NULL,
    description TEXT,
    max_users INTEGER,
    max_storage_gb INTEGER,
    monthly_price NUMERIC(12, 2),
    annual_price NUMERIC(12, 2),
    currency_code VARCHAR(8),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    CONSTRAINT ck_subscription_plans_max_users CHECK (max_users IS NULL OR max_users >= 0),
    CONSTRAINT ck_subscription_plans_max_storage_gb CHECK (max_storage_gb IS NULL OR max_storage_gb >= 0),
    CONSTRAINT ck_subscription_plans_monthly_price CHECK (monthly_price IS NULL OR monthly_price >= 0),
    CONSTRAINT ck_subscription_plans_annual_price CHECK (annual_price IS NULL OR annual_price >= 0)
);

CREATE INDEX IF NOT EXISTS idx_subscription_plans_active_name
    ON master_data.subscription_plans (active, plan_name);

CREATE TABLE IF NOT EXISTS master_data.tenant_settings (
    tenant_code VARCHAR(64) PRIMARY KEY,
    legal_name VARCHAR(255) NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    default_timezone VARCHAR(64) NOT NULL,
    go_live_date DATE,
    default_language_code VARCHAR(16),
    home_country_code VARCHAR(8),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    CONSTRAINT fk_tenant_settings_tenant FOREIGN KEY (tenant_code)
        REFERENCES tenant.tenants (tenant_code)
);

CREATE INDEX IF NOT EXISTS idx_tenant_settings_home_country
    ON master_data.tenant_settings (home_country_code);

CREATE TABLE IF NOT EXISTS master_data.tenant_subscriptions (
    tenant_code VARCHAR(64) PRIMARY KEY,
    subscription_plan_id UUID NOT NULL,
    subscription_start_date DATE NOT NULL,
    subscription_end_date DATE,
    auto_renew BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    CONSTRAINT fk_tenant_subscriptions_tenant FOREIGN KEY (tenant_code)
        REFERENCES tenant.tenants (tenant_code),
    CONSTRAINT fk_tenant_subscriptions_plan FOREIGN KEY (subscription_plan_id)
        REFERENCES master_data.subscription_plans (id),
    CONSTRAINT ck_tenant_subscriptions_window CHECK (
        subscription_end_date IS NULL OR subscription_end_date >= subscription_start_date
    )
);

CREATE INDEX IF NOT EXISTS idx_tenant_subscriptions_plan
    ON master_data.tenant_subscriptions (subscription_plan_id, active);

CREATE TABLE IF NOT EXISTS master_data.tenant_branding (
    tenant_code VARCHAR(64) PRIMARY KEY,
    brand_name VARCHAR(128) NOT NULL,
    logo_url VARCHAR(512),
    favicon_url VARCHAR(512),
    primary_color VARCHAR(16),
    secondary_color VARCHAR(16),
    login_banner_url VARCHAR(512),
    email_logo_url VARCHAR(512),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    CONSTRAINT fk_tenant_branding_tenant FOREIGN KEY (tenant_code)
        REFERENCES tenant.tenants (tenant_code)
);

CREATE TABLE IF NOT EXISTS master_data.tenant_localization_preferences (
    id UUID PRIMARY KEY,
    tenant_code VARCHAR(64) NOT NULL,
    country_code VARCHAR(8) NOT NULL,
    default_language_code VARCHAR(16) NOT NULL,
    date_format VARCHAR(32) NOT NULL,
    time_format VARCHAR(32) NOT NULL,
    week_start_day VARCHAR(16) NOT NULL,
    currency_code VARCHAR(8) NOT NULL,
    number_format VARCHAR(32) NOT NULL,
    rtl_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    public_holiday_calendar_code VARCHAR(64),
    calendar_type VARCHAR(32),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(128) NOT NULL,
    updated_by VARCHAR(128) NOT NULL,
    CONSTRAINT uq_tenant_localization_preferences UNIQUE (tenant_code, country_code),
    CONSTRAINT fk_tenant_localization_preferences_tenant FOREIGN KEY (tenant_code)
        REFERENCES tenant.tenants (tenant_code)
);

CREATE INDEX IF NOT EXISTS idx_tenant_localization_preferences_lookup
    ON master_data.tenant_localization_preferences (tenant_code, active, country_code);
