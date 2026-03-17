INSERT INTO job_architecture.employment_types (
    id, tenant_id, employment_type_code, employment_type_name, contract_required, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000051', 'default', 'FULL_TIME', 'Full Time', TRUE, 'Standard full-time employment', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000052', 'lite', 'FULL_TIME', 'Full Time', TRUE, 'Lite full-time employment', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, employment_type_code) DO UPDATE
SET employment_type_name = EXCLUDED.employment_type_name,
    contract_required = EXCLUDED.contract_required,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.worker_types (
    id, tenant_id, worker_type_code, worker_type_name, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000061', 'default', 'EMPLOYEE', 'Employee', 'Standard employee worker type', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000062', 'lite', 'EMPLOYEE', 'Employee', 'Lite employee worker type', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, worker_type_code) DO UPDATE
SET worker_type_name = EXCLUDED.worker_type_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.employee_categories (
    id, tenant_id, employee_category_code, employee_category_name, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000071', 'default', 'STAFF', 'Staff', 'Regular staff employee category', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000072', 'lite', 'STAFF', 'Staff', 'Lite staff employee category', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, employee_category_code) DO UPDATE
SET employee_category_name = EXCLUDED.employee_category_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.employee_subcategories (
    id, tenant_id, employee_subcategory_code, employee_subcategory_name, employee_category_id, description, active, created_by, updated_by
)
VALUES
    (
        'c3201000-0000-0000-0000-000000000081',
        'default',
        'PERMANENT',
        'Permanent',
        (SELECT id FROM job_architecture.employee_categories WHERE tenant_id = 'default' AND employee_category_code = 'STAFF'),
        'Permanent staff',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3201000-0000-0000-0000-000000000082',
        'lite',
        'PERMANENT',
        'Permanent',
        (SELECT id FROM job_architecture.employee_categories WHERE tenant_id = 'lite' AND employee_category_code = 'STAFF'),
        'Lite permanent staff',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, employee_subcategory_code) DO UPDATE
SET employee_subcategory_name = EXCLUDED.employee_subcategory_name,
    employee_category_id = EXCLUDED.employee_category_id,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.contract_types (
    id, tenant_id, contract_type_code, contract_type_name, fixed_term_flag, default_duration_days, renewal_allowed, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000091', 'default', 'UNLIMITED', 'Unlimited Contract', FALSE, NULL, TRUE, 'Unlimited duration contract', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000092', 'lite', 'LIMITED_2Y', 'Limited 2 Year Contract', TRUE, 730, TRUE, 'Two-year renewable contract', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, contract_type_code) DO UPDATE
SET contract_type_name = EXCLUDED.contract_type_name,
    fixed_term_flag = EXCLUDED.fixed_term_flag,
    default_duration_days = EXCLUDED.default_duration_days,
    renewal_allowed = EXCLUDED.renewal_allowed,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.probation_policies (
    id, tenant_id, probation_policy_code, probation_policy_name, duration_days, extension_allowed, max_extension_days, confirmation_required, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000101', 'default', 'PROB_90', '90 Day Probation', 90, TRUE, 90, TRUE, 'Standard 90-day probation policy', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000102', 'lite', 'PROB_60', '60 Day Probation', 60, TRUE, 30, TRUE, 'Lite 60-day probation policy', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, probation_policy_code) DO UPDATE
SET probation_policy_name = EXCLUDED.probation_policy_name,
    duration_days = EXCLUDED.duration_days,
    extension_allowed = EXCLUDED.extension_allowed,
    max_extension_days = EXCLUDED.max_extension_days,
    confirmation_required = EXCLUDED.confirmation_required,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.notice_period_policies (
    id, tenant_id, notice_policy_code, notice_policy_name, employee_notice_days, employer_notice_days, payment_in_lieu_allowed, garden_leave_allowed, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000111', 'default', 'NOTICE_30', '30 Day Notice', 30, 30, TRUE, FALSE, 'Standard 30 day bilateral notice policy', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000112', 'lite', 'NOTICE_15', '15 Day Notice', 15, 15, TRUE, FALSE, 'Lite 15 day notice policy', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, notice_policy_code) DO UPDATE
SET notice_policy_name = EXCLUDED.notice_policy_name,
    employee_notice_days = EXCLUDED.employee_notice_days,
    employer_notice_days = EXCLUDED.employer_notice_days,
    payment_in_lieu_allowed = EXCLUDED.payment_in_lieu_allowed,
    garden_leave_allowed = EXCLUDED.garden_leave_allowed,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.transfer_types (
    id, tenant_id, transfer_type_code, transfer_type_name, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000121', 'default', 'INTERNAL_TRANSFER', 'Internal Transfer', 'Default internal transfer type', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000122', 'lite', 'INTERNAL_TRANSFER', 'Internal Transfer', 'Lite internal transfer type', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, transfer_type_code) DO UPDATE
SET transfer_type_name = EXCLUDED.transfer_type_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.promotion_types (
    id, tenant_id, promotion_type_code, promotion_type_name, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000131', 'default', 'MERIT_PROMOTION', 'Merit Promotion', 'Performance-based promotion', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000132', 'lite', 'LEVEL_PROMOTION', 'Level Promotion', 'Lite level-based promotion', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, promotion_type_code) DO UPDATE
SET promotion_type_name = EXCLUDED.promotion_type_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO job_architecture.separation_reasons (
    id, tenant_id, separation_reason_code, separation_reason_name, separation_category, voluntary_flag, final_settlement_required, description, active, created_by, updated_by
)
VALUES
    ('c3201000-0000-0000-0000-000000000141', 'default', 'RESIGNATION', 'Resignation', 'VOLUNTARY', TRUE, TRUE, 'Employee initiated resignation', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3201000-0000-0000-0000-000000000142', 'lite', 'CONTRACT_END', 'Contract End', 'INVOLUNTARY', FALSE, TRUE, 'Contract completion separation', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, separation_reason_code) DO UPDATE
SET separation_reason_name = EXCLUDED.separation_reason_name,
    separation_category = EXCLUDED.separation_category,
    voluntary_flag = EXCLUDED.voluntary_flag,
    final_settlement_required = EXCLUDED.final_settlement_required,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
