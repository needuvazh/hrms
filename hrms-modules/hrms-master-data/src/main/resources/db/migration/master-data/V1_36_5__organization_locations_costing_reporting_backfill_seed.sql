INSERT INTO organization.work_locations (
    id, tenant_id, legal_entity_id, branch_id, location_code, location_name, location_type,
    address_line1, city, state, country_code, postal_code, latitude, longitude, geofence_radius,
    active, created_by, updated_by
)
VALUES
    (
        'c3001000-0000-0000-0000-000000000061',
        'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'default' AND branch_code = 'MCT_MAIN'),
        'MCT_HQ',
        'Muscat HQ',
        'OFFICE',
        'Al Khuwair, Building 10',
        'Muscat',
        'Muscat',
        'OM',
        '112',
        23.5880000,
        58.3829000,
        200.00,
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000062',
        'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'lite' AND branch_code = 'SEEB_MAIN'),
        'SEEB_HQ',
        'Seeb HQ',
        'OFFICE',
        'Al Hail, Building 5',
        'Seeb',
        'Muscat',
        'OM',
        '132',
        23.6700000,
        58.2100000,
        150.00,
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, location_code) DO UPDATE
SET legal_entity_id = EXCLUDED.legal_entity_id,
    branch_id = EXCLUDED.branch_id,
    location_name = EXCLUDED.location_name,
    location_type = EXCLUDED.location_type,
    address_line1 = EXCLUDED.address_line1,
    city = EXCLUDED.city,
    state = EXCLUDED.state,
    country_code = EXCLUDED.country_code,
    postal_code = EXCLUDED.postal_code,
    latitude = EXCLUDED.latitude,
    longitude = EXCLUDED.longitude,
    geofence_radius = EXCLUDED.geofence_radius,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization.cost_centers (
    id, tenant_id, legal_entity_id, cost_center_code, cost_center_name, description, gl_account_code,
    active, created_by, updated_by
)
VALUES
    (
        'c3001000-0000-0000-0000-000000000071',
        'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        'CC_HR',
        'HR Cost Center',
        'Default HR cost center',
        '5001',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000072',
        'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        'CC_CORE',
        'Core Cost Center',
        'Lite core cost center',
        '5101',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, cost_center_code) DO UPDATE
SET legal_entity_id = EXCLUDED.legal_entity_id,
    cost_center_name = EXCLUDED.cost_center_name,
    description = EXCLUDED.description,
    gl_account_code = EXCLUDED.gl_account_code,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization.reporting_units (
    id, tenant_id, reporting_unit_code, reporting_unit_name, description, active, created_by, updated_by
)
VALUES
    (
        'c3001000-0000-0000-0000-000000000081',
        'default',
        'RU_CORP',
        'Corporate Reporting',
        'Corporate reporting line',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000082',
        'lite',
        'RU_CORE',
        'Core Reporting',
        'Lite reporting line',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, reporting_unit_code) DO UPDATE
SET reporting_unit_name = EXCLUDED.reporting_unit_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
