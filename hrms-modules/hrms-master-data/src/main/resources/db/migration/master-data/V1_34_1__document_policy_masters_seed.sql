INSERT INTO master_data.document_categories (
    id, tenant_id, document_category_code, document_category_name, description, display_order, active, created_by, updated_by
)
VALUES
    ('d3401000-0000-0000-0000-000000000001', 'default', 'IDENTITY_DOCS', 'Identity Documents', 'Identity verification documents', 1, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000002', 'default', 'IMMIGRATION_DOCS', 'Immigration Documents', 'Visa and permit related documents', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000003', 'default', 'EDUCATION_DOCS', 'Education Documents', 'Academic and qualification proofs', 3, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000004', 'default', 'EMPLOYMENT_DOCS', 'Employment Documents', 'Offer, contract, and employment records', 4, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000005', 'default', 'POLICY_DOCS', 'Policy Documents', 'Policy acknowledgement and compliance docs', 5, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
        'd3401000-0000-0000-0000-000000000011', 'default', 'PASSPORT', 'Passport', 'Employee passport copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'IDENTITY_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3401000-0000-0000-0000-000000000012', 'default', 'VISA', 'Visa', 'Work/residence visa document', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'IMMIGRATION_DOCS'),
        TRUE, TRUE, TRUE, TRUE, TRUE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3401000-0000-0000-0000-000000000013', 'default', 'CIVIL_ID', 'Civil ID', 'Civil ID card copy', 'BOTH',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'IDENTITY_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3401000-0000-0000-0000-000000000014', 'default', 'LABOUR_CARD', 'Labour Card', 'Labour card copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'IMMIGRATION_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3401000-0000-0000-0000-000000000015', 'default', 'EDU_CERT', 'Education Certificate', 'Highest qualification certificate', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'EDUCATION_DOCS'),
        TRUE, FALSE, FALSE, FALSE, TRUE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3401000-0000-0000-0000-000000000016', 'default', 'MED_CERT', 'Medical Certificate', 'Pre-employment medical certificate', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'EMPLOYMENT_DOCS'),
        TRUE, TRUE, FALSE, FALSE, TRUE,
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
        'd3401000-0000-0000-0000-000000000021', 'default', 'PASSPORT_EXPAT_EMP',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'default' AND document_type_code = 'PASSPORT'),
        NULL,
        NULL,
        (SELECT id FROM master_data.nationalisation_categories WHERE tenant_id = 'default' AND nationalisation_category_code = 'EXPAT'),
        NULL,
        NULL,
        NULL,
        NULL,
        TRUE,
        TRUE,
        'Passport mandatory for expatriate employees during onboarding',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'd3401000-0000-0000-0000-000000000022', 'default', 'CIVIL_ID_OMANI',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'default' AND document_type_code = 'CIVIL_ID'),
        NULL,
        NULL,
        (SELECT id FROM master_data.nationalisation_categories WHERE tenant_id = 'default' AND nationalisation_category_code = 'OMANI'),
        NULL,
        NULL,
        NULL,
        NULL,
        TRUE,
        TRUE,
        'Civil ID mandatory for Omani employee records',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'd3401000-0000-0000-0000-000000000023', 'default', 'VISA_DEPENDENT_DEFAULT',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'default' AND document_type_code = 'VISA'),
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        (SELECT id FROM master_data.dependent_types WHERE tenant_id = 'default' AND dependent_type_code = 'SPOUSE_DEP'),
        TRUE,
        FALSE,
        'Visa copy required for sponsored spouse dependents',
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
        'd3401000-0000-0000-0000-000000000031', 'default', 'PASSPORT_EXPIRY_RULE',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'default' AND document_type_code = 'PASSPORT'),
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
        'd3401000-0000-0000-0000-000000000032', 'default', 'VISA_EXPIRY_RULE',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'default' AND document_type_code = 'VISA'),
        TRUE,
        TRUE,
        '[60,30,7]'::jsonb,
        0,
        TRUE,
        'Visa expiry alerts and blocking rule',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'd3401000-0000-0000-0000-000000000033', 'default', 'EDU_CERT_NO_EXPIRY',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'default' AND document_type_code = 'EDU_CERT'),
        FALSE,
        FALSE,
        '[]'::jsonb,
        0,
        FALSE,
        'Education certificates do not require expiry tracking',
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
    ('d3401000-0000-0000-0000-000000000041', 'default', 'EMP_HANDBOOK', 'Employee Handbook', 'Core employee handbook policy', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000042', 'default', 'CODE_OF_CONDUCT', 'Code of Conduct', 'Employee code of conduct policy', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000043', 'default', 'INFO_SECURITY', 'Information Security Policy', 'Information security and acceptable use', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3401000-0000-0000-0000-000000000051', 'default', 'READ_ACCEPT', 'Read and Accept', FALSE, TRUE, FALSE, 'Read and accept acknowledgement flow', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000052', 'default', 'CHECKBOX_CONFIRM', 'Checkbox Confirmation', FALSE, FALSE, FALSE, 'Simple checkbox acknowledgement', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000053', 'default', 'E_SIGNATURE', 'E-Signature Required', TRUE, TRUE, FALSE, 'Electronic signature required for acknowledgement', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3401000-0000-0000-0000-000000000061', 'default', 'IDENTITY_SCAN', 'Identity Scan', 'IMAGE', 10, 'Identity card and passport scans', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000062', 'default', 'SIGNED_POLICY_PDF', 'Signed Policy PDF', 'PDF', 15, 'Signed policy PDF attachments', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000063', 'default', 'CERTIFICATE_COPY', 'Certificate Copy', 'PDF', 20, 'Education/certification attachments', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000064', 'default', 'SUPPORTING_DOC', 'Supporting Document', 'OFFICE_DOC', 25, 'General supporting documents', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, attachment_category_code) DO UPDATE
SET attachment_category_name = EXCLUDED.attachment_category_name,
    mime_group = EXCLUDED.mime_group,
    max_file_size_mb = EXCLUDED.max_file_size_mb,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.document_categories (
    id, tenant_id, document_category_code, document_category_name, description, display_order, active, created_by, updated_by
)
VALUES
    ('d3401000-0000-0000-0000-000000000101', 'lite', 'IDENTITY_DOCS', 'Identity Documents', 'Identity verification documents', 1, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3401000-0000-0000-0000-000000000102', 'lite', 'IMMIGRATION_DOCS', 'Immigration Documents', 'Visa and permit related documents', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
        'd3401000-0000-0000-0000-000000000111', 'lite', 'PASSPORT', 'Passport', 'Employee passport copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'lite' AND document_category_code = 'IDENTITY_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'd3401000-0000-0000-0000-000000000112', 'lite', 'VISA', 'Visa', 'Work/residence visa document', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'lite' AND document_category_code = 'IMMIGRATION_DOCS'),
        TRUE, TRUE, TRUE, TRUE, TRUE,
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
        'd3401000-0000-0000-0000-000000000121', 'lite', 'PASSPORT_DEFAULT',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'lite' AND document_type_code = 'PASSPORT'),
        NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        TRUE, TRUE,
        'Passport mandatory default rule for lite tenant',
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
        'd3401000-0000-0000-0000-000000000131', 'lite', 'PASSPORT_EXPIRY_RULE',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'lite' AND document_type_code = 'PASSPORT'),
        TRUE,
        TRUE,
        '[90,30,7]'::jsonb,
        0,
        TRUE,
        'Lite passport expiry alerts and blocking rule',
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
    ('d3401000-0000-0000-0000-000000000141', 'lite', 'EMP_HANDBOOK', 'Employee Handbook', 'Core employee handbook policy', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3401000-0000-0000-0000-000000000151', 'lite', 'READ_ACCEPT', 'Read and Accept', FALSE, TRUE, FALSE, 'Read and accept acknowledgement flow', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3401000-0000-0000-0000-000000000161', 'lite', 'IDENTITY_SCAN', 'Identity Scan', 'IMAGE', 10, 'Identity card and passport scans', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, attachment_category_code) DO UPDATE
SET attachment_category_name = EXCLUDED.attachment_category_name,
    mime_group = EXCLUDED.mime_group,
    max_file_size_mb = EXCLUDED.max_file_size_mb,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
