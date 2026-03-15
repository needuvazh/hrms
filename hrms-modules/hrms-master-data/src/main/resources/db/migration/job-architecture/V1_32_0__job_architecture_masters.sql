CREATE SCHEMA IF NOT EXISTS job_architecture;

CREATE TABLE IF NOT EXISTS job_architecture.job_families (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    job_family_code VARCHAR(64) NOT NULL,
    job_family_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_job_families_tenant_code UNIQUE (tenant_id, job_family_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.job_functions (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    job_function_code VARCHAR(64) NOT NULL,
    job_function_name VARCHAR(255) NOT NULL,
    job_family_id UUID,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_job_functions_family FOREIGN KEY (job_family_id) REFERENCES job_architecture.job_families(id),
    CONSTRAINT uq_job_functions_tenant_code UNIQUE (tenant_id, job_function_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.designations (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    designation_code VARCHAR(64) NOT NULL,
    designation_name VARCHAR(255) NOT NULL,
    short_name VARCHAR(120),
    job_family_id UUID,
    job_function_id UUID,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_designations_family FOREIGN KEY (job_family_id) REFERENCES job_architecture.job_families(id),
    CONSTRAINT fk_designations_function FOREIGN KEY (job_function_id) REFERENCES job_architecture.job_functions(id),
    CONSTRAINT uq_designations_tenant_code UNIQUE (tenant_id, designation_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.grade_bands (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    grade_band_code VARCHAR(64) NOT NULL,
    grade_band_name VARCHAR(255) NOT NULL,
    band_order INTEGER,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_grade_bands_tenant_code UNIQUE (tenant_id, grade_band_code),
    CONSTRAINT ck_grade_bands_band_order CHECK (band_order IS NULL OR band_order >= 0)
);

CREATE TABLE IF NOT EXISTS job_architecture.grades (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    grade_code VARCHAR(64) NOT NULL,
    grade_name VARCHAR(255) NOT NULL,
    grade_band_id UUID,
    ranking_order INTEGER,
    salary_scale_min NUMERIC(14,2),
    salary_scale_max NUMERIC(14,2),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_grades_grade_band FOREIGN KEY (grade_band_id) REFERENCES job_architecture.grade_bands(id),
    CONSTRAINT uq_grades_tenant_code UNIQUE (tenant_id, grade_code),
    CONSTRAINT ck_grades_ranking_order CHECK (ranking_order IS NULL OR ranking_order >= 0),
    CONSTRAINT ck_grades_salary_range CHECK (salary_scale_min IS NULL OR salary_scale_max IS NULL OR salary_scale_min <= salary_scale_max)
);

CREATE TABLE IF NOT EXISTS job_architecture.employment_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employment_type_code VARCHAR(64) NOT NULL,
    employment_type_name VARCHAR(255) NOT NULL,
    contract_required BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_employment_types_tenant_code UNIQUE (tenant_id, employment_type_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.worker_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    worker_type_code VARCHAR(64) NOT NULL,
    worker_type_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_worker_types_tenant_code UNIQUE (tenant_id, worker_type_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.employee_categories (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_category_code VARCHAR(64) NOT NULL,
    employee_category_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_employee_categories_tenant_code UNIQUE (tenant_id, employee_category_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.employee_subcategories (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_subcategory_code VARCHAR(64) NOT NULL,
    employee_subcategory_name VARCHAR(255) NOT NULL,
    employee_category_id UUID NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_employee_subcategories_category FOREIGN KEY (employee_category_id) REFERENCES job_architecture.employee_categories(id),
    CONSTRAINT uq_employee_subcategories_tenant_code UNIQUE (tenant_id, employee_subcategory_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.contract_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    contract_type_code VARCHAR(64) NOT NULL,
    contract_type_name VARCHAR(255) NOT NULL,
    fixed_term_flag BOOLEAN,
    default_duration_days INTEGER,
    renewal_allowed BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_contract_types_tenant_code UNIQUE (tenant_id, contract_type_code),
    CONSTRAINT ck_contract_types_default_duration CHECK (default_duration_days IS NULL OR default_duration_days > 0)
);

CREATE TABLE IF NOT EXISTS job_architecture.probation_policies (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    probation_policy_code VARCHAR(64) NOT NULL,
    probation_policy_name VARCHAR(255) NOT NULL,
    duration_days INTEGER NOT NULL,
    extension_allowed BOOLEAN,
    max_extension_days INTEGER,
    confirmation_required BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_probation_policies_tenant_code UNIQUE (tenant_id, probation_policy_code),
    CONSTRAINT ck_probation_policies_duration CHECK (duration_days > 0),
    CONSTRAINT ck_probation_policies_max_extension CHECK (max_extension_days IS NULL OR max_extension_days >= 0)
);

CREATE TABLE IF NOT EXISTS job_architecture.notice_period_policies (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    notice_policy_code VARCHAR(64) NOT NULL,
    notice_policy_name VARCHAR(255) NOT NULL,
    employee_notice_days INTEGER,
    employer_notice_days INTEGER,
    payment_in_lieu_allowed BOOLEAN,
    garden_leave_allowed BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_notice_period_policies_tenant_code UNIQUE (tenant_id, notice_policy_code),
    CONSTRAINT ck_notice_period_employee_days CHECK (employee_notice_days IS NULL OR employee_notice_days >= 0),
    CONSTRAINT ck_notice_period_employer_days CHECK (employer_notice_days IS NULL OR employer_notice_days >= 0)
);

CREATE TABLE IF NOT EXISTS job_architecture.transfer_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    transfer_type_code VARCHAR(64) NOT NULL,
    transfer_type_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_transfer_types_tenant_code UNIQUE (tenant_id, transfer_type_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.promotion_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    promotion_type_code VARCHAR(64) NOT NULL,
    promotion_type_name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_promotion_types_tenant_code UNIQUE (tenant_id, promotion_type_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.separation_reasons (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    separation_reason_code VARCHAR(64) NOT NULL,
    separation_reason_name VARCHAR(255) NOT NULL,
    separation_category VARCHAR(32) NOT NULL,
    voluntary_flag BOOLEAN,
    final_settlement_required BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_separation_reasons_tenant_code UNIQUE (tenant_id, separation_reason_code)
);

CREATE TABLE IF NOT EXISTS job_architecture.positions (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    position_code VARCHAR(64) NOT NULL,
    position_name VARCHAR(255) NOT NULL,
    designation_id UUID NOT NULL,
    job_family_id UUID,
    job_function_id UUID,
    grade_id UUID NOT NULL,
    grade_band_id UUID,
    legal_entity_id UUID,
    branch_id UUID,
    business_unit_id UUID,
    division_id UUID,
    department_id UUID,
    section_id UUID,
    work_location_id UUID,
    cost_center_id UUID,
    reporting_unit_id UUID,
    reports_to_position_id UUID,
    approved_headcount INTEGER NOT NULL DEFAULT 0,
    filled_headcount INTEGER NOT NULL DEFAULT 0,
    vacancy_status VARCHAR(32) NOT NULL,
    critical_position_flag BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT fk_positions_designation FOREIGN KEY (designation_id) REFERENCES job_architecture.designations(id),
    CONSTRAINT fk_positions_job_family FOREIGN KEY (job_family_id) REFERENCES job_architecture.job_families(id),
    CONSTRAINT fk_positions_job_function FOREIGN KEY (job_function_id) REFERENCES job_architecture.job_functions(id),
    CONSTRAINT fk_positions_grade FOREIGN KEY (grade_id) REFERENCES job_architecture.grades(id),
    CONSTRAINT fk_positions_grade_band FOREIGN KEY (grade_band_id) REFERENCES job_architecture.grade_bands(id),
    CONSTRAINT fk_positions_legal_entity FOREIGN KEY (legal_entity_id) REFERENCES organization.legal_entities(id),
    CONSTRAINT fk_positions_branch FOREIGN KEY (branch_id) REFERENCES organization.branches(id),
    CONSTRAINT fk_positions_business_unit FOREIGN KEY (business_unit_id) REFERENCES organization.business_units(id),
    CONSTRAINT fk_positions_division FOREIGN KEY (division_id) REFERENCES organization.divisions(id),
    CONSTRAINT fk_positions_department FOREIGN KEY (department_id) REFERENCES organization.departments(id),
    CONSTRAINT fk_positions_section FOREIGN KEY (section_id) REFERENCES organization.sections(id),
    CONSTRAINT fk_positions_work_location FOREIGN KEY (work_location_id) REFERENCES organization.work_locations(id),
    CONSTRAINT fk_positions_cost_center FOREIGN KEY (cost_center_id) REFERENCES organization.cost_centers(id),
    CONSTRAINT fk_positions_reporting_unit FOREIGN KEY (reporting_unit_id) REFERENCES organization.reporting_units(id),
    CONSTRAINT fk_positions_reports_to FOREIGN KEY (reports_to_position_id) REFERENCES job_architecture.positions(id),
    CONSTRAINT uq_positions_tenant_code UNIQUE (tenant_id, position_code),
    CONSTRAINT ck_positions_headcount CHECK (approved_headcount >= 0 AND filled_headcount >= 0 AND filled_headcount <= approved_headcount),
    CONSTRAINT ck_positions_vacancy_status CHECK (vacancy_status IN ('VACANT','PARTIALLY_FILLED','FILLED','FROZEN'))
);

CREATE TABLE IF NOT EXISTS job_architecture.audit_logs (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64),
    entity_name VARCHAR(128) NOT NULL,
    entity_id VARCHAR(128) NOT NULL,
    action VARCHAR(64) NOT NULL,
    old_value JSONB,
    new_value JSONB,
    changed_by VARCHAR(64),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(64),
    source VARCHAR(128),
    correlation_id VARCHAR(128)
);

CREATE INDEX IF NOT EXISTS idx_job_arch_designations_tenant_active ON job_architecture.designations (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_job_arch_job_families_tenant_active ON job_architecture.job_families (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_job_arch_job_functions_tenant_active ON job_architecture.job_functions (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_job_arch_grade_bands_tenant_active ON job_architecture.grade_bands (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_job_arch_grades_tenant_active ON job_architecture.grades (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_job_arch_positions_tenant_active ON job_architecture.positions (tenant_id, active);
