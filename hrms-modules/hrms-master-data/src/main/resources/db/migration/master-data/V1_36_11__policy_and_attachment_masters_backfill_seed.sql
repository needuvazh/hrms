INSERT INTO master_data.policy_document_types (
    id, tenant_id, policy_document_type_code, policy_document_type_name, description, version_required_flag, active, created_by, updated_by
)
VALUES
    ('c3611000-0000-0000-0000-000000000001', 'default', 'EMP_HANDBOOK', 'Employee Handbook', 'Core employee handbook policy', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3611000-0000-0000-0000-000000000002', 'default', 'CODE_OF_CONDUCT', 'Code of Conduct', 'Employee code of conduct policy', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3611000-0000-0000-0000-000000000003', 'lite', 'EMP_HANDBOOK', 'Employee Handbook', 'Core employee handbook policy', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('c3611000-0000-0000-0000-000000000011', 'default', 'READ_ACCEPT', 'Read and Accept', FALSE, TRUE, FALSE, 'Read and accept acknowledgement flow', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3611000-0000-0000-0000-000000000012', 'default', 'E_SIGNATURE', 'E-Signature Required', TRUE, TRUE, FALSE, 'Electronic signature required for acknowledgement', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3611000-0000-0000-0000-000000000013', 'lite', 'READ_ACCEPT', 'Read and Accept', FALSE, TRUE, FALSE, 'Read and accept acknowledgement flow', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('c3611000-0000-0000-0000-000000000021', 'default', 'IDENTITY_SCAN', 'Identity Scan', 'IMAGE', 10, 'Identity card and passport scans', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3611000-0000-0000-0000-000000000022', 'default', 'COMPLIANCE_PDF', 'Compliance PDF', 'PDF', 20, 'Compliance document attachments', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3611000-0000-0000-0000-000000000023', 'lite', 'IDENTITY_SCAN', 'Identity Scan', 'IMAGE', 10, 'Identity card and passport scans', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, attachment_category_code) DO UPDATE
SET attachment_category_name = EXCLUDED.attachment_category_name,
    mime_group = EXCLUDED.mime_group,
    max_file_size_mb = EXCLUDED.max_file_size_mb,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
