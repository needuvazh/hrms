CREATE SCHEMA IF NOT EXISTS attendance;

CREATE TABLE IF NOT EXISTS attendance.shifts (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    shift_code VARCHAR(50) NOT NULL,
    shift_name VARCHAR(120) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, shift_code)
);

CREATE TABLE IF NOT EXISTS attendance.shift_assignments (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_id UUID NOT NULL,
    shift_id UUID NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_shift_assignments_shift FOREIGN KEY (shift_id) REFERENCES attendance.shifts (id)
);

CREATE TABLE IF NOT EXISTS attendance.attendance_records (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_id UUID NOT NULL,
    attendance_date DATE NOT NULL,
    shift_id UUID NOT NULL,
    attendance_status VARCHAR(40) NOT NULL,
    first_punch_in TIMESTAMPTZ,
    last_punch_out TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (tenant_id, employee_id, attendance_date),
    CONSTRAINT fk_attendance_records_shift FOREIGN KEY (shift_id) REFERENCES attendance.shifts (id)
);

CREATE TABLE IF NOT EXISTS attendance.punch_events (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_id UUID NOT NULL,
    shift_id UUID NOT NULL,
    attendance_record_id UUID NOT NULL,
    punch_type VARCHAR(20) NOT NULL,
    event_time TIMESTAMPTZ NOT NULL,
    source VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_punch_events_shift FOREIGN KEY (shift_id) REFERENCES attendance.shifts (id),
    CONSTRAINT fk_punch_events_record FOREIGN KEY (attendance_record_id) REFERENCES attendance.attendance_records (id)
);

CREATE INDEX IF NOT EXISTS idx_attendance_shift_assignment_employee_date
    ON attendance.shift_assignments (tenant_id, employee_id, effective_from, effective_to);

CREATE INDEX IF NOT EXISTS idx_attendance_records_employee_date
    ON attendance.attendance_records (tenant_id, employee_id, attendance_date);

CREATE INDEX IF NOT EXISTS idx_punch_events_employee_time
    ON attendance.punch_events (tenant_id, employee_id, event_time);
