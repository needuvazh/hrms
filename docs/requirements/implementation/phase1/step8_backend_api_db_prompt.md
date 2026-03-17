SYSTEM ROLE
You are a senior enterprise SaaS HRMS solution architect and senior Spring Boot developer.

PROJECT CONTEXT
I am building a multi-tenant enterprise HRMS platform using:

- Java
- Spring Boot
- Gradle multi-module architecture
- PostgreSQL
- JPA / Hibernate
- Flyway
- Lombok
- MapStruct
- Bean Validation
- REST APIs

This platform supports enterprise HR operations with employee administration, leave setup, holiday setup, attendance support, onboarding, offboarding, and lifecycle tracking.

----------------------------------------------------

MODULE
Step 8 — HR-only Time and Lifecycle Support Masters

These are HR-side support masters only.

They are needed for employee administration and workflow support, but they do NOT implement payroll calculations.

Modules to implement:

1. Holiday Calendar
2. Leave Type
3. Shift
4. Attendance Source
5. Onboarding Task Type
6. Offboarding Task Type
7. Event Type
8. Employee Status
9. Employment Lifecycle Stage

----------------------------------------------------

BUSINESS CONTEXT

These masters support:

- holiday and working calendar configuration
- leave category classification
- shift template setup
- attendance source tracing
- onboarding checklist setup
- offboarding checklist setup
- employee lifecycle event logging
- employee current status tracking
- broader lifecycle stage tracking

These masters are used by later modules such as:

- employee master
- employee leave requests
- attendance records
- onboarding checklists
- offboarding clearance
- employee history / event tracking
- lifecycle dashboards
- ESS / MSS workflow routing
- HR reporting

Important distinction:

Holiday Calendar
- defines the calendar header or configuration
- does NOT yet define all holiday-date transaction rows unless needed as child config

Leave Type
- defines HR leave classification
- does NOT implement entitlement accrual or payroll deduction logic

Shift
- defines reusable shift templates
- does NOT implement roster assignments or actual attendance transactions

Attendance Source
- defines where attendance came from
- example: biometric, mobile, manual entry

Employee Status
- current administrative/operational state
- example: ACTIVE, SUSPENDED, ON_LEAVE

Employment Lifecycle Stage
- broader journey stage
- example: ONBOARDING, ACTIVE_SERVICE, EXIT_CLEARANCE

Do NOT create payroll calculation logic in this step.

----------------------------------------------------

ARCHITECTURE REQUIREMENTS

Use clean layered architecture.

Generate:

- database schema
- Flyway migration scripts
- JPA entities
- repositories
- DTOs
- mappers
- services
- controllers
- validation
- audit logging
- pagination
- filtering
- option APIs

Use these design standards:

Primary keys
- UUID

Every table must include:

- tenant_id
- created_at
- updated_at
- created_by
- updated_by
- active flag

Use soft delete through active status.

All codes should be unique within tenant.

Assume these modules are tenant-owned for flexibility.

----------------------------------------------------

ENTITY DEFINITIONS

1. HOLIDAY CALENDAR

Purpose
Defines holiday calendar headers/configuration used by HR.

Examples
- OMAN_PUBLIC_2026
- UAE_PUBLIC_2026
- CORPORATE_OFFICE_2026
- FACTORY_CALENDAR_2026

Fields

id
tenant_id
holiday_calendar_code
holiday_calendar_name
country_code nullable
calendar_year
calendar_type
hijri_enabled_flag
weekend_adjustment_flag
description
active

calendar_type values

PUBLIC
COMPANY
LOCATION
ENTITY

Rules

holiday_calendar_code unique per tenant
holiday_calendar_name required
calendar_year required and should be a valid year
country_code optional but if provided should follow valid country code conventions

Important note
You may optionally add a child config table if needed:

holiday_calendar_dates
- id
- holiday_calendar_id
- holiday_date
- holiday_name
- holiday_type
- half_day_flag
- active

If you include it, clearly explain why and keep it simple and practical.


2. LEAVE TYPE

Purpose
Defines HR leave categories.

Examples
- ANNUAL_LEAVE
- SICK_LEAVE
- MATERNITY_LEAVE
- PATERNITY_LEAVE
- HAJJ_LEAVE
- BEREAVEMENT_LEAVE
- UNPAID_LEAVE
- STUDY_LEAVE
- COMP_OFF

Fields

id
tenant_id
leave_type_code
leave_type_name
leave_category
paid_flag
supporting_document_required_flag
gender_applicability nullable
religion_applicability nullable
nationalisation_applicability nullable
description
active

