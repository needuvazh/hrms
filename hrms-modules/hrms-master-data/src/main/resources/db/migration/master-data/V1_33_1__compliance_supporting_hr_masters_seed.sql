INSERT INTO master_data.visa_types (
    id, tenant_id, visa_type_code, visa_type_name, visa_category, applies_to, renewable_flag, description, active, created_by, updated_by
)
VALUES
    ('d3301000-0000-0000-0000-000000000001', 'default', 'EMPLOYMENT_VISA', 'Employment Visa', 'WORK', 'EMPLOYEE', TRUE, 'Standard employee work visa', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000002', 'default', 'FAMILY_VISA', 'Family Visa', 'FAMILY', 'DEPENDENT', TRUE, 'Dependent family sponsorship visa', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000003', 'default', 'VISIT_VISA', 'Visit Visa', 'VISIT', 'BOTH', FALSE, 'Short-term visit visa', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000011', 'default', 'OMANI_CITIZEN', 'Omani Citizen', 'Citizen residence status', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000012', 'default', 'EXPAT_RESIDENT', 'Expatriate Resident', 'Valid resident permit holder', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000013', 'default', 'VISIT_STATUS', 'Visit Status', 'Short term visit status', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000021', 'default', 'PRIVATE_SECTOR', 'Private Sector Labour Card', TRUE, 'Standard private sector labour card', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000022', 'default', 'FREEZONE', 'Freezone Labour Card', TRUE, 'Freezone labour card', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000031', 'default', 'NATIONAL_ID', 'National Civil ID', 'OMANI', TRUE, 'Civil ID for Omani nationals', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000032', 'default', 'RESIDENT_ID', 'Resident Card', 'EXPATRIATE', TRUE, 'Civil/resident ID for expatriates', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000033', 'default', 'UNIFIED_ID', 'Unified ID', 'BOTH', TRUE, 'Applicable to both employee groups', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000041', 'default', 'ORDINARY', 'Ordinary Passport', 'Standard travel passport', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000042', 'default', 'DIPLOMATIC', 'Diplomatic Passport', 'Diplomatic passport type', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000051', 'default', 'COMPANY_SPONSOR', 'Company Sponsored', 'EMPLOYEE', 'Employer-sponsored employee', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000052', 'default', 'FAMILY_SPONSOR', 'Family Sponsored', 'DEPENDENT', 'Family-sponsored dependent', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000053', 'default', 'GOV_SPONSOR', 'Government Sponsored', 'BOTH', 'Government-sponsored case', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000061', 'default', 'STANDARD_PERMIT', 'Standard Work Permit', TRUE, 'Standard private-sector permit', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000062', 'default', 'TEMP_PERMIT', 'Temporary Work Permit', FALSE, 'Temporary short-duration permit', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000071', 'default', 'OMANI', 'Omani National', TRUE, TRUE, 'Counts fully for Omanisation', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000072', 'default', 'GCC_NON_OMANI', 'GCC Non-Omani', FALSE, FALSE, 'GCC national excluding Oman', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000073', 'default', 'EXPAT', 'Expatriate', FALSE, FALSE, 'Non-GCC expatriate', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000081', 'default', 'PASI_OMANI', 'PASI Eligible Omani', TRUE, TRUE, TRUE, 'Omani employee PASI-eligible profile', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000082', 'default', 'EXPAT_INSURED', 'Expatriate Insured', FALSE, TRUE, FALSE, 'Expat profile with hazard coverage only', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000083', 'default', 'NOT_ELIGIBLE', 'Not Eligible', FALSE, FALSE, FALSE, 'No statutory social insurance eligibility', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000091', 'default', 'SPOUSE', 'Spouse Beneficiary', 1, 'Primary spouse beneficiary', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000092', 'default', 'CHILD', 'Child Beneficiary', 2, 'Child beneficiary', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000093', 'default', 'PARENT', 'Parent Beneficiary', 3, 'Parent beneficiary', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
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
    ('d3301000-0000-0000-0000-000000000101', 'default', 'SPOUSE_DEP', 'Spouse', TRUE, TRUE, 'Spouse dependent classification', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000102', 'default', 'CHILD_DEP', 'Child', TRUE, TRUE, 'Child dependent classification', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('d3301000-0000-0000-0000-000000000103', 'default', 'PARENT_DEP', 'Parent', FALSE, TRUE, 'Parent dependent classification', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, dependent_type_code) DO UPDATE
SET dependent_type_name = EXCLUDED.dependent_type_name,
    insurance_eligible_flag = EXCLUDED.insurance_eligible_flag,
    family_visa_eligible_flag = EXCLUDED.family_visa_eligible_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
