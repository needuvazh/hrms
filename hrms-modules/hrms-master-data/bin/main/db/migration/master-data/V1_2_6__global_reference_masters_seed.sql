INSERT INTO master_data.currencies (
    id,
    currency_code,
    currency_name,
    currency_symbol,
    decimal_places,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00510001', 'OMR', 'Omani Rial', 'OMR', 3, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00510002', 'AED', 'UAE Dirham', 'AED', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00510003', 'SAR', 'Saudi Riyal', 'SAR', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00510004', 'INR', 'Indian Rupee', 'INR', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00510005', 'USD', 'US Dollar', 'USD', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (currency_code) DO UPDATE
SET currency_name = EXCLUDED.currency_name,
    currency_symbol = EXCLUDED.currency_symbol,
    decimal_places = EXCLUDED.decimal_places,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.countries (
    id,
    country_code,
    country_name,
    short_name,
    iso2_code,
    iso3_code,
    phone_code,
    nationality_name,
    default_currency_code,
    default_timezone,
    gcc_flag,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00520001', 'OM', 'Oman', 'Oman', 'OM', 'OMN', '+968', 'Omani', 'OMR', 'Asia/Muscat', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00520002', 'AE', 'United Arab Emirates', 'UAE', 'AE', 'ARE', '+971', 'Emirati', 'AED', 'Asia/Dubai', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00520003', 'SA', 'Saudi Arabia', 'Saudi', 'SA', 'SAU', '+966', 'Saudi', 'SAR', 'Asia/Riyadh', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00520004', 'IN', 'India', 'India', 'IN', 'IND', '+91', 'Indian', 'INR', 'Asia/Kolkata', FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00520005', 'US', 'United States', 'USA', 'US', 'USA', '+1', 'American', 'USD', 'America/New_York', FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (country_code) DO UPDATE
SET country_name = EXCLUDED.country_name,
    short_name = EXCLUDED.short_name,
    iso2_code = EXCLUDED.iso2_code,
    iso3_code = EXCLUDED.iso3_code,
    phone_code = EXCLUDED.phone_code,
    nationality_name = EXCLUDED.nationality_name,
    default_currency_code = EXCLUDED.default_currency_code,
    default_timezone = EXCLUDED.default_timezone,
    gcc_flag = EXCLUDED.gcc_flag,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.languages (
    id,
    language_code,
    language_name,
    native_name,
    rtl_enabled,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00530001', 'EN', 'English', 'English', FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00530002', 'AR', 'Arabic', 'al-Arabiyyah', TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00530003', 'HI', 'Hindi', 'Hindi', FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (language_code) DO UPDATE
SET language_name = EXCLUDED.language_name,
    native_name = EXCLUDED.native_name,
    rtl_enabled = EXCLUDED.rtl_enabled,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.nationalities (
    id,
    nationality_code,
    nationality_name,
    country_code,
    gcc_national_flag,
    omani_flag,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00540001', 'OMANI', 'Omani', 'OM', TRUE, TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00540002', 'EMIRATI', 'Emirati', 'AE', TRUE, FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00540003', 'SAUDI', 'Saudi', 'SA', TRUE, FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00540004', 'INDIAN', 'Indian', 'IN', FALSE, FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (nationality_code) DO UPDATE
SET nationality_name = EXCLUDED.nationality_name,
    country_code = EXCLUDED.country_code,
    gcc_national_flag = EXCLUDED.gcc_national_flag,
    omani_flag = EXCLUDED.omani_flag,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.religions (
    id,
    religion_code,
    religion_name,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00550001', 'ISLAM', 'Islam', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00550002', 'CHRISTIANITY', 'Christianity', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00550003', 'HINDUISM', 'Hinduism', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (religion_code) DO UPDATE
SET religion_name = EXCLUDED.religion_name,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.genders (
    id,
    gender_code,
    gender_name,
    display_order,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00560001', 'MALE', 'Male', 1, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00560002', 'FEMALE', 'Female', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00560003', 'OTHER', 'Other', 3, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (gender_code) DO UPDATE
SET gender_name = EXCLUDED.gender_name,
    display_order = EXCLUDED.display_order,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.marital_statuses (
    id,
    marital_status_code,
    marital_status_name,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00570001', 'SINGLE', 'Single', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00570002', 'MARRIED', 'Married', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00570003', 'DIVORCED', 'Divorced', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (marital_status_code) DO UPDATE
SET marital_status_name = EXCLUDED.marital_status_name,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.relationship_types (
    id,
    relationship_type_code,
    relationship_type_name,
    dependent_allowed,
    emergency_contact_allowed,
    beneficiary_allowed,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00580001', 'SPOUSE', 'Spouse', TRUE, TRUE, TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00580002', 'FATHER', 'Father', TRUE, TRUE, TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00580003', 'MOTHER', 'Mother', TRUE, TRUE, TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00580004', 'CHILD', 'Child', TRUE, TRUE, TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00580005', 'SIBLING', 'Sibling', FALSE, TRUE, FALSE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (relationship_type_code) DO UPDATE
SET relationship_type_name = EXCLUDED.relationship_type_name,
    dependent_allowed = EXCLUDED.dependent_allowed,
    emergency_contact_allowed = EXCLUDED.emergency_contact_allowed,
    beneficiary_allowed = EXCLUDED.beneficiary_allowed,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.document_types (
    id,
    document_type_code,
    document_type_name,
    short_description,
    document_for,
    issue_date_required,
    expiry_date_required,
    alert_required,
    alert_days_before,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00590001', 'PASSPORT', 'Passport', 'Government passport', 'EMPLOYEE', TRUE, TRUE, TRUE, 90, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00590002', 'NATIONAL_ID', 'National ID', 'National identity card', 'BOTH', TRUE, TRUE, TRUE, 60, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00590003', 'EMPLOYMENT_CONTRACT', 'Employment Contract', 'Signed employment contract', 'EMPLOYER', TRUE, FALSE, FALSE, 0, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (document_type_code) DO UPDATE
SET document_type_name = EXCLUDED.document_type_name,
    short_description = EXCLUDED.short_description,
    document_for = EXCLUDED.document_for,
    issue_date_required = EXCLUDED.issue_date_required,
    expiry_date_required = EXCLUDED.expiry_date_required,
    alert_required = EXCLUDED.alert_required,
    alert_days_before = EXCLUDED.alert_days_before,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.education_levels (
    id,
    education_level_code,
    education_level_name,
    ranking_order,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00600001', 'HIGH_SCHOOL', 'High School', 1, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00600002', 'DIPLOMA', 'Diploma', 2, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00600003', 'BACHELOR', 'Bachelor Degree', 3, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00600004', 'MASTER', 'Master Degree', 4, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (education_level_code) DO UPDATE
SET education_level_name = EXCLUDED.education_level_name,
    ranking_order = EXCLUDED.ranking_order,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.certification_types (
    id,
    certification_type_code,
    certification_type_name,
    expiry_tracking_required,
    issuing_body_required,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00610001', 'PROFESSIONAL_LICENSE', 'Professional License', TRUE, TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00610002', 'TECHNICAL_CERTIFICATION', 'Technical Certification', TRUE, TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00610003', 'COURSE_COMPLETION', 'Course Completion Certificate', FALSE, TRUE, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (certification_type_code) DO UPDATE
SET certification_type_name = EXCLUDED.certification_type_name,
    expiry_tracking_required = EXCLUDED.expiry_tracking_required,
    issuing_body_required = EXCLUDED.issuing_body_required,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.skill_categories (
    id,
    skill_category_code,
    skill_category_name,
    description,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00620001', 'TECH', 'Technical', 'Technical competencies', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00620002', 'SOFT', 'Soft Skills', 'Behavioral and communication skills', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00620003', 'LEAD', 'Leadership', 'Leadership and people management skills', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (skill_category_code) DO UPDATE
SET skill_category_name = EXCLUDED.skill_category_name,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.skills (
    id,
    skill_code,
    skill_name,
    skill_category_id,
    description,
    active,
    created_by,
    updated_by
)
VALUES
    ('f9f0f4e4-8406-46de-a7e4-5d2f00630001', 'JAVA', 'Java', (SELECT id FROM master_data.skill_categories WHERE skill_category_code = 'TECH'), 'Java application development', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00630002', 'SPRING_BOOT', 'Spring Boot', (SELECT id FROM master_data.skill_categories WHERE skill_category_code = 'TECH'), 'Spring Boot microservices and APIs', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00630003', 'COMMUNICATION', 'Communication', (SELECT id FROM master_data.skill_categories WHERE skill_category_code = 'SOFT'), 'Written and verbal communication', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00630004', 'TEAMWORK', 'Teamwork', (SELECT id FROM master_data.skill_categories WHERE skill_category_code = 'SOFT'), 'Cross-functional collaboration', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('f9f0f4e4-8406-46de-a7e4-5d2f00630005', 'COACHING', 'Coaching', (SELECT id FROM master_data.skill_categories WHERE skill_category_code = 'LEAD'), 'Mentoring and coaching capability', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (skill_code) DO UPDATE
SET skill_name = EXCLUDED.skill_name,
    skill_category_id = EXCLUDED.skill_category_id,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