leave_category values

ANNUAL
SICK
MATERNITY
PATERNITY
RELIGIOUS
BEREAVEMENT
UNPAID
STUDY
COMP_OFF
OTHER

Rules

leave_type_code unique per tenant
leave_type_name required
leave_category required

Important boundary
Do not implement entitlement accrual, deduction, encashment, or payroll integration logic here.


3. SHIFT

Purpose
Defines reusable working shift templates.

Examples
- GENERAL_SHIFT
- NIGHT_SHIFT
- FLEXI_SHIFT
- SPLIT_SHIFT
- ROTATION_A
- ROTATION_B

Fields

id
tenant_id
shift_code
shift_name
shift_type
start_time
end_time
break_duration_minutes
overnight_flag
grace_in_minutes
grace_out_minutes
description
active

shift_type values

FIXED
ROTATING
FLEXIBLE
SPLIT
ROSTER

Rules

shift_code unique per tenant
shift_name required
shift_type required
break_duration_minutes >= 0 when provided
grace_in_minutes >= 0 when provided
grace_out_minutes >= 0 when provided

Important note
This step defines shift templates only.
Do not create shift assignment or roster transaction tables in this step.


4. ATTENDANCE SOURCE

Purpose
Defines where attendance data originates.

Examples
- BIOMETRIC_DEVICE
- MOBILE_GPS
- WEB_CHECKIN
- MANUAL_HR_ENTRY
- RAW_PUNCH_UPLOAD
- API_INTEGRATION
- TIMESHEET_ENTRY

Fields

id
tenant_id
attendance_source_code
attendance_source_name
source_type
trusted_source_flag
manual_override_flag
description
active

source_type values

BIOMETRIC
MOBILE
WEB
MANUAL
UPLOAD
API
TIMESHEET

Rules

attendance_source_code unique per tenant
attendance_source_name required
source_type required


5. ONBOARDING TASK TYPE

Purpose
Defines reusable onboarding checklist task categories.

Examples
- DOCUMENT_COLLECTION
- ID_CARD_CREATION
- LAPTOP_ALLOCATION
- EMAIL_SETUP
- ORIENTATION
- POLICY_ACKNOWLEDGEMENT
- MEDICAL_CHECK
- BANK_DETAILS_COLLECTION

Fields

id
tenant_id
onboarding_task_type_code
onboarding_task_type_name
task_category
mandatory_flag
assignee_type
description
active

assignee_type values

HR
IT
MANAGER
EMPLOYEE
ADMIN
FACILITIES
SECURITY

Rules

onboarding_task_type_code unique per tenant
onboarding_task_type_name required
assignee_type required


6. OFFBOARDING TASK TYPE

Purpose
Defines reusable exit and clearance checklist task categories.

Examples
- ASSET_RETURN
- ID_CARD_RETURN
- ACCESS_DISABLEMENT
- EXIT_INTERVIEW
- FINAL_DOCUMENT_COLLECTION
- VISA_CANCELLATION_INITIATION
- CLEARANCE_APPROVAL
- EXPERIENCE_LETTER_PREPARATION

Fields

id
tenant_id
offboarding_task_type_code
offboarding_task_type_name
task_category
mandatory_flag
assignee_type
description
active

assignee_type values

HR
IT
MANAGER
EMPLOYEE
ADMIN
FACILITIES
SECURITY
FINANCE

Rules

offboarding_task_type_code unique per tenant
offboarding_task_type_name required
assignee_type required


7. EVENT TYPE

Purpose
Defines HR lifecycle events recorded against employees.

Examples
- JOIN
- CONFIRMATION
- TRANSFER
- PROMOTION
- SECONDMENT
- CONTRACT_RENEWAL
- SUSPENSION
- WARNING
- SEPARATION
- RETIREMENT
- REHIRE

Fields

id
tenant_id
event_type_code
event_type_name
event_group
description
active

event_group values

LIFECYCLE
DISCIPLINARY
MOVEMENT
CONTRACT
STATUS
OTHER

Rules

event_type_code unique per tenant
event_type_name required
event_group required


8. EMPLOYEE STATUS

Purpose
Defines the employee’s current operational or administrative status.

Examples
- DRAFT
- ACTIVE
- ON_PROBATION
- ON_LEAVE
- SUSPENDED
- ABSCONDED
- RESIGNED
- TERMINATED
- RETIRED
- DECEASED
- INACTIVE

