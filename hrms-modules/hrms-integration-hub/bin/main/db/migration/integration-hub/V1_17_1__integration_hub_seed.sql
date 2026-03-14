INSERT INTO integration_hub.integration_definitions(
    id, tenant_id, integration_key, provider_type, display_name, status, created_at, updated_at
)
VALUES (
    'd4950f7a-021d-4423-9201-afde07f70290',
    'default',
    'BIOMETRIC_DEFAULT',
    'BIOMETRIC_DEVICE',
    'Default Biometric Device Connector',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (tenant_id, integration_key) DO NOTHING;

INSERT INTO integration_hub.integration_endpoints(
    id, definition_id, tenant_id, endpoint_key, base_url, auth_type, configuration_json, status, created_at, updated_at
)
VALUES (
    'eb80e95b-bc68-40f8-8459-dd7eaef35d2a',
    'd4950f7a-021d-4423-9201-afde07f70290',
    'default',
    'BIOMETRIC_DEFAULT_ENDPOINT',
    'stub://biometric/default',
    'NONE',
    '{"mode":"stub"}',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (tenant_id, endpoint_key) DO NOTHING;
