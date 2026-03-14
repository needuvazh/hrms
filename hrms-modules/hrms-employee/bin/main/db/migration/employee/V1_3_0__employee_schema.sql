CREATE SCHEMA IF NOT EXISTS employee;

CREATE TABLE IF NOT EXISTS employee.employees (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_code VARCHAR(64) NOT NULL,
    first_name VARCHAR(128) NOT NULL,
    last_name VARCHAR(128),
    email VARCHAR(255) NOT NULL,
    department_code VARCHAR(64),
    job_title VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_employee_tenant_code UNIQUE (tenant_id, employee_code),
    CONSTRAINT uq_employee_tenant_email UNIQUE (tenant_id, email)
);

CREATE INDEX IF NOT EXISTS idx_employee_tenant_created_at ON employee.employees (tenant_id, created_at DESC);
