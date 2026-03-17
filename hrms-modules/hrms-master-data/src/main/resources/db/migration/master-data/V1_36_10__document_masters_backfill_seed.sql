INSERT INTO master_data.document_categories (
    id, tenant_id, document_category_code, document_category_name, description, display_order, active, created_by, updated_by
)
VALUES
    ('c3610000-0000-0000-0000-000000000001', 'default', 'IDENTITY_DOCS', 'Identity Documents', 'Identity verification documents', 1, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3610000-0000-0000-0000-000000000002', 'default', 'COMPLIANCE_DOCS', 'Compliance Documents', 'Statutory and compliance-related employee documents', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3610000-0000-0000-0000-000000000003', 'lite', 'IDENTITY_DOCS', 'Identity Documents', 'Identity verification documents', 1, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3610000-0000-0000-0000-000000000004', 'lite', 'COMPLIANCE_DOCS', 'Compliance Documents', 'Lite statutory and compliance documents', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, document_category_code) DO UPDATE
SET document_category_name = EXCLUDED.document_category_name,
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.document_types (
    id, tenant_id, document_type_code, document_type_name, short_description, document_for, document_category_id,
    attachment_required, issue_date_required, expiry_date_required, reference_no_required, multiple_allowed,
    active, created_by, updated_by
)
VALUES
    (
        'c3610000-0000-0000-0000-000000000011', 'default', 'PASSPORT', 'Passport', 'Employee passport copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'IDENTITY_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3610000-0000-0000-0000-000000000012', 'default', 'WORK_PERMIT_DOC', 'Work Permit Document', 'Employee work permit copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'COMPLIANCE_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3610000-0000-0000-0000-000000000013', 'lite', 'PASSPORT', 'Passport', 'Employee passport copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'lite' AND document_category_code = 'IDENTITY_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3610000-0000-0000-0000-000000000014', 'lite', 'WORK_PERMIT_DOC', 'Work Permit Document', 'Lite employee work permit copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'lite' AND document_category_code = 'COMPLIANCE_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, document_type_code) DO UPDATE
SET document_type_name = EXCLUDED.document_type_name,
    short_description = EXCLUDED.short_description,
    document_for = EXCLUDED.document_for,
    document_category_id = EXCLUDED.document_category_id,
    attachment_required = EXCLUDED.attachment_required,
    issue_date_required = EXCLUDED.issue_date_required,
    expiry_date_required = EXCLUDED.expiry_date_required,
    reference_no_required = EXCLUDED.reference_no_required,
    multiple_allowed = EXCLUDED.multiple_allowed,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.document_applicability_rules (
    id, tenant_id, applicability_rule_code, document_type_id, worker_type_id, employee_category_id,
    nationalisation_category_id, legal_entity_id, job_family_id, designation_id, dependent_type_id,
    mandatory_flag, onboarding_required_flag, description, active, created_by, updated_by
)
VALUES
    (
        'c3610000-0000-0000-0000-000000000021',
        'default',
        'PASSPORT_DEFAULT',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'default' AND document_type_code = 'PASSPORT'),
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        TRUE,
        TRUE,
        'Passport required by default for employee onboarding',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3610000-0000-0000-0000-000000000022',
        'lite',
        'PASSPORT_DEFAULT',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'lite' AND document_type_code = 'PASSPORT'),
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        TRUE,
        TRUE,
        'Passport required by default for lite employee onboarding',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, applicability_rule_code) DO UPDATE
SET document_type_id = EXCLUDED.document_type_id,
    worker_type_id = EXCLUDED.worker_type_id,
    employee_category_id = EXCLUDED.employee_category_id,
    nationalisation_category_id = EXCLUDED.nationalisation_category_id,
    legal_entity_id = EXCLUDED.legal_entity_id,
    job_family_id = EXCLUDED.job_family_id,
    designation_id = EXCLUDED.designation_id,
    dependent_type_id = EXCLUDED.dependent_type_id,
    mandatory_flag = EXCLUDED.mandatory_flag,
    onboarding_required_flag = EXCLUDED.onboarding_required_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.document_expiry_rules (
    id, tenant_id, expiry_rule_code, document_type_id, expiry_tracking_required, renewal_required,
    alert_days_before_json, grace_period_days, block_transaction_on_expiry_flag,
    description, active, created_by, updated_by
)
VALUES
    (
        'c3610000-0000-0000-0000-000000000031',
        'default',
        'PASSPORT_EXPIRY_RULE',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'default' AND document_type_code = 'PASSPORT'),
        TRUE,
        TRUE,
        '[90,60,30,7,1]'::jsonb,
        0,
        TRUE,
        'Passport expiry monitoring rule',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3610000-0000-0000-0000-000000000032',
        'lite',
        'PASSPORT_EXPIRY_RULE',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'lite' AND document_type_code = 'PASSPORT'),
        TRUE,
        TRUE,
        '[60,30,7,1]'::jsonb,
        0,
        TRUE,
        'Lite passport expiry monitoring rule',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, expiry_rule_code) DO UPDATE
SET document_type_id = EXCLUDED.document_type_id,
    expiry_tracking_required = EXCLUDED.expiry_tracking_required,
    renewal_required = EXCLUDED.renewal_required,
    alert_days_before_json = EXCLUDED.alert_days_before_json,
    grace_period_days = EXCLUDED.grace_period_days,
    block_transaction_on_expiry_flag = EXCLUDED.block_transaction_on_expiry_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
