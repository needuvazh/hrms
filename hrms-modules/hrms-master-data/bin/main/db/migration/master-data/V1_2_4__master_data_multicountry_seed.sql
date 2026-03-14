INSERT INTO master_data.currency_master (
    id, tenant_id, country_code, currency_code, currency_name, symbol, minor_unit, rounding_mode, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000001', NULL, 'OM', 'OMR', 'Omani Rial', 'OMR', 3, 'HALF_UP', 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000002', NULL, 'AE', 'AED', 'UAE Dirham', 'AED', 2, 'HALF_UP', 2, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000003', NULL, 'SA', 'SAR', 'Saudi Riyal', 'SAR', 2, 'HALF_UP', 3, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.language_master (
    id, tenant_id, country_code, language_code, language_name, native_name, is_rtl, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000011', NULL, 'OM', 'en', 'English', 'English', FALSE, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000012', NULL, 'OM', 'ar', 'Arabic', 'العربية', TRUE, 2, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.timezone_master (
    id, tenant_id, country_code, timezone_code, timezone_name, utc_offset_minutes, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000021', NULL, 'OM', 'Asia/Muscat', 'Muscat Time', 240, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000022', NULL, 'AE', 'Asia/Dubai', 'Dubai Time', 240, 2, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000023', NULL, 'SA', 'Asia/Riyadh', 'Riyadh Time', 180, 3, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.country_locale_map (
    id, tenant_id, country_code, default_currency_code, default_language_code, default_timezone_code, first_day_of_week, weekend_days, date_format, time_format, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000031', NULL, 'OM', 'OMR', 'en', 'Asia/Muscat', 1, 'FRI,SAT', 'yyyy-MM-dd', 'HH:mm', TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000032', NULL, 'AE', 'AED', 'en', 'Asia/Dubai', 1, 'SAT,SUN', 'yyyy-MM-dd', 'HH:mm', TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000033', NULL, 'SA', 'SAR', 'ar', 'Asia/Riyadh', 1, 'FRI,SAT', 'yyyy-MM-dd', 'HH:mm', TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.employment_type_master (
    id, tenant_id, country_code, code, name, full_time_equivalent, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000101', NULL, NULL, 'FULL_TIME', 'Full Time', 1.000, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000102', NULL, NULL, 'PART_TIME', 'Part Time', 0.500, 2, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000103', NULL, NULL, 'CONTRACT', 'Contract', 1.000, 3, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.contract_type_master (
    id, tenant_id, country_code, code, name, max_duration_months, renewable_flag, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000111', NULL, NULL, 'LIMITED', 'Limited Contract', 24, TRUE, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000112', NULL, NULL, 'UNLIMITED', 'Unlimited Contract', NULL, FALSE, 2, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.worker_category_master (
    id, tenant_id, country_code, code, name, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000121', NULL, NULL, 'LOCAL', 'Local Employee', 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000122', NULL, NULL, 'EXPAT', 'Expat Employee', 2, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.hiring_stage_master (
    id, tenant_id, country_code, code, name, stage_order, terminal_flag, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000131', NULL, NULL, 'APPLIED', 'Applied', 1, FALSE, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000132', NULL, NULL, 'INTERVIEW', 'Interview', 2, FALSE, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000133', NULL, NULL, 'OFFER', 'Offer', 3, FALSE, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000134', NULL, NULL, 'HIRED', 'Hired', 4, TRUE, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.offer_status_master (
    id, tenant_id, country_code, code, name, terminal_flag, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000141', NULL, NULL, 'DRAFT', 'Draft', FALSE, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000142', NULL, NULL, 'SENT', 'Sent', FALSE, 2, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000143', NULL, NULL, 'ACCEPTED', 'Accepted', TRUE, 3, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000144', NULL, NULL, 'REJECTED', 'Rejected', TRUE, 4, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.pay_component_master (
    id, tenant_id, country_code, code, name, component_type, calculation_basis, taxable_flag, social_insurance_flag, affects_net_pay_flag, display_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000201', NULL, NULL, 'BASIC', 'Basic Salary', 'EARNING', 'FIXED', TRUE, TRUE, TRUE, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000202', NULL, NULL, 'HRA', 'Housing Allowance', 'EARNING', 'FIXED', TRUE, FALSE, TRUE, 2, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000203', NULL, NULL, 'TAX_WITHHOLD', 'Tax Withholding', 'TAX', 'SLAB', FALSE, FALSE, TRUE, 90, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000204', NULL, NULL, 'LOAN_DED', 'Loan Deduction', 'DEDUCTION', 'FIXED', FALSE, FALSE, TRUE, 91, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.deduction_type_master (
    id, tenant_id, country_code, code, name, mandatory_flag, recurring_flag, statutory_flag, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000211', NULL, NULL, 'LOAN', 'Loan Recovery', FALSE, TRUE, FALSE, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000212', NULL, NULL, 'PENALTY', 'Penalty', FALSE, FALSE, FALSE, 2, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.benefit_type_master (
    id, tenant_id, country_code, code, name, employee_contribution_allowed, employer_contribution_allowed, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000221', NULL, NULL, 'HEALTH_INS', 'Health Insurance', TRUE, TRUE, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000222', NULL, NULL, 'TRANSPORT', 'Transport Benefit', FALSE, TRUE, 2, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.tax_category_master (
    id, tenant_id, country_code, code, name, category_type, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000231', NULL, 'OM', 'OM_INCOME', 'Oman Income Tax', 'INCOME', 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000232', NULL, 'AE', 'AE_INCOME', 'UAE Income Tax', 'INCOME', 2, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000233', NULL, 'SA', 'SA_INCOME', 'Saudi Income Tax', 'INCOME', 3, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.status_master (
    id, tenant_id, country_code, status_domain, code, name, terminal_flag, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000241', NULL, NULL, 'EMPLOYEE_STATUS', 'ACTIVE', 'Active', FALSE, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000242', NULL, NULL, 'EMPLOYEE_STATUS', 'TERMINATED', 'Terminated', TRUE, 2, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000243', NULL, NULL, 'CANDIDATE_STATUS', 'OFFER_ACCEPTED', 'Offer Accepted', FALSE, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000244', NULL, NULL, 'CANDIDATE_STATUS', 'HIRED', 'Hired', TRUE, 2, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.reason_code_master (
    id, tenant_id, country_code, reason_domain, code, name, requires_comment, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000251', NULL, NULL, 'CANDIDATE_REJECTION', 'SKILL_GAP', 'Skill Gap', TRUE, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000252', NULL, NULL, 'PAYROLL_ADJUSTMENT', 'MANUAL_CORRECTION', 'Manual Correction', TRUE, 1, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.document_type_master (
    id, tenant_id, country_code, code, name, category, mandatory_flag, expiry_required_flag, retention_years, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000261', NULL, 'OM', 'PASSPORT', 'Passport', 'IDENTITY', TRUE, TRUE, 10, 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000262', NULL, 'OM', 'CIVIL_ID', 'Civil ID', 'IDENTITY', TRUE, TRUE, 10, 2, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000263', NULL, NULL, 'CONTRACT', 'Employment Contract', 'EMPLOYMENT', TRUE, FALSE, 10, 3, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.business_unit_master (
    id, tenant_id, country_code, code, name, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000301', 'default', 'OM', 'BU_CORE', 'Core Operations', 1, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.department_master (
    id, tenant_id, country_code, business_unit_id, code, name, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000311', 'default', 'OM', '34000000-0000-0000-0000-000000000301', 'ENG', 'Engineering', 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000312', 'default', 'OM', '34000000-0000-0000-0000-000000000301', 'HR', 'Human Resources', 2, TRUE, 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO master_data.designation_master (
    id, tenant_id, country_code, code, name, sort_order, is_active, created_by
) VALUES
    ('34000000-0000-0000-0000-000000000321', 'default', 'OM', 'SDE_1', 'Software Engineer I', 1, TRUE, 'seed'),
    ('34000000-0000-0000-0000-000000000322', 'default', 'OM', 'HR_EXEC', 'HR Executive', 2, TRUE, 'seed')
ON CONFLICT DO NOTHING;
