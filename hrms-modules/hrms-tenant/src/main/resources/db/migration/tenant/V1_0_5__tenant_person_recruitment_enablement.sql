INSERT INTO tenant.module_catalog (module_key, module_name, is_active)
VALUES
    ('person', 'Person', TRUE),
    ('recruitment', 'Recruitment', TRUE)
ON CONFLICT (module_key) DO NOTHING;

INSERT INTO tenant.tenant_module_subscriptions (tenant_code, module_key, is_enabled)
VALUES
    ('default', 'person', TRUE),
    ('default', 'recruitment', TRUE),
    ('lite', 'person', FALSE),
    ('lite', 'recruitment', FALSE)
ON CONFLICT (tenant_code, module_key) DO NOTHING;

INSERT INTO tenant.tenant_feature_flags (tenant_code, feature_key, is_enabled)
VALUES
    ('default', 'person.registry', TRUE),
    ('default', 'person.search', TRUE),
    ('default', 'recruitment.pipeline', TRUE),
    ('lite', 'person.registry', FALSE),
    ('lite', 'person.search', FALSE),
    ('lite', 'recruitment.pipeline', FALSE)
ON CONFLICT (tenant_code, feature_key) DO NOTHING;
