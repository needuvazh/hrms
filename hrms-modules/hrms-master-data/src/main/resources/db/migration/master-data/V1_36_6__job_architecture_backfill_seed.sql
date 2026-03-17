INSERT INTO job_architecture.job_families (
    id, tenant_id, job_family_code, job_family_name, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000001', 'default', 'HR_FAMILY', 'Human Resources', 'Human resources job family', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000002', 'lite', 'OPS_FAMILY', 'Operations', 'Lite operations job family', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, job_family_code) DO UPDATE
SET job_family_name = EXCLUDED.job_family_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.job_functions (
    id, tenant_id, job_function_code, job_function_name, job_family_id, description, active, created_by, updated_by
)
VALUES
    (
        'c3201000-0000-0000-0000-000000000011',
        'default',
        'HR_OPERATIONS',
        'HR Operations',
        (SELECT id FROM job_architecture.job_families WHERE tenant_id = 'default' AND job_family_code = 'HR_FAMILY'),
        'HR operations function',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3201000-0000-0000-0000-000000000012',
        'lite',
        'CORE_OPERATIONS',
        'Core Operations',
        (SELECT id FROM job_architecture.job_families WHERE tenant_id = 'lite' AND job_family_code = 'OPS_FAMILY'),
        'Lite core operations function',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, job_function_code) DO UPDATE
SET job_function_name = EXCLUDED.job_function_name,
    job_family_id = EXCLUDED.job_family_id,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.designations (
    id, tenant_id, designation_code, designation_name, short_name, job_family_id, job_function_id, description, active, created_by, updated_by
)
VALUES
    (
        'c3201000-0000-0000-0000-000000000021',
        'default',
        'HR_EXEC',
        'HR Executive',
        'HR Exec',
        (SELECT id FROM job_architecture.job_families WHERE tenant_id = 'default' AND job_family_code = 'HR_FAMILY'),
        (SELECT id FROM job_architecture.job_functions WHERE tenant_id = 'default' AND job_function_code = 'HR_OPERATIONS'),
        'Default HR executive role',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3201000-0000-0000-0000-000000000022',
        'lite',
        'HR_COORD',
        'HR Coordinator',
        'HR Coord',
        (SELECT id FROM job_architecture.job_families WHERE tenant_id = 'lite' AND job_family_code = 'OPS_FAMILY'),
        (SELECT id FROM job_architecture.job_functions WHERE tenant_id = 'lite' AND job_function_code = 'CORE_OPERATIONS'),
        'Lite HR coordinator role',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, designation_code) DO UPDATE
SET designation_name = EXCLUDED.designation_name,
    short_name = EXCLUDED.short_name,
    job_family_id = EXCLUDED.job_family_id,
    job_function_id = EXCLUDED.job_function_id,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.grade_bands (
    id, tenant_id, grade_band_code, grade_band_name, band_order, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000031', 'default', 'BAND_A', 'Band A', 1, 'Entry to mid-level band', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000032', 'lite', 'BAND_L1', 'Band L1', 1, 'Lite primary band', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, grade_band_code) DO UPDATE
SET grade_band_name = EXCLUDED.grade_band_name,
    band_order = EXCLUDED.band_order,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.grades (
    id, tenant_id, grade_code, grade_name, grade_band_id, ranking_order, salary_scale_min, salary_scale_max, description, active, created_by, updated_by
)
VALUES
    (
        'c3201000-0000-0000-0000-000000000041',
        'default',
        'G5',
        'Grade 5',
        (SELECT id FROM job_architecture.grade_bands WHERE tenant_id = 'default' AND grade_band_code = 'BAND_A'),
        5,
        600.00,
        1200.00,
        'Default grade 5',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3201000-0000-0000-0000-000000000042',
        'lite',
        'L2',
        'Level 2',
        (SELECT id FROM job_architecture.grade_bands WHERE tenant_id = 'lite' AND grade_band_code = 'BAND_L1'),
        2,
        450.00,
        900.00,
        'Lite level 2',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, grade_code) DO UPDATE
