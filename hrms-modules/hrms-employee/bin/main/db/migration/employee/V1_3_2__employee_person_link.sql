ALTER TABLE employee.employees
    ADD COLUMN IF NOT EXISTS person_id UUID;

CREATE INDEX IF NOT EXISTS idx_employee_tenant_person_id
    ON employee.employees (tenant_id, person_id);

CREATE TABLE IF NOT EXISTS employee.employment_history (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_id UUID NOT NULL,
    person_id UUID,
    lifecycle_type VARCHAR(64) NOT NULL,
    details_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    effective_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_employment_history_employee FOREIGN KEY (employee_id) REFERENCES employee.employees(id)
);

CREATE INDEX IF NOT EXISTS idx_employment_history_employee
    ON employee.employment_history (tenant_id, employee_id, effective_at DESC);
