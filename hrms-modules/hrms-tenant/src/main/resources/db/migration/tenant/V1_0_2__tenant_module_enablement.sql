CREATE TABLE IF NOT EXISTS tenant.module_catalog (
    module_key VARCHAR(64) PRIMARY KEY,
    module_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tenant.tenant_module_subscriptions (
    tenant_code VARCHAR(64) NOT NULL,
    module_key VARCHAR(64) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_code, module_key),
    CONSTRAINT fk_tenant_module_subscriptions_tenant FOREIGN KEY (tenant_code)
        REFERENCES tenant.tenants (tenant_code),
    CONSTRAINT fk_tenant_module_subscriptions_catalog FOREIGN KEY (module_key)
        REFERENCES tenant.module_catalog (module_key)
);

CREATE TABLE IF NOT EXISTS tenant.tenant_feature_flags (
    tenant_code VARCHAR(64) NOT NULL,
    feature_key VARCHAR(128) NOT NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (tenant_code, feature_key),
    CONSTRAINT fk_tenant_feature_flags_tenant FOREIGN KEY (tenant_code)
        REFERENCES tenant.tenants (tenant_code)
);

INSERT INTO tenant.module_catalog (module_key, module_name, is_active)
VALUES
    ('employee', 'Employee', TRUE),
    ('person', 'Person', TRUE),
    ('recruitment', 'Recruitment', TRUE),
    ('attendance', 'Attendance', TRUE),
    ('leave', 'Leave', TRUE),
    ('payroll', 'Payroll', TRUE)
ON CONFLICT (module_key) DO NOTHING;

INSERT INTO tenant.tenant_module_subscriptions (tenant_code, module_key, is_enabled)
VALUES
    ('default', 'employee', TRUE),
    ('default', 'person', TRUE),
    ('default', 'recruitment', TRUE),
    ('default', 'attendance', TRUE),
    ('default', 'leave', TRUE),
    ('default', 'payroll', TRUE),
    ('lite', 'employee', FALSE),
    ('lite', 'person', FALSE),
    ('lite', 'recruitment', FALSE),
    ('lite', 'attendance', FALSE),
    ('lite', 'leave', TRUE),
    ('lite', 'payroll', FALSE)
ON CONFLICT (tenant_code, module_key) DO NOTHING;

INSERT INTO tenant.tenant_feature_flags (tenant_code, feature_key, is_enabled)
VALUES
    ('default', 'employee.search', TRUE),
    ('default', 'person.registry', TRUE),
    ('default', 'person.search', TRUE),
    ('default', 'recruitment.pipeline', TRUE),
    ('default', 'employee.export', FALSE),
    ('lite', 'employee.search', FALSE),
    ('lite', 'person.registry', FALSE),
    ('lite', 'person.search', FALSE),
    ('lite', 'recruitment.pipeline', FALSE)
ON CONFLICT (tenant_code, feature_key) DO NOTHING;
