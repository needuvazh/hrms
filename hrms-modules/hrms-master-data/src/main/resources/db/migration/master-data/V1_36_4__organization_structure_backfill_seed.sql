INSERT INTO organization.branches (
    id, tenant_id, legal_entity_id, branch_code, branch_name, branch_short_name,
    address_line1, city, state, country_code, postal_code, phone, email,
    active, created_by, updated_by
)
VALUES
    (
        'c3001000-0000-0000-0000-000000000011',
        'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        'MCT_MAIN',
        'Muscat Main Branch',
        'Muscat Main',
        'Al Khuwair',
        'Muscat',
        'Muscat',
        'OM',
        '112',
        '+96824001001',
        'muscat.branch@default.example.com',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000012',
        'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        'SEEB_MAIN',
        'Seeb Main Branch',
        'Seeb Main',
        'Al Hail',
        'Seeb',
        'Muscat',
        'OM',
        '132',
        '+96824001002',
        'seeb.branch@lite.example.com',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
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
    (
        'c3001000-0000-0000-0000-000000000021',
        'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        'CORP',
        'Corporate Services',
        'Corporate shared services unit',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000022',
        'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        'CORE',
        'Core Operations',
        'Lite tenant core operations unit',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
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
        'c3001000-0000-0000-0000-000000000031',
        'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'default' AND business_unit_code = 'CORP'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'default' AND branch_code = 'MCT_MAIN'),
        'PEOPLE',
        'People & Culture',
        'HR and people operations division',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000032',
        'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'lite' AND business_unit_code = 'CORE'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'lite' AND branch_code = 'SEEB_MAIN'),
        'HR',
        'Human Resources',
        'Lite tenant HR division',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
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
        'c3001000-0000-0000-0000-000000000041',
        'default',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'default' AND legal_entity_code = 'DEFAULT_HQ'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'default' AND business_unit_code = 'CORP'),
        (SELECT id FROM organization.divisions WHERE tenant_id = 'default' AND division_code = 'PEOPLE'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'default' AND branch_code = 'MCT_MAIN'),
        'HR_OPS',
        'HR Operations',
        'HR Ops',
        'Core HR operations department',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000042',
        'lite',
        (SELECT id FROM organization.legal_entities WHERE tenant_id = 'lite' AND legal_entity_code = 'LITE_HQ'),
        (SELECT id FROM organization.business_units WHERE tenant_id = 'lite' AND business_unit_code = 'CORE'),
        (SELECT id FROM organization.divisions WHERE tenant_id = 'lite' AND division_code = 'HR'),
        (SELECT id FROM organization.branches WHERE tenant_id = 'lite' AND branch_code = 'SEEB_MAIN'),
        'HR',
        'HR Department',
        'HR',
        'Lite HR department',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
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
    (
        'c3001000-0000-0000-0000-000000000051',
        'default',
        (SELECT id FROM organization.departments WHERE tenant_id = 'default' AND department_code = 'HR_OPS'),
        'PAYROLL_SEC',
        'Payroll Section',
        'Payroll operations section',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    ),
    (
        'c3001000-0000-0000-0000-000000000052',
        'lite',
        (SELECT id FROM organization.departments WHERE tenant_id = 'lite' AND department_code = 'HR'),
        'GEN_SEC',
        'General Section',
        'General HR section',
        TRUE,
        'SYSTEM_SEED',
        'SYSTEM_SEED'
    )
ON CONFLICT (tenant_id, section_code) DO UPDATE
SET department_id = EXCLUDED.department_id,
    section_name = EXCLUDED.section_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
