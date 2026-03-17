INSERT INTO master_data.visa_types (
    id, tenant_id, visa_type_code, visa_type_name, visa_category, applies_to, renewable_flag, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000001', 'lite', 'EMPLOYMENT_VISA', 'Employment Visa', 'WORK', 'EMPLOYEE', TRUE, 'Lite standard employee work visa', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3302000-0000-0000-0000-000000000002', 'lite', 'FAMILY_VISA', 'Family Visa', 'FAMILY', 'DEPENDENT', TRUE, 'Lite dependent sponsorship visa', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, visa_type_code) DO UPDATE
SET visa_type_name = EXCLUDED.visa_type_name,
    visa_category = EXCLUDED.visa_category,
    applies_to = EXCLUDED.applies_to,
    renewable_flag = EXCLUDED.renewable_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.residence_statuses (
    id, tenant_id, residence_status_code, residence_status_name, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000011', 'lite', 'OMANI_CITIZEN', 'Omani Citizen', 'Lite citizen residence status', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3302000-0000-0000-0000-000000000012', 'lite', 'EXPAT_RESIDENT', 'Expatriate Resident', 'Lite resident permit holder', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, residence_status_code) DO UPDATE
SET residence_status_name = EXCLUDED.residence_status_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.labour_card_types (
    id, tenant_id, labour_card_type_code, labour_card_type_name, expiry_tracking_required, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000021', 'lite', 'PRIVATE_SECTOR', 'Private Sector Labour Card', TRUE, 'Lite private-sector labour card', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, labour_card_type_code) DO UPDATE
SET labour_card_type_name = EXCLUDED.labour_card_type_name,
    expiry_tracking_required = EXCLUDED.expiry_tracking_required,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.civil_id_types (
    id, tenant_id, civil_id_type_code, civil_id_type_name, applies_to, expiry_tracking_required, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000031', 'lite', 'NATIONAL_ID', 'National Civil ID', 'OMANI', TRUE, 'Lite Omani civil ID', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3302000-0000-0000-0000-000000000032', 'lite', 'RESIDENT_ID', 'Resident Card', 'EXPATRIATE', TRUE, 'Lite expatriate resident ID', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, civil_id_type_code) DO UPDATE
SET civil_id_type_name = EXCLUDED.civil_id_type_name,
    applies_to = EXCLUDED.applies_to,
    expiry_tracking_required = EXCLUDED.expiry_tracking_required,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.passport_types (
    id, tenant_id, passport_type_code, passport_type_name, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000041', 'lite', 'ORDINARY', 'Ordinary Passport', 'Lite standard travel passport', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, passport_type_code) DO UPDATE
SET passport_type_name = EXCLUDED.passport_type_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.sponsor_types (
    id, tenant_id, sponsor_type_code, sponsor_type_name, applies_to, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000051', 'lite', 'COMPANY_SPONSOR', 'Company Sponsored', 'EMPLOYEE', 'Lite company-sponsored employee', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3302000-0000-0000-0000-000000000052', 'lite', 'FAMILY_SPONSOR', 'Family Sponsored', 'DEPENDENT', 'Lite family-sponsored dependent', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, sponsor_type_code) DO UPDATE
SET sponsor_type_name = EXCLUDED.sponsor_type_name,
    applies_to = EXCLUDED.applies_to,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.work_permit_types (
    id, tenant_id, work_permit_type_code, work_permit_type_name, renewable_flag, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000061', 'lite', 'STANDARD_PERMIT', 'Standard Work Permit', TRUE, 'Lite standard work permit', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, work_permit_type_code) DO UPDATE
SET work_permit_type_name = EXCLUDED.work_permit_type_name,
    renewable_flag = EXCLUDED.renewable_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.nationalisation_categories (
    id, tenant_id, nationalisation_category_code, nationalisation_category_name, omani_flag, counts_for_omanisation_flag, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000071', 'lite', 'OMANI', 'Omani National', TRUE, TRUE, 'Lite Omanisation eligible category', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3302000-0000-0000-0000-000000000072', 'lite', 'EXPAT', 'Expatriate', FALSE, FALSE, 'Lite expatriate category', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, nationalisation_category_code) DO UPDATE
SET nationalisation_category_name = EXCLUDED.nationalisation_category_name,
    omani_flag = EXCLUDED.omani_flag,
    counts_for_omanisation_flag = EXCLUDED.counts_for_omanisation_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.social_insurance_eligibility_types (
    id, tenant_id, social_insurance_type_code, social_insurance_type_name, pension_eligible_flag, occupational_hazard_eligible_flag, govt_contribution_applicable_flag, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000081', 'lite', 'PASI_OMANI', 'PASI Eligible Omani', TRUE, TRUE, TRUE, 'Lite PASI-eligible Omani profile', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3302000-0000-0000-0000-000000000082', 'lite', 'NOT_ELIGIBLE', 'Not Eligible', FALSE, FALSE, FALSE, 'Lite not eligible insurance profile', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, social_insurance_type_code) DO UPDATE
SET social_insurance_type_name = EXCLUDED.social_insurance_type_name,
    pension_eligible_flag = EXCLUDED.pension_eligible_flag,
    occupational_hazard_eligible_flag = EXCLUDED.occupational_hazard_eligible_flag,
    govt_contribution_applicable_flag = EXCLUDED.govt_contribution_applicable_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.beneficiary_types (
    id, tenant_id, beneficiary_type_code, beneficiary_type_name, priority_order, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000091', 'lite', 'SPOUSE', 'Spouse Beneficiary', 1, 'Lite spouse beneficiary', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3302000-0000-0000-0000-000000000092', 'lite', 'CHILD', 'Child Beneficiary', 2, 'Lite child beneficiary', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, beneficiary_type_code) DO UPDATE
SET beneficiary_type_name = EXCLUDED.beneficiary_type_name,
    priority_order = EXCLUDED.priority_order,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.dependent_types (
    id, tenant_id, dependent_type_code, dependent_type_name, insurance_eligible_flag, family_visa_eligible_flag, description, active, created_by, updated_by
)
VALUES
    ('c3302000-0000-0000-0000-000000000101', 'lite', 'SPOUSE_DEP', 'Spouse', TRUE, TRUE, 'Lite spouse dependent type', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3302000-0000-0000-0000-000000000102', 'lite', 'CHILD_DEP', 'Child', TRUE, TRUE, 'Lite child dependent type', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, dependent_type_code) DO UPDATE
SET dependent_type_name = EXCLUDED.dependent_type_name,
    insurance_eligible_flag = EXCLUDED.insurance_eligible_flag,
    family_visa_eligible_flag = EXCLUDED.family_visa_eligible_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
