CREATE SCHEMA IF NOT EXISTS payroll;

CREATE TABLE IF NOT EXISTS payroll.payroll_periods (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    period_code VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_payroll_periods_tenant_period_code UNIQUE (tenant_id, period_code)
);

CREATE TABLE IF NOT EXISTS payroll.payroll_runs (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    payroll_period_id UUID NOT NULL REFERENCES payroll.payroll_periods(id),
    status VARCHAR(30) NOT NULL,
    initiated_by VARCHAR(200) NOT NULL,
    notes TEXT,
    locked_at TIMESTAMP WITH TIME ZONE,
    finalized_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payroll_runs_tenant_period
    ON payroll.payroll_runs(tenant_id, payroll_period_id);

CREATE TABLE IF NOT EXISTS payroll.payroll_employee_records (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    payroll_run_id UUID NOT NULL REFERENCES payroll.payroll_runs(id),
    employee_id UUID NOT NULL,
    gross_amount NUMERIC(18,2) NOT NULL,
    total_deduction_amount NUMERIC(18,2) NOT NULL,
    net_amount NUMERIC(18,2) NOT NULL,
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_payroll_record_employee UNIQUE (tenant_id, payroll_run_id, employee_id)
);

CREATE INDEX IF NOT EXISTS idx_payroll_records_tenant_run
    ON payroll.payroll_employee_records(tenant_id, payroll_run_id);

CREATE TABLE IF NOT EXISTS payroll.payroll_components (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    payroll_employee_record_id UUID NOT NULL REFERENCES payroll.payroll_employee_records(id),
    component_type VARCHAR(30) NOT NULL,
    component_code VARCHAR(100) NOT NULL,
    component_name VARCHAR(200) NOT NULL,
    amount NUMERIC(18,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payroll_components_tenant_record
    ON payroll.payroll_components(tenant_id, payroll_employee_record_id);

CREATE TABLE IF NOT EXISTS payroll.payslips (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    payroll_run_id UUID NOT NULL REFERENCES payroll.payroll_runs(id),
    payroll_employee_record_id UUID NOT NULL REFERENCES payroll.payroll_employee_records(id),
    employee_id UUID NOT NULL,
    gross_amount NUMERIC(18,2) NOT NULL,
    total_deduction_amount NUMERIC(18,2) NOT NULL,
    net_amount NUMERIC(18,2) NOT NULL,
    generated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_payslips_tenant_run
    ON payroll.payslips(tenant_id, payroll_run_id);
