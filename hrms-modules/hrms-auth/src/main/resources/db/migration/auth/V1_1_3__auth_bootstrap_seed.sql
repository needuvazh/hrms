INSERT INTO auth.roles (id, tenant_id, role_code, role_name, description, is_system_role)
VALUES (
    '7d8f0d37-5b5b-4ab9-8d61-c7a2a85f9201',
    'platform',
    'SUPER_ADMIN',
    'Super Administrator',
    'Bootstrap platform super administrator role with unrestricted access',
    TRUE
)
ON CONFLICT (tenant_id, role_code) DO UPDATE
SET role_name = EXCLUDED.role_name,
    description = EXCLUDED.description,
    is_system_role = EXCLUDED.is_system_role,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO auth.users (
    id,
    tenant_id,
    username,
    email,
    password_hash,
    first_name,
    last_name,
    status,
    is_active,
    is_super_admin,
    can_view_all_tenants,
    created_by,
    updated_by
)
VALUES (
    '8e2dffb8-26ad-4a89-bc84-f397f9772112',
    'platform',
    'admin',
    'admin@local',
    '{bcrypt}$2y$10$fbH9laCuB75U8SoXilr9ieP99IDwvAa/epSF3VanNaLblalHDSl/.',
    'System',
    'Administrator',
    'ACTIVE',
    TRUE,
    TRUE,
    TRUE,
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (tenant_id, username) DO UPDATE
SET email = EXCLUDED.email,
    password_hash = EXCLUDED.password_hash,
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    status = EXCLUDED.status,
    is_active = EXCLUDED.is_active,
    is_super_admin = EXCLUDED.is_super_admin,
    can_view_all_tenants = EXCLUDED.can_view_all_tenants,
    updated_by = 'SYSTEM',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO auth.scopes (id, scope_code, scope_name, description)
VALUES
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307001', 'TENANT_ALL', 'Tenant All', 'Unrestricted tenant scope'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307002', 'TENANT_VIEW_ALL', 'Tenant View All', 'Can view all tenant data'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307003', 'TENANT_MANAGE_ALL', 'Tenant Manage All', 'Can manage all tenants'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307004', 'SYSTEM_ALL', 'System All', 'Can administer all platform settings'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307005', 'USER_ALL', 'User All', 'Can manage all users'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307006', 'ROLE_ALL', 'Role All', 'Can manage all roles'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307007', 'PERMISSION_ALL', 'Permission All', 'Can manage all permissions'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307008', 'SCOPE_ALL', 'Scope All', 'Can manage all scopes'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307009', 'CONFIG_ALL', 'Config All', 'Can manage global configuration'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307010', 'AUDIT_ALL', 'Audit All', 'Can access all audit data'),
    ('5e23f58f-1e28-4f6b-9ec8-5944ef307011', 'MASTER_ALL', 'Master All', 'Can manage all master data')
