package com.company.hrms.hrlifecycle.model;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public final class HrLifecycleModels {

    private HrLifecycleModels() {
    }

    public enum CalendarType {
        PUBLIC,
        COMPANY,
        LOCATION,
        ENTITY
    }

    public enum LeaveCategory {
        ANNUAL,
        SICK,
        MATERNITY,
        PATERNITY,
        RELIGIOUS,
        BEREAVEMENT,
        UNPAID,
        STUDY,
        COMP_OFF,
        OTHER
    }

    public enum ShiftType {
        FIXED,
        ROTATING,
        FLEXIBLE,
        SPLIT,
        ROSTER
    }

    public enum AttendanceSourceType {
        BIOMETRIC,
        MOBILE,
        WEB,
        MANUAL,
        UPLOAD,
        API,
        TIMESHEET
    }

    public enum AssigneeType {
        HR,
        IT,
        MANAGER,
        EMPLOYEE,
        ADMIN,
        FACILITIES,
        SECURITY,
        FINANCE
    }

    public enum EventGroup {
        LIFECYCLE,
        DISCIPLINARY,
        MOVEMENT,
        CONTRACT,
        STATUS,
        OTHER
    }

    public enum Resource {
        HOLIDAY_CALENDARS("holiday-calendars", "master_data.holiday_calendars", "holiday_calendar_code", "holiday_calendar_name"),
        LEAVE_TYPES("leave-types", "master_data.leave_types", "leave_type_code", "leave_type_name"),
        SHIFTS("shifts", "master_data.shifts", "shift_code", "shift_name"),
        ATTENDANCE_SOURCES("attendance-sources", "master_data.attendance_sources", "attendance_source_code", "attendance_source_name"),
        ONBOARDING_TASK_TYPES("onboarding-task-types", "master_data.onboarding_task_types", "onboarding_task_type_code", "onboarding_task_type_name"),
        OFFBOARDING_TASK_TYPES("offboarding-task-types", "master_data.offboarding_task_types", "offboarding_task_type_code", "offboarding_task_type_name"),
        EVENT_TYPES("event-types", "master_data.event_types", "event_type_code", "event_type_name"),
        EMPLOYEE_STATUSES("employee-statuses", "master_data.employee_statuses", "employee_status_code", "employee_status_name"),
        EMPLOYMENT_LIFECYCLE_STAGES(
                "employment-lifecycle-stages",
                "master_data.employment_lifecycle_stages",
                "lifecycle_stage_code",
                "lifecycle_stage_name"
        );

        private final String path;
        private final String table;
        private final String codeColumn;
        private final String nameColumn;

        Resource(String path, String table, String codeColumn, String nameColumn) {
            this.path = path;
            this.table = table;
            this.codeColumn = codeColumn;
            this.nameColumn = nameColumn;
        }

        public String path() {
            return path;
        }

        public String table() {
            return table;
        }

        public String codeColumn() {
            return codeColumn;
        }

        public String nameColumn() {
            return nameColumn;
        }

        public static Resource fromPath(String path) {
            for (Resource value : values()) {
                if (value.path.equalsIgnoreCase(path)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown hr-lifecycle resource: " + path);
        }

        public Set<String> sortableColumns() {
            return switch (this) {
                case HOLIDAY_CALENDARS -> Set.of("holiday_calendar_code", "holiday_calendar_name", "calendar_year", "updated_at");
                case LEAVE_TYPES -> Set.of("leave_type_code", "leave_type_name", "leave_category", "updated_at");
                case SHIFTS -> Set.of("shift_code", "shift_name", "shift_type", "updated_at");
                case ATTENDANCE_SOURCES -> Set.of("attendance_source_code", "attendance_source_name", "source_type", "updated_at");
                case ONBOARDING_TASK_TYPES -> Set.of("onboarding_task_type_code", "onboarding_task_type_name", "assignee_type", "updated_at");
                case OFFBOARDING_TASK_TYPES -> Set.of("offboarding_task_type_code", "offboarding_task_type_name", "assignee_type", "updated_at");
                case EVENT_TYPES -> Set.of("event_type_code", "event_type_name", "event_group", "updated_at");
                case EMPLOYEE_STATUSES -> Set.of("employee_status_code", "employee_status_name", "updated_at");
                case EMPLOYMENT_LIFECYCLE_STAGES -> Set.of("lifecycle_stage_code", "lifecycle_stage_name", "stage_order", "updated_at");
            };
        }
    }

    public record SearchQuery(
            String q,
            Boolean active,
            int limit,
            int offset,
            String sort,
            String countryCode,
            Integer calendarYear,
            String calendarType,
            Boolean hijriEnabledFlag,
            Boolean weekendAdjustmentFlag,
            String leaveCategory,
            Boolean paidFlag,
            Boolean supportingDocumentRequiredFlag,
            String genderApplicability,
            String religionApplicability,
            String nationalisationApplicability,
            String shiftType,
            Boolean overnightFlag,
            String sourceType,
            Boolean trustedSourceFlag,
            Boolean manualOverrideFlag,
            String assigneeType,
            Boolean mandatoryFlag,
            String taskCategory,
            String eventGroup,
            Boolean employmentActiveFlag,
            Boolean selfServiceAccessFlag,
            Boolean entryStageFlag,
            Boolean exitStageFlag
    ) {
    }

    public record StatusUpdateCommand(boolean active) {
    }

    public record OptionViewDto(UUID id, String code, String name) {
    }

    public record MasterUpsertRequest(
            String code,
            String name,
            String countryCode,
            Integer calendarYear,
            String calendarType,
            Boolean hijriEnabledFlag,
            Boolean weekendAdjustmentFlag,
            String leaveCategory,
            Boolean paidFlag,
            Boolean supportingDocumentRequiredFlag,
            String genderApplicability,
            String religionApplicability,
            String nationalisationApplicability,
            String shiftType,
            LocalTime startTime,
            LocalTime endTime,
            Integer breakDurationMinutes,
            Boolean overnightFlag,
            Integer graceInMinutes,
            Integer graceOutMinutes,
            String sourceType,
            Boolean trustedSourceFlag,
            Boolean manualOverrideFlag,
            String taskCategory,
            Boolean mandatoryFlag,
            String assigneeType,
            String eventGroup,
            Boolean employmentActiveFlag,
            Boolean selfServiceAccessFlag,
            Integer stageOrder,
            Boolean entryStageFlag,
            Boolean exitStageFlag,
            String description,
            Boolean active
    ) {
    }

    public record MasterViewDto(
            UUID id,
            String tenantId,
            String code,
            String name,
            String countryCode,
            Integer calendarYear,
            String calendarType,
            Boolean hijriEnabledFlag,
            Boolean weekendAdjustmentFlag,
            String leaveCategory,
            Boolean paidFlag,
            Boolean supportingDocumentRequiredFlag,
            String genderApplicability,
            String religionApplicability,
            String nationalisationApplicability,
            String shiftType,
            LocalTime startTime,
            LocalTime endTime,
            Integer breakDurationMinutes,
            Boolean overnightFlag,
            Integer graceInMinutes,
            Integer graceOutMinutes,
            String sourceType,
            Boolean trustedSourceFlag,
            Boolean manualOverrideFlag,
            String taskCategory,
            Boolean mandatoryFlag,
            String assigneeType,
            String eventGroup,
            Boolean employmentActiveFlag,
            Boolean selfServiceAccessFlag,
            Integer stageOrder,
            Boolean entryStageFlag,
            Boolean exitStageFlag,
            String description,
            boolean active,
            Instant createdAt,
            Instant updatedAt,
            String createdBy,
            String updatedBy
    ) {
    }
}
