INSERT INTO master_data.holiday_calendars (
    id, tenant_id, holiday_calendar_code, holiday_calendar_name, country_code, calendar_year,
    calendar_type, hijri_enabled_flag, weekend_adjustment_flag, description, active, created_by, updated_by
)
VALUES
    ('c3613000-0000-0000-0000-000000000001', 'default', 'OMAN_PUBLIC_2026', 'Oman Public Calendar 2026', 'OM', 2026, 'PUBLIC', TRUE, TRUE, 'Default Oman public calendar', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000002', 'default', 'CORPORATE_2026', 'Corporate Calendar 2026', NULL, 2026, 'COMPANY', FALSE, TRUE, 'Default corporate calendar', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000003', 'lite', 'OMAN_PUBLIC_2026', 'Oman Public Calendar 2026', 'OM', 2026, 'PUBLIC', TRUE, TRUE, 'Lite Oman public calendar', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, holiday_calendar_code) DO UPDATE
SET holiday_calendar_name = EXCLUDED.holiday_calendar_name,
    country_code = EXCLUDED.country_code,
    calendar_year = EXCLUDED.calendar_year,
    calendar_type = EXCLUDED.calendar_type,
    hijri_enabled_flag = EXCLUDED.hijri_enabled_flag,
    weekend_adjustment_flag = EXCLUDED.weekend_adjustment_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.leave_types (
    id, tenant_id, leave_type_code, leave_type_name, leave_category, paid_flag,
    supporting_document_required_flag, gender_applicability, religion_applicability,
    nationalisation_applicability, description, active, created_by, updated_by
)
VALUES
    ('c3613000-0000-0000-0000-000000000011', 'default', 'ANNUAL_LEAVE', 'Annual Leave', 'ANNUAL', TRUE, FALSE, NULL, NULL, NULL, 'Default annual leave', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000012', 'default', 'SICK_LEAVE', 'Sick Leave', 'SICK', TRUE, TRUE, NULL, NULL, NULL, 'Default sick leave', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000013', 'lite', 'ANNUAL_LEAVE', 'Annual Leave', 'ANNUAL', TRUE, FALSE, NULL, NULL, NULL, 'Lite annual leave', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, leave_type_code) DO UPDATE
SET leave_type_name = EXCLUDED.leave_type_name,
    leave_category = EXCLUDED.leave_category,
    paid_flag = EXCLUDED.paid_flag,
    supporting_document_required_flag = EXCLUDED.supporting_document_required_flag,
    gender_applicability = EXCLUDED.gender_applicability,
    religion_applicability = EXCLUDED.religion_applicability,
    nationalisation_applicability = EXCLUDED.nationalisation_applicability,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.shifts (
    id, tenant_id, shift_code, shift_name, shift_type, start_time, end_time,
    break_duration_minutes, overnight_flag, grace_in_minutes, grace_out_minutes,
    description, active, created_by, updated_by
)
VALUES
    ('c3613000-0000-0000-0000-000000000021', 'default', 'GENERAL_SHIFT', 'General Shift', 'FIXED', '09:00:00', '18:00:00', 60, FALSE, 15, 15, 'Default day shift', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000022', 'default', 'NIGHT_SHIFT', 'Night Shift', 'FIXED', '21:00:00', '06:00:00', 60, TRUE, 10, 10, 'Default night shift', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000023', 'lite', 'GENERAL_SHIFT', 'General Shift', 'FIXED', '09:00:00', '18:00:00', 60, FALSE, 15, 15, 'Lite day shift', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, shift_code) DO UPDATE
SET shift_name = EXCLUDED.shift_name,
    shift_type = EXCLUDED.shift_type,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    break_duration_minutes = EXCLUDED.break_duration_minutes,
    overnight_flag = EXCLUDED.overnight_flag,
    grace_in_minutes = EXCLUDED.grace_in_minutes,
    grace_out_minutes = EXCLUDED.grace_out_minutes,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.attendance_sources (
    id, tenant_id, attendance_source_code, attendance_source_name, source_type,
    trusted_source_flag, manual_override_flag, description, active, created_by, updated_by
)
VALUES
    ('c3613000-0000-0000-0000-000000000031', 'default', 'BIOMETRIC_DEVICE', 'Biometric Device', 'BIOMETRIC', TRUE, FALSE, 'Biometric punch source', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000032', 'default', 'MANUAL_HR_ENTRY', 'Manual HR Entry', 'MANUAL', FALSE, TRUE, 'Manual attendance override', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000033', 'lite', 'BIOMETRIC_DEVICE', 'Biometric Device', 'BIOMETRIC', TRUE, FALSE, 'Lite biometric source', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, attendance_source_code) DO UPDATE
