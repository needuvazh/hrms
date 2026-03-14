CREATE SCHEMA IF NOT EXISTS wps;

CREATE TABLE IF NOT EXISTS wps.wps_batches (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    payroll_run_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    validation_summary TEXT,
    created_by VARCHAR(200) NOT NULL,
    exported_by VARCHAR(200),
    exported_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_wps_batches_tenant_payroll_run
    ON wps.wps_batches(tenant_id, payroll_run_id);

CREATE TABLE IF NOT EXISTS wps.wps_employee_entries (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    wps_batch_id UUID NOT NULL REFERENCES wps.wps_batches(id),
    employee_id UUID NOT NULL,
    net_amount NUMERIC(18,2) NOT NULL,
    payment_reference VARCHAR(250) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_wps_entries_tenant_batch
    ON wps.wps_employee_entries(tenant_id, wps_batch_id);

CREATE TABLE IF NOT EXISTS wps.wps_export_files (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    wps_batch_id UUID NOT NULL REFERENCES wps.wps_batches(id),
    export_type VARCHAR(60) NOT NULL,
    file_name VARCHAR(300) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    content_hash VARCHAR(128) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_wps_exports_tenant_batch
    ON wps.wps_export_files(tenant_id, wps_batch_id);
