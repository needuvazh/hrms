INSERT INTO master_data.document_categories (
    id, tenant_id, document_category_code, document_category_name, description, display_order, active, created_by, updated_by
)
VALUES
    ('c3403000-0000-0000-0000-000000000001', 'default', 'COMPLIANCE_DOCS', 'Compliance Documents', 'Statutory and compliance-related employee documents', 6, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3403000-0000-0000-0000-000000000002', 'lite', 'COMPLIANCE_DOCS', 'Compliance Documents', 'Lite statutory and compliance documents', 5, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
        'c3403000-0000-0000-0000-000000000011', 'default', 'WORK_PERMIT_DOC', 'Work Permit Document', 'Employee work permit copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'COMPLIANCE_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3403000-0000-0000-0000-000000000012', 'default', 'SOCIAL_INSURANCE_DOC', 'Social Insurance Registration', 'PASI/social insurance registration document', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'default' AND document_category_code = 'COMPLIANCE_DOCS'),
        TRUE, TRUE, FALSE, TRUE, FALSE,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3403000-0000-0000-0000-000000000013', 'lite', 'WORK_PERMIT_DOC', 'Work Permit Document', 'Lite employee work permit copy', 'EMPLOYEE',
        (SELECT id FROM master_data.document_categories WHERE tenant_id = 'lite' AND document_category_code = 'COMPLIANCE_DOCS'),
        TRUE, TRUE, TRUE, TRUE, FALSE,
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

INSERT INTO master_data.document_expiry_rules (
    id, tenant_id, expiry_rule_code, document_type_id, expiry_tracking_required, renewal_required,
    alert_days_before_json, grace_period_days, block_transaction_on_expiry_flag,
    description, active, created_by, updated_by
)
VALUES
    (
        'c3403000-0000-0000-0000-000000000021', 'default', 'WORK_PERMIT_EXPIRY_RULE',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'default' AND document_type_code = 'WORK_PERMIT_DOC'),
        TRUE, TRUE, '[60,30,7]'::jsonb, 0, TRUE,
        'Work permit expiry monitoring rule', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3403000-0000-0000-0000-000000000022', 'lite', 'WORK_PERMIT_EXPIRY_RULE',
        (SELECT id FROM master_data.document_types WHERE tenant_id = 'lite' AND document_type_code = 'WORK_PERMIT_DOC'),
        TRUE, TRUE, '[30,7,1]'::jsonb, 0, TRUE,
        'Lite work permit expiry monitoring rule', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
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

INSERT INTO master_data.attachment_categories (
    id, tenant_id, attachment_category_code, attachment_category_name, mime_group, max_file_size_mb, description, active, created_by, updated_by
)
VALUES
    ('c3403000-0000-0000-0000-000000000031', 'default', 'COMPLIANCE_PDF', 'Compliance PDF', 'PDF', 20, 'Compliance document attachments', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3403000-0000-0000-0000-000000000032', 'lite', 'COMPLIANCE_PDF', 'Compliance PDF', 'PDF', 15, 'Lite compliance document attachments', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, attachment_category_code) DO UPDATE
SET attachment_category_name = EXCLUDED.attachment_category_name,
    mime_group = EXCLUDED.mime_group,
    max_file_size_mb = EXCLUDED.max_file_size_mb,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