SET attendance_source_name = EXCLUDED.attendance_source_name,
    source_type = EXCLUDED.source_type,
    trusted_source_flag = EXCLUDED.trusted_source_flag,
    manual_override_flag = EXCLUDED.manual_override_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.onboarding_task_types (
    id, tenant_id, onboarding_task_type_code, onboarding_task_type_name, task_category,
    mandatory_flag, assignee_type, description, active, created_by, updated_by
)
VALUES
    ('c3613000-0000-0000-0000-000000000041', 'default', 'DOCUMENT_COLLECTION', 'Document Collection', 'DOCUMENTS', TRUE, 'HR', 'Collect mandatory onboarding documents', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000042', 'default', 'EMAIL_SETUP', 'Email Setup', 'IT_SETUP', TRUE, 'IT', 'Provision official email', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000043', 'lite', 'DOCUMENT_COLLECTION', 'Document Collection', 'DOCUMENTS', TRUE, 'HR', 'Lite document collection task', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, onboarding_task_type_code) DO UPDATE
SET onboarding_task_type_name = EXCLUDED.onboarding_task_type_name,
    task_category = EXCLUDED.task_category,
    mandatory_flag = EXCLUDED.mandatory_flag,
    assignee_type = EXCLUDED.assignee_type,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.offboarding_task_types (
    id, tenant_id, offboarding_task_type_code, offboarding_task_type_name, task_category,
    mandatory_flag, assignee_type, description, active, created_by, updated_by
)
VALUES
    ('c3613000-0000-0000-0000-000000000051', 'default', 'ASSET_RETURN', 'Asset Return', 'CLEARANCE', TRUE, 'IT', 'Return issued assets', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000052', 'default', 'EXIT_INTERVIEW', 'Exit Interview', 'INTERVIEW', FALSE, 'HR', 'Conduct exit interview', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000053', 'lite', 'ASSET_RETURN', 'Asset Return', 'CLEARANCE', TRUE, 'IT', 'Lite asset return task', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, offboarding_task_type_code) DO UPDATE
SET offboarding_task_type_name = EXCLUDED.offboarding_task_type_name,
    task_category = EXCLUDED.task_category,
    mandatory_flag = EXCLUDED.mandatory_flag,
    assignee_type = EXCLUDED.assignee_type,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.event_types (
    id, tenant_id, event_type_code, event_type_name, event_group, description, active, created_by, updated_by
)
VALUES
    ('c3613000-0000-0000-0000-000000000061', 'default', 'JOIN', 'Join', 'LIFECYCLE', 'Employee joining event', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000062', 'default', 'TRANSFER', 'Transfer', 'MOVEMENT', 'Employee transfer event', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000063', 'lite', 'JOIN', 'Join', 'LIFECYCLE', 'Lite joining event', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, event_type_code) DO UPDATE
SET event_type_name = EXCLUDED.event_type_name,
    event_group = EXCLUDED.event_group,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.employee_statuses (
    id, tenant_id, employee_status_code, employee_status_name, employment_active_flag,
    self_service_access_flag, description, active, created_by, updated_by
)
VALUES
    ('c3613000-0000-0000-0000-000000000071', 'default', 'ACTIVE', 'Active', TRUE, TRUE, 'Active employee status', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000072', 'default', 'ON_LEAVE', 'On Leave', TRUE, TRUE, 'Employee currently on leave', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000073', 'lite', 'ACTIVE', 'Active', TRUE, TRUE, 'Lite active status', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, employee_status_code) DO UPDATE
SET employee_status_name = EXCLUDED.employee_status_name,
    employment_active_flag = EXCLUDED.employment_active_flag,
    self_service_access_flag = EXCLUDED.self_service_access_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO master_data.employment_lifecycle_stages (
    id, tenant_id, lifecycle_stage_code, lifecycle_stage_name, stage_order,
    entry_stage_flag, exit_stage_flag, description, active, created_by, updated_by
)
VALUES
    ('c3613000-0000-0000-0000-000000000081', 'default', 'ONBOARDING', 'Onboarding', 1, TRUE, FALSE, 'Pre-active onboarding stage', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000082', 'default', 'ACTIVE_SERVICE', 'Active Service', 2, FALSE, FALSE, 'Primary active service stage', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000083', 'default', 'EXIT_CLEARANCE', 'Exit Clearance', 3, FALSE, TRUE, 'Exit and separation stage', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
    ('c3613000-0000-0000-0000-000000000084', 'lite', 'ONBOARDING', 'Onboarding', 1, TRUE, FALSE, 'Lite onboarding stage', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED')
ON CONFLICT (tenant_id, lifecycle_stage_code) DO UPDATE
SET lifecycle_stage_name = EXCLUDED.lifecycle_stage_name,
    stage_order = EXCLUDED.stage_order,
    entry_stage_flag = EXCLUDED.entry_stage_flag,
    exit_stage_flag = EXCLUDED.exit_stage_flag,
    description = EXCLUDED.description,
    active = EXCLUDED.active,
    updated_by = 'SYSTEM_SEED',
    updated_at = CURRENT_TIMESTAMP;
