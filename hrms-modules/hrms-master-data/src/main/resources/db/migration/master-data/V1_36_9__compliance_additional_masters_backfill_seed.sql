INSERT INTO master_data.social_insurance_eligibility_types (
    id, tenant_id, social_insurance_type_code, social_insurance_type_name,
    pension_eligible_flag, occupational_hazard_eligible_flag, govt_contribution_applicable_flag,
    description, active, created_by, updated_by
)
VALUES
    (
        'c3369000-0000-0000-0000-000000000001',
        'default',
        'PASI_OMANI',
        'PASI Eligible Omani',
        TRUE,
        TRUE,
        TRUE,
        'Omani employee PASI-eligible profile',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3369000-0000-0000-0000-000000000002',
        'lite',
        'PASI_OMANI',
        'PASI Eligible Omani',
        TRUE,
        TRUE,
        TRUE,
        'Lite PASI-eligible Omani profile',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, social_insurance_type_code) DO UPDATE
SET social_insurance_type_name = EXCLUDED.social_insurance_type_name,
    pension_eligible_flag = EXCLUDED.pension_eligible_flag,
    occupational_hazard_eligible_flag = EXCLUDED.occupational_hazard_eligible_flag,
    govt_contribution_applicable_flag = EXCLUDED.govt_contribution_applicable_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.beneficiary_types (
    id, tenant_id, beneficiary_type_code, beneficiary_type_name, priority_order, description, active, created_by, updated_by
)
VALUES
    ('c3369000-0000-0000-0000-000000000011', 'default', 'SPOUSE', 'Spouse Beneficiary', 1, 'Primary spouse beneficiary', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3369000-0000-0000-0000-000000000012', 'default', 'CHILD', 'Child Beneficiary', 2, 'Child beneficiary', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3369000-0000-0000-0000-000000000013', 'lite', 'SPOUSE', 'Spouse Beneficiary', 1, 'Lite spouse beneficiary', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3369000-0000-0000-0000-000000000014', 'lite', 'CHILD', 'Child Beneficiary', 2, 'Lite child beneficiary', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, beneficiary_type_code) DO UPDATE
SET beneficiary_type_name = EXCLUDED.beneficiary_type_name,
    priority_order = EXCLUDED.priority_order,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.dependent_types (
    id, tenant_id, dependent_type_code, dependent_type_name,
    insurance_eligible_flag, family_visa_eligible_flag, description, active, created_by, updated_by
)
VALUES
    ('c3369000-0000-0000-0000-000000000021', 'default', 'SPOUSE_DEP', 'Spouse', TRUE, TRUE, 'Spouse dependent classification', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3369000-0000-0000-0000-000000000022', 'default', 'CHILD_DEP', 'Child', TRUE, TRUE, 'Child dependent classification', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3369000-0000-0000-0000-000000000023', 'lite', 'SPOUSE_DEP', 'Spouse', TRUE, TRUE, 'Lite spouse dependent type', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3369000-0000-0000-0000-000000000024', 'lite', 'CHILD_DEP', 'Child', TRUE, TRUE, 'Lite child dependent type', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, dependent_type_code) DO UPDATE
SET dependent_type_name = EXCLUDED.dependent_type_name,
    insurance_eligible_flag = EXCLUDED.insurance_eligible_flag,
    family_visa_eligible_flag = EXCLUDED.family_visa_eligible_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
