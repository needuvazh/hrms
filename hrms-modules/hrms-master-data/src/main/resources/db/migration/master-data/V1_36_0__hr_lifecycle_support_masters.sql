CREATE TABLE IF NOT EXISTS master_data.holiday_calendars (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    holiday_calendar_code VARCHAR(128) NOT NULL,
    holiday_calendar_name VARCHAR(255) NOT NULL,
    country_code VARCHAR(16),
    calendar_year INTEGER NOT NULL,
    calendar_type VARCHAR(32) NOT NULL,
    hijri_enabled_flag BOOLEAN,
    weekend_adjustment_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_holiday_calendars_tenant_code UNIQUE (tenant_id, holiday_calendar_code),
    CONSTRAINT ck_holiday_calendars_year CHECK (calendar_year BETWEEN 1900 AND 3000),
    CONSTRAINT ck_holiday_calendars_type CHECK (calendar_type IN ('PUBLIC', 'COMPANY', 'LOCATION', 'ENTITY')),
    CONSTRAINT fk_holiday_calendars_country FOREIGN KEY (country_code) REFERENCES master_data.countries(country_code)
);

CREATE TABLE IF NOT EXISTS master_data.leave_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    leave_type_code VARCHAR(128) NOT NULL,
    leave_type_name VARCHAR(255) NOT NULL,
    leave_category VARCHAR(32) NOT NULL,
    paid_flag BOOLEAN,
    supporting_document_required_flag BOOLEAN,
    gender_applicability VARCHAR(16),
    religion_applicability VARCHAR(16),
    nationalisation_applicability VARCHAR(32),
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_leave_types_tenant_code UNIQUE (tenant_id, leave_type_code),
    CONSTRAINT ck_leave_types_category CHECK (leave_category IN (
        'ANNUAL', 'SICK', 'MATERNITY', 'PATERNITY', 'RELIGIOUS', 'BEREAVEMENT', 'UNPAID', 'STUDY', 'COMP_OFF', 'OTHER'
    ))
);

CREATE TABLE IF NOT EXISTS master_data.shifts (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    shift_code VARCHAR(128) NOT NULL,
    shift_name VARCHAR(255) NOT NULL,
    shift_type VARCHAR(32) NOT NULL,
    start_time TIME,
    end_time TIME,
    break_duration_minutes INTEGER,
    overnight_flag BOOLEAN,
    grace_in_minutes INTEGER,
    grace_out_minutes INTEGER,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_shifts_tenant_code UNIQUE (tenant_id, shift_code),
    CONSTRAINT ck_shifts_type CHECK (shift_type IN ('FIXED', 'ROTATING', 'FLEXIBLE', 'SPLIT', 'ROSTER')),
    CONSTRAINT ck_shifts_break_duration CHECK (break_duration_minutes IS NULL OR break_duration_minutes >= 0),
    CONSTRAINT ck_shifts_grace_in CHECK (grace_in_minutes IS NULL OR grace_in_minutes >= 0),
    CONSTRAINT ck_shifts_grace_out CHECK (grace_out_minutes IS NULL OR grace_out_minutes >= 0)
);

CREATE TABLE IF NOT EXISTS master_data.attendance_sources (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    attendance_source_code VARCHAR(128) NOT NULL,
    attendance_source_name VARCHAR(255) NOT NULL,
    source_type VARCHAR(32) NOT NULL,
    trusted_source_flag BOOLEAN,
    manual_override_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_attendance_sources_tenant_code UNIQUE (tenant_id, attendance_source_code),
    CONSTRAINT ck_attendance_sources_type CHECK (source_type IN ('BIOMETRIC', 'MOBILE', 'WEB', 'MANUAL', 'UPLOAD', 'API', 'TIMESHEET'))
);

CREATE TABLE IF NOT EXISTS master_data.onboarding_task_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    onboarding_task_type_code VARCHAR(128) NOT NULL,
    onboarding_task_type_name VARCHAR(255) NOT NULL,
    task_category VARCHAR(128),
    mandatory_flag BOOLEAN,
    assignee_type VARCHAR(32) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_onboarding_task_types_tenant_code UNIQUE (tenant_id, onboarding_task_type_code),
    CONSTRAINT ck_onboarding_task_types_assignee CHECK (assignee_type IN ('HR', 'IT', 'MANAGER', 'EMPLOYEE', 'ADMIN', 'FACILITIES', 'SECURITY', 'FINANCE'))
);

CREATE TABLE IF NOT EXISTS master_data.offboarding_task_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    offboarding_task_type_code VARCHAR(128) NOT NULL,
    offboarding_task_type_name VARCHAR(255) NOT NULL,
    task_category VARCHAR(128),
    mandatory_flag BOOLEAN,
    assignee_type VARCHAR(32) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_offboarding_task_types_tenant_code UNIQUE (tenant_id, offboarding_task_type_code),
    CONSTRAINT ck_offboarding_task_types_assignee CHECK (assignee_type IN ('HR', 'IT', 'MANAGER', 'EMPLOYEE', 'ADMIN', 'FACILITIES', 'SECURITY', 'FINANCE'))
);

CREATE TABLE IF NOT EXISTS master_data.event_types (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    event_type_code VARCHAR(128) NOT NULL,
    event_type_name VARCHAR(255) NOT NULL,
    event_group VARCHAR(32) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_event_types_tenant_code UNIQUE (tenant_id, event_type_code),
    CONSTRAINT ck_event_types_group CHECK (event_group IN ('LIFECYCLE', 'DISCIPLINARY', 'MOVEMENT', 'CONTRACT', 'STATUS', 'OTHER'))
);

CREATE TABLE IF NOT EXISTS master_data.employee_statuses (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    employee_status_code VARCHAR(128) NOT NULL,
    employee_status_name VARCHAR(255) NOT NULL,
    employment_active_flag BOOLEAN,
    self_service_access_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_employee_statuses_tenant_code UNIQUE (tenant_id, employee_status_code)
);

CREATE TABLE IF NOT EXISTS master_data.employment_lifecycle_stages (
    id UUID PRIMARY KEY,
    tenant_id VARCHAR(64) NOT NULL,
    lifecycle_stage_code VARCHAR(128) NOT NULL,
    lifecycle_stage_name VARCHAR(255) NOT NULL,
    stage_order INTEGER,
    entry_stage_flag BOOLEAN,
    exit_stage_flag BOOLEAN,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(64) NOT NULL,
    updated_by VARCHAR(64) NOT NULL,
    CONSTRAINT uq_lifecycle_stages_tenant_code UNIQUE (tenant_id, lifecycle_stage_code),
    CONSTRAINT ck_lifecycle_stages_order CHECK (stage_order IS NULL OR stage_order >= 0)
);

CREATE INDEX IF NOT EXISTS idx_holiday_calendars_tenant_active ON master_data.holiday_calendars (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_leave_types_tenant_active ON master_data.leave_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_shifts_tenant_active ON master_data.shifts (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_attendance_sources_tenant_active ON master_data.attendance_sources (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_onboarding_task_types_tenant_active ON master_data.onboarding_task_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_offboarding_task_types_tenant_active ON master_data.offboarding_task_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_event_types_tenant_active ON master_data.event_types (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_employee_statuses_tenant_active ON master_data.employee_statuses (tenant_id, active);
CREATE INDEX IF NOT EXISTS idx_lifecycle_stages_tenant_active ON master_data.employment_lifecycle_stages (tenant_id, active);