Fields

id
tenant_id
employee_status_code
employee_status_name
employment_active_flag
self_service_access_flag
description
active

Rules

employee_status_code unique per tenant
employee_status_name required

Important note
Do not hardcode payroll behavior here.
Keep this HR-side and lifecycle-focused.


9. EMPLOYMENT LIFECYCLE STAGE

Purpose
Defines broader journey stages across employee lifecycle.

Examples
- PREBOARDING
- ONBOARDING
- ACTIVE_SERVICE
- CONFIRMATION_PENDING
- INTERNAL_MOVEMENT
- NOTICE_PERIOD
- EXIT_CLEARANCE
- SEPARATED
- ARCHIVED

Fields

id
tenant_id
lifecycle_stage_code
lifecycle_stage_name
stage_order
entry_stage_flag
exit_stage_flag
description
active

Rules

lifecycle_stage_code unique per tenant
lifecycle_stage_name required
stage_order >= 0 when provided


10. AUDIT LOG

Fields

id
entity_name
entity_id
action
old_value
new_value
changed_by
changed_at
ip_address
source
correlation_id

Track events

create
update
status change
rule/category change
stage order change
task type reassignment
shift behavior update

----------------------------------------------------

DATABASE TABLES

Create tables:

holiday_calendars
leave_types
shifts
attendance_sources
onboarding_task_types
offboarding_task_types
event_types
employee_statuses
employment_lifecycle_stages
audit_logs

Optional supporting table if you choose to include it:

holiday_calendar_dates

Include:

- indexes
- foreign keys where needed
- tenant scoped unique constraints
- check constraints where useful

Reference dependencies from previous steps if needed:

Step 2 global/reference masters
- countries
- genders
- religions

Step 5 compliance masters
- nationalisation_categories

----------------------------------------------------

API ENDPOINTS

Holiday Calendar

POST /api/hr-lifecycle/holiday-calendars
GET /api/hr-lifecycle/holiday-calendars
GET /api/hr-lifecycle/holiday-calendars/{id}
PUT /api/hr-lifecycle/holiday-calendars/{id}
PATCH /api/hr-lifecycle/holiday-calendars/{id}/status
GET /api/hr-lifecycle/holiday-calendars/options

Optional if holiday_calendar_dates is included:

POST /api/hr-lifecycle/holiday-calendars/{id}/dates
GET /api/hr-lifecycle/holiday-calendars/{id}/dates
PUT /api/hr-lifecycle/holiday-calendars/{id}/dates/{dateId}
PATCH /api/hr-lifecycle/holiday-calendars/{id}/dates/{dateId}/status


Leave Type

POST /api/hr-lifecycle/leave-types
GET /api/hr-lifecycle/leave-types
GET /api/hr-lifecycle/leave-types/{id}
PUT /api/hr-lifecycle/leave-types/{id}
PATCH /api/hr-lifecycle/leave-types/{id}/status
GET /api/hr-lifecycle/leave-types/options


Shift

POST /api/hr-lifecycle/shifts
GET /api/hr-lifecycle/shifts
GET /api/hr-lifecycle/shifts/{id}
PUT /api/hr-lifecycle/shifts/{id}
PATCH /api/hr-lifecycle/shifts/{id}/status
GET /api/hr-lifecycle/shifts/options


Attendance Source

POST /api/hr-lifecycle/attendance-sources
GET /api/hr-lifecycle/attendance-sources
GET /api/hr-lifecycle/attendance-sources/{id}
PUT /api/hr-lifecycle/attendance-sources/{id}
PATCH /api/hr-lifecycle/attendance-sources/{id}/status
GET /api/hr-lifecycle/attendance-sources/options


Onboarding Task Type

POST /api/hr-lifecycle/onboarding-task-types
GET /api/hr-lifecycle/onboarding-task-types
GET /api/hr-lifecycle/onboarding-task-types/{id}
PUT /api/hr-lifecycle/onboarding-task-types/{id}
PATCH /api/hr-lifecycle/onboarding-task-types/{id}/status
GET /api/hr-lifecycle/onboarding-task-types/options


Offboarding Task Type

POST /api/hr-lifecycle/offboarding-task-types
GET /api/hr-lifecycle/offboarding-task-types
GET /api/hr-lifecycle/offboarding-task-types/{id}
PUT /api/hr-lifecycle/offboarding-task-types/{id}
PATCH /api/hr-lifecycle/offboarding-task-types/{id}/status
GET /api/hr-lifecycle/offboarding-task-types/options