ON CONFLICT (scope_code) DO UPDATE
SET scope_name = EXCLUDED.scope_name,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO auth.permissions (id, tenant_id, permission_code, permission_name, module_code, description)
VALUES
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0001', 'platform', 'auth.login', 'Authenticate user login', 'AUTH', 'Issue JWT token from username and password'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0002', 'platform', 'auth.me', 'Read current authenticated user', 'AUTH', 'Read authenticated profile'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0003', 'platform', 'user.create', 'Create user', 'USER', 'Create security user'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0004', 'platform', 'user.read', 'Read user', 'USER', 'Read security users'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0005', 'platform', 'user.update', 'Update user', 'USER', 'Update security users'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0006', 'platform', 'user.delete', 'Delete user', 'USER', 'Delete security users'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0007', 'platform', 'user.activate', 'Activate user', 'USER', 'Activate security users'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0008', 'platform', 'user.deactivate', 'Deactivate user', 'USER', 'Deactivate security users'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0009', 'platform', 'role.create', 'Create role', 'ROLE', 'Create role'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0010', 'platform', 'role.read', 'Read role', 'ROLE', 'Read role'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0011', 'platform', 'role.update', 'Update role', 'ROLE', 'Update role'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0012', 'platform', 'role.delete', 'Delete role', 'ROLE', 'Delete role'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0013', 'platform', 'permission.read', 'Read permission', 'PERMISSION', 'Read permission'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0014', 'platform', 'scope.read', 'Read scope', 'SCOPE', 'Read scope'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0015', 'platform', 'tenant.create', 'Create tenant', 'TENANT', 'Create tenant'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0016', 'platform', 'tenant.read', 'Read tenant', 'TENANT', 'Read tenant'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0017', 'platform', 'tenant.update', 'Update tenant', 'TENANT', 'Update tenant'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0018', 'platform', 'tenant.delete', 'Delete tenant', 'TENANT', 'Delete tenant'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0019', 'platform', 'tenant.view.all', 'View all tenants', 'TENANT', 'Read all tenants'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0020', 'platform', 'tenant.manage.all', 'Manage all tenants', 'TENANT', 'Manage all tenants'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0021', 'platform', 'master.create', 'Create master data', 'MASTER', 'Create master data'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0022', 'platform', 'master.read', 'Read master data', 'MASTER', 'Read master data'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0023', 'platform', 'master.update', 'Update master data', 'MASTER', 'Update master data'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0024', 'platform', 'master.delete', 'Delete master data', 'MASTER', 'Delete master data'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0025', 'platform', 'employee.create', 'Create employee', 'EMPLOYEE', 'Create employee'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0026', 'platform', 'employee.read', 'Read employee', 'EMPLOYEE', 'Read employee'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0027', 'platform', 'employee.update', 'Update employee', 'EMPLOYEE', 'Update employee'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0028', 'platform', 'employee.delete', 'Delete employee', 'EMPLOYEE', 'Delete employee'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0029', 'platform', 'employee.view.all', 'View all employees', 'EMPLOYEE', 'View all employees'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0030', 'platform', 'company.create', 'Create company', 'ORGANIZATION', 'Create company'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0031', 'platform', 'company.read', 'Read company', 'ORGANIZATION', 'Read company'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0032', 'platform', 'company.update', 'Update company', 'ORGANIZATION', 'Update company'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0033', 'platform', 'company.delete', 'Delete company', 'ORGANIZATION', 'Delete company'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0034', 'platform', 'branch.create', 'Create branch', 'ORGANIZATION', 'Create branch'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0035', 'platform', 'branch.read', 'Read branch', 'ORGANIZATION', 'Read branch'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0036', 'platform', 'branch.update', 'Update branch', 'ORGANIZATION', 'Update branch'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0037', 'platform', 'branch.delete', 'Delete branch', 'ORGANIZATION', 'Delete branch'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0038', 'platform', 'department.create', 'Create department', 'ORGANIZATION', 'Create department'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0039', 'platform', 'department.read', 'Read department', 'ORGANIZATION', 'Read department'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0040', 'platform', 'department.update', 'Update department', 'ORGANIZATION', 'Update department'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0041', 'platform', 'department.delete', 'Delete department', 'ORGANIZATION', 'Delete department'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0042', 'platform', 'leave.create', 'Create leave', 'LEAVE', 'Create leave request'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0043', 'platform', 'leave.read', 'Read leave', 'LEAVE', 'Read leave data'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0044', 'platform', 'leave.update', 'Update leave', 'LEAVE', 'Update leave data'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0045', 'platform', 'leave.delete', 'Delete leave', 'LEAVE', 'Delete leave data'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0046', 'platform', 'leave.approve', 'Approve leave', 'LEAVE', 'Approve leave request'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0047', 'platform', 'leave.reject', 'Reject leave', 'LEAVE', 'Reject leave request'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0048', 'platform', 'attendance.create', 'Create attendance', 'ATTENDANCE', 'Create attendance record'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0049', 'platform', 'attendance.read', 'Read attendance', 'ATTENDANCE', 'Read attendance records'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0050', 'platform', 'attendance.update', 'Update attendance', 'ATTENDANCE', 'Update attendance record'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0051', 'platform', 'attendance.delete', 'Delete attendance', 'ATTENDANCE', 'Delete attendance record'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0052', 'platform', 'attendance.process', 'Process attendance', 'ATTENDANCE', 'Process attendance cycle'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0053', 'platform', 'payroll.create', 'Create payroll run', 'PAYROLL', 'Create payroll run'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0054', 'platform', 'payroll.read', 'Read payroll run', 'PAYROLL', 'Read payroll run'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0055', 'platform', 'payroll.update', 'Update payroll run', 'PAYROLL', 'Update payroll run'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0056', 'platform', 'payroll.delete', 'Delete payroll run', 'PAYROLL', 'Delete payroll run'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0057', 'platform', 'payroll.process', 'Process payroll', 'PAYROLL', 'Process payroll'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0058', 'platform', 'payroll.lock', 'Lock payroll run', 'PAYROLL', 'Lock payroll'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0059', 'platform', 'payroll.unlock', 'Unlock payroll run', 'PAYROLL', 'Unlock payroll'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0060', 'platform', 'payslip.view', 'View payslip', 'PAYROLL', 'View payslip'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0061', 'platform', 'payslip.publish', 'Publish payslip', 'PAYROLL', 'Publish payslip'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0062', 'platform', 'report.read', 'Read report', 'REPORT', 'Read report'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0063', 'platform', 'report.export', 'Export report', 'REPORT', 'Export report'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0064', 'platform', 'dashboard.read', 'Read dashboard', 'REPORT', 'Read dashboard'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0065', 'platform', 'audit.read', 'Read audit', 'AUDIT', 'Read audit'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0066', 'platform', 'audit.export', 'Export audit', 'AUDIT', 'Export audit'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0067', 'platform', 'EMPLOYEE_READ', 'Read employee data', 'EMPLOYEE', 'Legacy permission for existing endpoint security rules'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0068', 'platform', 'EMPLOYEE_WRITE', 'Manage employee data', 'EMPLOYEE', 'Legacy permission for existing endpoint security rules'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0069', 'platform', 'PERSON_READ', 'Read person data', 'PERSON', 'Legacy permission for existing endpoint security rules'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0070', 'platform', 'PERSON_WRITE', 'Manage person data', 'PERSON', 'Legacy permission for existing endpoint security rules'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0071', 'platform', 'RECRUITMENT_READ', 'Read recruitment data', 'RECRUITMENT', 'Legacy permission for existing endpoint security rules'),
    ('df4b2c2b-04c8-420d-9f9f-5cbf8a3d0072', 'platform', 'RECRUITMENT_WRITE', 'Manage recruitment data', 'RECRUITMENT', 'Legacy permission for existing endpoint security rules')
