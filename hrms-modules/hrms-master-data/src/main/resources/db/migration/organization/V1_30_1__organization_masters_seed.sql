INSERT INTO organization.legal_entities (
    id, tenant_id, legal_entity_code, legal_entity_name, short_name, registration_no, tax_no,
    country_code, base_currency_code, default_language_code, contact_email, contact_phone,
    address_line1, city, state, postal_code, active, created_by, updated_by
)
VALUES
    ('c3001000-0000-0000-0000-000000000001', 'default', 'DEFAULT_HQ', 'Default Holdings LLC', 'Default HQ', 'REG-DEF-001', 'TAX-DEF-001', 'OM', 'OMR', 'en', 'hq@default.example.com', '+96824000001', 'Muscat Business District', 'Muscat', 'Muscat', '100', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3001000-0000-0000-0000-000000000002', 'lite', 'LITE_HQ', 'Lite Services SPC', 'Lite HQ', 'REG-LITE-001', 'TAX-LITE-001', 'OM', 'OMR', 'en', 'hq@lite.example.com', '+96824000002', 'Seeb Commercial Area', 'Seeb', 'Muscat', '130', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, legal_entity_code) DO UPDATE
SET legal_entity_name = EXCLUDED.legal_entity_name,
    short_name = EXCLUDED.short_name,
    registration_no = EXCLUDED.registration_no,
    tax_no = EXCLUDED.tax_no,
    country_code = EXCLUDED.country_code,
    base_currency_code = EXCLUDED.base_currency_code,
    default_language_code = EXCLUDED.default_language_code,
    contact_email = EXCLUDED.contact_email,
    contact_phone = EXCLUDED.contact_phone,
    address_line1 = EXCLUDED.address_line1,
    city = EXCLUDED.city,
    state = EXCLUDED.state,
    postal_code = EXCLUDED.postal_code,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization.branches (
    id, tenant_id, legal_entity_id, branch_code, branch_name, branch_short_name,
    address_line1, city, state, country_code, postal_code, phone, email,
    active, created_by, updated_by
)
VALUES
    (
        'c3001000-0000-0000-0000-000000000011', 'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        'MCT_MAIN', 'Muscat Main Branch', 'Muscat Main',
        'Al Khuwair', 'Muscat', 'Muscat', 'OM', '112', '+96824001001', 'muscat.branch@default.example.com',
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000012', 'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        'SEEB_MAIN', 'Seeb Main Branch', 'Seeb Main',
        'Al Hail', 'Seeb', 'Muscat', 'OM', '132', '+96824001002', 'seeb.branch@lite.example.com',
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, branch_code) DO UPDATE
SET legal_entity_id = EXCLUDED.legal_entity_id,
    branch_name = EXCLUDED.branch_name,
    branch_short_name = EXCLUDED.branch_short_name,
    address_line1 = EXCLUDED.address_line1,
    city = EXCLUDED.city,
    state = EXCLUDED.state,
    country_code = EXCLUDED.country_code,
    postal_code = EXCLUDED.postal_code,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization.business_units (
    id, tenant_id, legal_entity_id, business_unit_code, business_unit_name, description,
    active, created_by, updated_by
)
VALUES
    ('c3001000-0000-0000-0000-000000000021', 'default', (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'), 'CORP', 'Corporate Services', 'Corporate shared services unit', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3001000-0000-0000-0000-000000000022', 'lite', (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'), 'CORE', 'Core Operations', 'Lite tenant core operations unit', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, business_unit_code) DO UPDATE
SET legal_entity_id = EXCLUDED.legal_entity_id,
    business_unit_name = EXCLUDED.business_unit_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization.divisions (
    id, tenant_id, legal_entity_id, business_unit_id, branch_id, division_code, division_name, description,
    active, created_by, updated_by
)
VALUES
    (
        'c3001000-0000-0000-0000-000000000031', 'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'default' AND business_unit_code = 'CORP'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'default' AND branch_code = 'MCT_MAIN'),
        'PEOPLE', 'People & Culture', 'HR and people operations division',
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000032', 'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'lite' AND business_unit_code = 'CORE'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'lite' AND branch_code = 'SEEB_MAIN'),
        'HR', 'Human Resources', 'Lite tenant HR division',
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, division_code) DO UPDATE
SET legal_entity_id = EXCLUDED.legal_entity_id,
    business_unit_id = EXCLUDED.business_unit_id,
    branch_id = EXCLUDED.branch_id,
    division_name = EXCLUDED.division_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization.departments (
    id, tenant_id, legal_entity_id, business_unit_id, division_id, branch_id,
    department_code, department_name, short_name, description, active, created_by, updated_by
)
VALUES
    (
        'c3001000-0000-0000-0000-000000000041', 'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'default' AND business_unit_code = 'CORP'),
        (SELECT id FROM organization.divisions WHERE tenant_id = 'default' AND division_code = 'PEOPLE'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'default' AND branch_code = 'MCT_MAIN'),
        'HR_OPS', 'HR Operations', 'HR Ops', 'Core HR operations department', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000042', 'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'lite' AND business_unit_code = 'CORE'),
        (SELECT id FROM organization.divisions WHERE tenant_id = 'lite' AND division_code = 'HR'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'lite' AND branch_code = 'SEEB_MAIN'),
        'HR', 'HR Department', 'HR', 'Lite HR department', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, department_code) DO UPDATE
SET legal_entity_id = EXCLUDED.legal_entity_id,
    business_unit_id = EXCLUDED.business_unit_id,
    division_id = EXCLUDED.division_id,
    branch_id = EXCLUDED.branch_id,
    department_name = EXCLUDED.department_name,
    short_name = EXCLUDED.short_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization.sections (
    id, tenant_id, department_id, section_code, section_name, description, active, created_by, updated_by
)
VALUES
    ('c3001000-0000-0000-0000-000000000051', 'default', (SELECT id FROM organization.departments WHERE tenant_id = 'default' AND department_code = 'HR_OPS'), 'PAYROLL_SEC', 'Payroll Section', 'Payroll operations section', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3001000-0000-0000-0000-000000000052', 'lite', (SELECT id FROM organization.departments WHERE tenant_id = 'lite' AND department_code = 'HR'), 'GEN_SEC', 'General Section', 'General HR section', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, section_code) DO UPDATE
SET department_id = EXCLUDED.department_id,
    section_name = EXCLUDED.section_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO organization.work_locations (
    id, tenant_id, legal_entity_id, branch_id, location_code, location_name, location_type,
    address_line1, city, state, country_code, postal_code, latitude, longitude, geofence_radius,
    active, created_by, updated_by
)
VALUES
    (
        'c3001000-0000-0000-0000-000000000061', 'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'default' AND branch_code = 'MCT_MAIN'),
        'MCT_HQ', 'Muscat HQ', 'OFFICE',
        'Al Khuwair, Building 10', 'Muscat', 'Muscat', 'OM', '112', 23.5880000, 58.3829000, 200.00,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000062', 'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'lite' AND branch_code = 'SEEB_MAIN'),
        'SEEB_HQ', 'Seeb HQ', 'OFFICE',
        'Al Hail, Building 5', 'Seeb', 'Muscat', 'OM', '132', 23.6700000, 58.2100000, 150.00,
        TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'
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
    ('c3001000-0000-0000-0000-000000000071', 'default', (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'), 'CC_HR', 'HR Cost Center', 'Default HR cost center', '5001', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3001000-0000-0000-0000-000000000072', 'lite', (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'), 'CC_CORE', 'Core Cost Center', 'Lite core cost center', '5101', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('c3001000-0000-0000-0000-000000000081', 'default', 'RU_CORP', 'Corporate Reporting', 'Corporate reporting line', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3001000-0000-0000-0000-000000000082', 'lite', 'RU_CORE', 'Core Reporting', 'Lite reporting line', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, reporting_unit_code) DO UPDATE
SET reporting_unit_name = EXCLUDED.reporting_unit_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