Event Type

POST /api/hr-lifecycle/event-types
GET /api/hr-lifecycle/event-types
GET /api/hr-lifecycle/event-types/{id}
PUT /api/hr-lifecycle/event-types/{id}
PATCH /api/hr-lifecycle/event-types/{id}/status
GET /api/hr-lifecycle/event-types/options


Employee Status

POST /api/hr-lifecycle/employee-statuses
GET /api/hr-lifecycle/employee-statuses
GET /api/hr-lifecycle/employee-statuses/{id}
PUT /api/hr-lifecycle/employee-statuses/{id}
PATCH /api/hr-lifecycle/employee-statuses/{id}/status
GET /api/hr-lifecycle/employee-statuses/options


Employment Lifecycle Stage

POST /api/hr-lifecycle/employment-lifecycle-stages
GET /api/hr-lifecycle/employment-lifecycle-stages
GET /api/hr-lifecycle/employment-lifecycle-stages/{id}
PUT /api/hr-lifecycle/employment-lifecycle-stages/{id}
PATCH /api/hr-lifecycle/employment-lifecycle-stages/{id}/status
GET /api/hr-lifecycle/employment-lifecycle-stages/options

----------------------------------------------------

LIST API REQUIREMENTS

All list APIs should support:

- tenant scoping
- pagination
- sorting
- search by code and name
- active/inactive filter

Additional filters:

Holiday Calendar
- countryCode
- calendarYear
- calendarType
- hijriEnabledFlag
- weekendAdjustmentFlag

Leave Type
- leaveCategory
- paidFlag
- supportingDocumentRequiredFlag
- genderApplicability
- religionApplicability
- nationalisationApplicability

Shift
- shiftType
- overnightFlag

Attendance Source
- sourceType
- trustedSourceFlag
- manualOverrideFlag

Onboarding Task Type
- assigneeType
- mandatoryFlag
- taskCategory

Offboarding Task Type
- assigneeType
- mandatoryFlag
- taskCategory

Event Type
- eventGroup

Employee Status
- employmentActiveFlag
- selfServiceAccessFlag

Employment Lifecycle Stage
- entryStageFlag
- exitStageFlag

Options APIs should return active items by default.

----------------------------------------------------

VALIDATION REQUIREMENTS

Use Bean Validation and service-level validation.

Validate:

- required fields
- code uniqueness per tenant
- valid enums
- calendar_year reasonable numeric/year validation
- display/stage order >= 0
- break_duration_minutes >= 0
- grace_in_minutes >= 0
- grace_out_minutes >= 0
- assignee_type required
- shift time consistency
- holiday calendar child dates unique within calendar if child table is included
- leave type applicability fields valid against reference masters
- attendance source type validity

Shift-specific validation

- if overnight_flag is false, start and end time should still be logically allowed but explain chosen validation approach
- if shift_type = FLEXIBLE, start and end time may be optional or interpreted differently, explain in code comments
- if shift_type = SPLIT or ROTATING, keep master simple and note that detailed sessions/rotation rules belong to later modules

Add extension points/comments for future validations such as:

- cannot deactivate leave type already used by active leave transactions
- cannot deactivate employee status already assigned to active employees without replacement strategy
- cannot change lifecycle stage ordering without impact review
- cannot deactivate holiday calendar already linked to legal entities, locations, or employees

----------------------------------------------------

PACKAGE / MODULE STRUCTURE

Use clean modular package structure like:

hrlifecycle/leavetype/domain/entity
hrlifecycle/leavetype/domain/repository
hrlifecycle/leavetype/application/service
hrlifecycle/leavetype/application/dto/request
hrlifecycle/leavetype/application/dto/response
hrlifecycle/leavetype/application/mapper
hrlifecycle/leavetype/interfaces/rest

Use a consistent equivalent structure for all modules.

Also include:

- base tenant-aware entity
- common active status handling
- reusable paged response wrapper
- reusable api response wrapper
- global exception handling
- common audit support
- reusable option DTO

----------------------------------------------------

OUTPUT FORMAT REQUIRED

Return answer in this order:

1. package structure
2. database schema
3. Flyway migrations
4. entity classes
5. DTO classes
6. repositories
7. services
8. mappers
9. controllers
10. validation rules
11. exception handling
12. audit logging
13. sample JSON requests and responses
14. option API response samples
15. future extension notes

Produce production-ready code.
Avoid pseudo-code.
Keep the code realistic, consistent, and maintainable.
