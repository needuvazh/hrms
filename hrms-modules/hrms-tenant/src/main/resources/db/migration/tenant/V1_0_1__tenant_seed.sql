INSERT INTO tenant.tenants (id, tenant_code, tenant_name, is_active)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'default', 'Default Tenant', TRUE),
    ('12222222-2222-2222-2222-222222222222', 'lite', 'Lite Tenant', TRUE)
ON CONFLICT (tenant_code) DO NOTHING;
