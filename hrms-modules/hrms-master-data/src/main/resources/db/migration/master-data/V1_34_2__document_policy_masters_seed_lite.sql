INSERT INTO master_data.document_categories (
    id, tenant_id, document_category_code, document_category_name, description, display_order, active, created_by, updated_by
)
VALUES
    ('d3402000-0000-0000-0000-000000000001', 'lite', 'IDENTITY_DOCS', 'Identity Documents', 'Identity verification documents', 1, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3402000-0000-0000-0000-000000000002', 'lite', 'IMMIGRATION_DOCS', 'Immigration Documents', 'Visa and permit related documents', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3402000-0000-0000-0000-000000000003', 'lite', 'EMPLOYMENT_DOCS', 'Employment Documents', 'Offer, contract, and employment records', 3, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3402000-0000-0000-0000-000000000004', 'lite', 'POLICY_DOCS', 'Policy Documents', 'Policy acknowledgement and compliance docs', 4, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
        'd3402000-0000-0000-0000-000000000011', 'lite', 'PASSPORT', 'Passport', 'Employee passport copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'lite' AND document_category_code = 'IDENTITY_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3402000-0000-0000-0000-000000000012', 'lite', 'VISA', 'Visa', 'Work/residence visa document', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'lite' AND document_category_code = 'IMMIGRATION_DOCS'),
        TRUE, TRUE, TRUE, TRUE, TRUE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3402000-0000-0000-0000-000000000013', 'lite', 'OFFER_LETTER', 'Offer Letter', 'Signed offer letter', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'lite' AND document_category_code = 'EMPLOYMENT_DOCS'),
        TRUE, TRUE, FALSE, FALSE, FALSE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3402000-0000-0000-0000-000000000014', 'lite', 'NDA', 'NDA', 'Signed non-disclosure agreement', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'lite' AND document_category_code = 'POLICY_DOCS'),
        TRUE, TRUE, FALSE, FALSE, FALSE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
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
        'd3402000-0000-0000-0000-000000000021', 'lite', 'PASSPORT_DEFAULT',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'lite' AND document_type_code = 'PASSPORT'),
        NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        TRUE, TRUE,
        'Passport required for default onboarding in lite tenant',
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3402000-0000-0000-0000-000000000022', 'lite', 'OFFER_LETTER_DEFAULT',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'lite' AND document_type_code = 'OFFER_LETTER'),
        NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        TRUE, TRUE,
        'Offer letter is mandatory during onboarding',
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
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
        'd3402000-0000-0000-0000-000000000031', 'lite', 'PASSPORT_EXPIRY_RULE',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'lite' AND document_type_code = 'PASSPORT'),
        TRUE,
        TRUE,
        '[90,60,30,7,1]'::jsonb,
        0,
        TRUE,
        'Passport expiry alerts and blocking rule',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'd3402000-0000-0000-0000-000000000032', 'lite', 'VISA_EXPIRY_RULE',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'lite' AND document_type_code = 'VISA'),
        TRUE,
        TRUE,
        '[60,30,7]'::jsonb,
        0,
        TRUE,
        'Visa expiry alerts and blocking rule',
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

INSERT INTO master_data.policy_document_types (
    id, tenant_id, policy_document_type_code, policy_document_type_name, description, version_required_flag, active, created_by, updated_by
)
VALUES
    ('d3402000-0000-0000-0000-000000000041', 'lite', 'EMP_HANDBOOK', 'Employee Handbook', 'Core employee handbook policy', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3402000-0000-0000-0000-000000000042', 'lite', 'CODE_OF_CONDUCT', 'Code of Conduct', 'Employee code of conduct policy', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, policy_document_type_code) DO UPDATE
SET policy_document_type_name = EXCLUDED.policy_document_type_name,
    description = EXCLUDED.description,
    version_required_flag = EXCLUDED.version_required_flag,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.policy_acknowledgement_types (
    id, tenant_id, policy_ack_type_code, policy_ack_type_name, e_signature_required_flag,
    reack_on_version_change_flag, annual_reack_flag, description, active, created_by, updated_by
)
VALUES
    ('d3402000-0000-0000-0000-000000000051', 'lite', 'READ_ACCEPT', 'Read and Accept', FALSE, TRUE, FALSE, 'Read and accept acknowledgement flow', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3402000-0000-0000-0000-000000000052', 'lite', 'CHECKBOX_CONFIRM', 'Checkbox Confirmation', FALSE, FALSE, FALSE, 'Simple checkbox acknowledgement', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, policy_ack_type_code) DO UPDATE
SET policy_ack_type_name = EXCLUDED.policy_ack_type_name,
    e_signature_required_flag = EXCLUDED.e_signature_required_flag,
    reack_on_version_change_flag = EXCLUDED.reack_on_version_change_flag,
    annual_reack_flag = EXCLUDED.annual_reack_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.attachment_categories (
    id, tenant_id, attachment_category_code, attachment_category_name, mime_group, max_file_size_mb, description, active, created_by, updated_by
)
VALUES
    ('d3402000-0000-0000-0000-000000000061', 'lite', 'IDENTITY_SCAN', 'Identity Scan', 'IMAGE', 10, 'Identity card and passport scans', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3402000-0000-0000-0000-000000000062', 'lite', 'SIGNED_POLICY_PDF', 'Signed Policy PDF', 'PDF', 15, 'Signed policy PDF attachments', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3402000-0000-0000-0000-000000000063', 'lite', 'SUPPORTING_DOC', 'Supporting Document', 'OFFICE_DOC', 20, 'General supporting documents', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, attachment_category_code) DO UPDATE
SET attachment_category_name = EXCLUDED.attachment_category_name,
    mime_group = EXCLUDED.mime_group,
    max_file_size_mb = EXCLUDED.max_file_size_mb,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
