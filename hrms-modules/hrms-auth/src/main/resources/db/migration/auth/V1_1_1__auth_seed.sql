INSERT INTO auth.users (id, tenant_id, username, email, password_hash, is_active)
VALUES ('21111111-1111-1111-1111-111111111111', 'default', 'admin', 'admin@default.hrms', '{noop}admin123', TRUE)
ON CONFLICT (tenant_id, username) DO NOTHING;

INSERT INTO auth.roles (id, tenant_id, role_code, role_name)
VALUES ('22222222-2222-2222-2222-222222222222', 'default', 'HR_ADMIN', 'HR Administrator')
ON CONFLICT (tenant_id, role_code) DO NOTHING;

INSERT INTO auth.permissions (id, tenant_id, permission_code, permission_name)
VALUES
    ('23333333-3333-3333-3333-333333333333', 'default', 'EMPLOYEE_READ', 'Read Employee Data'),
    ('24444444-4444-4444-4444-444444444444', 'default', 'EMPLOYEE_WRITE', 'Manage Employee Data')
ON CONFLICT (tenant_id, permission_code) DO NOTHING;

INSERT INTO auth.user_roles (user_id, role_id, tenant_id)
VALUES ('21111111-1111-1111-1111-111111111111', '22222222-2222-2222-2222-222222222222', 'default')
ON CONFLICT (user_id, role_id, tenant_id) DO NOTHING;

INSERT INTO auth.role_permissions (role_id, permission_id, tenant_id)
VALUES
    ('22222222-2222-2222-2222-222222222222', '23333333-3333-3333-3333-333333333333', 'default'),
    ('22222222-2222-2222-2222-222222222222', '24444444-4444-4444-4444-444444444444', 'default')
ON CONFLICT (role_id, permission_id, tenant_id) DO NOTHING;
