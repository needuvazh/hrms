INSERT INTO employee.employees (
    id,
    tenant_id,
    employee_code,
    first_name,
    last_name,
    email,
    department_code,
    job_title
) VALUES (
    '41111111-1111-1111-1111-111111111111',
    'default',
    'EMP-0001',
    'System',
    'Admin',
    'system.admin@default.hrms',
    'HR',
    'HR Administrator'
)
ON CONFLICT (tenant_id, employee_code) DO NOTHING;