ON CONFLICT (tenant_id, permission_code) DO UPDATE
SET permission_name = EXCLUDED.permission_name,
    module_code = EXCLUDED.module_code,
    description = EXCLUDED.description,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO auth.user_roles (user_id, role_id, tenant_id)
SELECT u.id, r.id, r.tenant_id
FROM auth.users u
JOIN auth.roles r ON r.tenant_id = 'platform' AND r.role_code = 'SUPER_ADMIN'
WHERE u.tenant_id = 'platform' AND u.username = 'admin'
ON CONFLICT (user_id, role_id, tenant_id) DO NOTHING;

INSERT INTO auth.role_permissions (role_id, permission_id, tenant_id)
SELECT r.id, p.id, r.tenant_id
FROM auth.roles r
JOIN auth.permissions p ON p.tenant_id = r.tenant_id
WHERE r.tenant_id = 'platform' AND r.role_code = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id, tenant_id) DO NOTHING;

INSERT INTO auth.user_scopes (id, user_id, scope_id, scope_value)
SELECT
    ('9f8a9f1b-' || LPAD(ROW_NUMBER() OVER (ORDER BY s.scope_code)::text, 4, '0') || '-4c31-a2a4-611bb84200aa')::UUID,
    u.id,
    s.id,
    NULL
FROM auth.users u
JOIN auth.scopes s ON TRUE
WHERE u.tenant_id = 'platform' AND u.username = 'admin'
ON CONFLICT (id) DO NOTHING;
