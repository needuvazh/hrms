INSERT INTO document.document_records (
    id,
    tenant_id,
    document_type,
    entity_type,
    entity_id,
    file_name,
    storage_provider,
    storage_bucket,
    storage_object_key,
    storage_checksum,
    content_type,
    size_bytes,
    expiry_date,
    verification_status,
    archived,
    created_by,
    updated_by
)
SELECT
    '46666666-6666-6666-6666-666666666666',
    'default',
    'EMPLOYMENT_CONTRACT',
    'EMPLOYEE',
    '41111111-1111-1111-1111-111111111111',
    'employment-contract.pdf',
    'LOCAL_DEV',
    'default',
    'employee/41111111-1111-1111-1111-111111111111/employment-contract.pdf',
    'seed-checksum-contract',
    'application/pdf',
    2048,
    DATE '2028-12-31',
    'VERIFIED',
    FALSE,
    'seed',
    'seed'
WHERE NOT EXISTS (
    SELECT 1
    FROM document.document_records
    WHERE id = '46666666-6666-6666-6666-666666666666'
);