SET grade_name = EXCLUDED.grade_name,
    grade_band_id = EXCLUDED.grade_band_id,
    ranking_order = EXCLUDED.ranking_order,
    salary_scale_min = EXCLUDED.salary_scale_min,
    salary_scale_max = EXCLUDED.salary_scale_max,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.positions (
    id, tenant_id, position_code, position_name,
    designation_id, job_family_id, job_function_id, grade_id, grade_band_id,
    legal_entity_id, branch_id, business_unit_id, division_id, department_id, section_id,
    work_location_id, cost_center_id, reporting_unit_id, reports_to_position_id,
    approved_headcount, filled_headcount, vacancy_status, critical_position_flag,
    description, active, created_by, updated_by
)
VALUES
    (
        'c3201000-0000-0000-0000-000000000151',
        'default',
        'HR_EXEC_MCT',
        'HR Executive - Muscat',
        (SELECT id FROM job_architecture.designations WHERE tenant_id = 'default' AND designation_code = 'HR_EXEC'),
        (SELECT id FROM job_architecture.job_families WHERE tenant_id = 'default' AND job_family_code = 'HR_FAMILY'),
        (SELECT id FROM job_architecture.job_functions WHERE tenant_id = 'default' AND job_function_code = 'HR_OPERATIONS'),
        (SELECT id FROM job_architecture.grades WHERE tenant_id = 'default' AND grade_code = 'G5'),
        (SELECT id FROM job_architecture.grade_bands WHERE tenant_id = 'default' AND grade_band_code = 'BAND_A'),
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'default' AND branch_code = 'MCT_MAIN'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'default' AND business_unit_code = 'CORP'),
        (SELECT id FROM organization.divisions WHERE tenant_id = 'default' AND division_code = 'PEOPLE'),
        (SELECT id FROM organization.departments WHERE tenant_id = 'default' AND department_code = 'HR_OPS'),
        (SELECT id FROM organization.sections WHERE tenant_id = 'default' AND section_code = 'PAYROLL_SEC'),
        (SELECT id FROM organization.work_locations WHERE tenant_id = 'default' AND location_code = 'MCT_HQ'),
        (SELECT id FROM organization.cost_centers WHERE tenant_id = 'default' AND cost_center_code = 'CC_HR'),
        (SELECT id FROM organization.reporting_units WHERE tenant_id = 'default' AND reporting_unit_code = 'RU_CORP'),
        NULL,
        3,
        1,
        'PARTIALLY_FILLED',
        FALSE,
        'Primary HR operations position for default tenant',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3201000-0000-0000-0000-000000000152',
        'lite',
        'HR_COORD_SEEB',
        'HR Coordinator - Seeb',
        (SELECT id FROM job_architecture.designations WHERE tenant_id = 'lite' AND designation_code = 'HR_COORD'),
        (SELECT id FROM job_architecture.job_families WHERE tenant_id = 'lite' AND job_family_code = 'OPS_FAMILY'),
        (SELECT id FROM job_architecture.job_functions WHERE tenant_id = 'lite' AND job_function_code = 'CORE_OPERATIONS'),
        (SELECT id FROM job_architecture.grades WHERE tenant_id = 'lite' AND grade_code = 'L2'),
        (SELECT id FROM job_architecture.grade_bands WHERE tenant_id = 'lite' AND grade_band_code = 'BAND_L1'),
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'lite' AND branch_code = 'SEEB_MAIN'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'lite' AND business_unit_code = 'CORE'),
        (SELECT id FROM organization.divisions WHERE tenant_id = 'lite' AND division_code = 'HR'),
        (SELECT id FROM organization.departments WHERE tenant_id = 'lite' AND department_code = 'HR'),
        (SELECT id FROM organization.sections WHERE tenant_id = 'lite' AND section_code = 'GEN_SEC'),
        (SELECT id FROM organization.work_locations WHERE tenant_id = 'lite' AND location_code = 'SEEB_HQ'),
        (SELECT id FROM organization.cost_centers WHERE tenant_id = 'lite' AND cost_center_code = 'CC_CORE'),
        (SELECT id FROM organization.reporting_units WHERE tenant_id = 'lite' AND reporting_unit_code = 'RU_CORE'),
        NULL,
        2,
        1,
        'PARTIALLY_FILLED',
        FALSE,
        'Core HR position for lite tenant',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, position_code) DO UPDATE
SET position_name = EXCLUDED.position_name,
    designation_id = EXCLUDED.designation_id,
    job_family_id = EXCLUDED.job_family_id,
    job_function_id = EXCLUDED.job_function_id,
    grade_id = EXCLUDED.grade_id,
    grade_band_id = EXCLUDED.grade_band_id,
    legal_entity_id = EXCLUDED.legal_entity_id,
    branch_id = EXCLUDED.branch_id,
    business_unit_id = EXCLUDED.business_unit_id,
    division_id = EXCLUDED.division_id,
    department_id = EXCLUDED.department_id,
    section_id = EXCLUDED.section_id,
    work_location_id = EXCLUDED.work_location_id,
    cost_center_id = EXCLUDED.cost_center_id,
    reporting_unit_id = EXCLUDED.reporting_unit_id,
    reports_to_position_id = EXCLUDED.reports_to_position_id,
    approved_headcount = EXCLUDED.approved_headcount,
    filled_headcount = EXCLUDED.filled_headcount,
    vacancy_status = EXCLUDED.vacancy_status,
    critical_position_flag = EXCLUDED.critical_position_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
