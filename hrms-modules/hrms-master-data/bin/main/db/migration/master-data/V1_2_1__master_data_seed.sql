INSERT INTO master_data.lookup_values (id, tenant_id, lookup_type, lookup_code, lookup_label, sort_order, is_active)
VALUES
    ('31111111-1111-1111-1111-111111111111', 'default', 'DEPARTMENT', 'ENG', 'Engineering', 1, TRUE),
    ('32222222-2222-2222-2222-222222222222', 'default', 'DEPARTMENT', 'HR', 'Human Resources', 2, TRUE),
    ('33333333-3333-3333-3333-333333333333', 'default', 'JOB_TITLE', 'SDE_1', 'Software Engineer I', 1, TRUE)
ON CONFLICT (tenant_id, lookup_type, lookup_code) DO NOTHING;
