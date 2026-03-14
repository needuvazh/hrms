CREATE SCHEMA IF NOT EXISTS pasi;

CREATE TABLE IF NOT EXISTS pasi.pasi_contribution_rules (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    rule_code VARCHAR(100) NOT NULL,
    name VARCHAR(200) NOT NULL,
    employee_rate_percent NUMERIC(8,3) NOT NULL,
    employer_rate_percent NUMERIC(8,3) NOT NULL,
    salary_cap NUMERIC(18,3),
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_pasi_rule_code UNIQUE (tenant_id, rule_code)
);

CREATE TABLE IF NOT EXISTS pasi.pasi_period_records (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    payroll_run_id UUID NOT NULL,
    period_code VARCHAR(60) NOT NULL,
    contribution_rule_id UUID NOT NULL REFERENCES pasi.pasi_contribution_rules(id),
    status VARCHAR(30) NOT NULL,
    total_employees INTEGER NOT NULL,
    total_employee_contribution NUMERIC(18,3) NOT NULL,
    total_employer_contribution NUMERIC(18,3) NOT NULL,
    total_contribution NUMERIC(18,3) NOT NULL,
    calculated_by VARCHAR(200) NOT NULL,
    calculated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_pasi_period_run UNIQUE (tenant_id, payroll_run_id)
);

CREATE TABLE IF NOT EXISTS pasi.pasi_employee_contributions (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    pasi_period_record_id UUID NOT NULL REFERENCES pasi.pasi_period_records(id),
    payroll_employee_record_id UUID NOT NULL,
    employee_id UUID NOT NULL,
    contributable_salary NUMERIC(18,3) NOT NULL,
    employee_contribution NUMERIC(18,3) NOT NULL,
    employer_contribution NUMERIC(18,3) NOT NULL,
    total_contribution NUMERIC(18,3) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pasi_period_records_tenant_period
    ON pasi.pasi_period_records(tenant_id, period_code);

CREATE INDEX IF NOT EXISTS idx_pasi_employee_contributions_period
    ON pasi.pasi_employee_contributions(tenant_id, pasi_period_record_id);
