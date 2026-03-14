CREATE SCHEMA IF NOT EXISTS leave;

CREATE TABLE IF NOT EXISTS leave.leave_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    leave_code VARCHAR(50) NOT NULL,
    leave_name VARCHAR(120) NOT NULL,
    is_paid BOOLEAN NOT NULL DEFAULT TRUE,
    annual_limit_days INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, leave_code)
);

CREATE TABLE IF NOT EXISTS leave.leave_balances (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_id UUID NOT NULL,
    leave_type_id UUID NOT NULL,
    leave_year INT NOT NULL,
    total_days INT NOT NULL,
    used_days INT NOT NULL,
    remaining_days INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, employee_id, leave_type_id, leave_year),
    CONSTRAINT fk_leave_balances_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave.leave_types (id)
);

CREATE TABLE IF NOT EXISTS leave.leave_requests (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_id UUID NOT NULL,
    leave_type_id UUID NOT NULL,
    from_date DATE NOT NULL,
    to_date DATE NOT NULL,
    requested_days INT NOT NULL,
    reason TEXT,
    leave_status VARCHAR(30) NOT NULL,
    workflow_instance_id UUID,
    requested_by VARCHAR(120) NOT NULL,
    reviewed_by VARCHAR(120),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_leave_requests_leave_type FOREIGN KEY (leave_type_id) REFERENCES leave.leave_types (id)
);

CREATE INDEX IF NOT EXISTS idx_leave_balances_employee_year
    ON leave.leave_balances (tenant_id, employee_id, leave_year);

CREATE INDEX IF NOT EXISTS idx_leave_requests_employee_dates
    ON leave.leave_requests (tenant_id, employee_id, from_date, to_date);
