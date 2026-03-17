SYSTEM ROLE
You are a senior enterprise UI architect and frontend engineer.

STACK
Flutter
Enterprise admin UI

----------------------------------------------------

MODULE
Step 8 — HR-only Time and Lifecycle Support Masters

Design the UI and validation system for:

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

UI GOALS

Enterprise HRMS HR-side lifecycle and time-support configuration interface.

Must support:

- holiday calendar setup
- leave type setup
- shift template setup
- attendance source setup
- onboarding task template setup
- offboarding task template setup
- employee lifecycle event classification
- employee status setup
- lifecycle stage setup

Important distinction:

Employee Status
- current operational state
- example: ACTIVE, ON_LEAVE, SUSPENDED

Employment Lifecycle Stage
- broader journey stage
- example: ONBOARDING, ACTIVE_SERVICE, EXIT_CLEARANCE

Shift
- reusable shift template
- not actual employee shift assignment or roster transaction

Leave Type
- leave classification only
- not accrual or payroll calculation logic

----------------------------------------------------

SCREEN STRUCTURE

For each module create:

- list screen
- create screen
- edit screen
- view screen
- status toggle
- audit log placeholder

Use different complexity levels:

Simple masters
- Attendance Source
- Onboarding Task Type
- Offboarding Task Type
- Event Type
- Employee Status
- Employment Lifecycle Stage

Medium masters
- Holiday Calendar
- Leave Type

Rich masters
- Shift

Optional richer sub-screen if included
- Holiday Calendar Dates management

----------------------------------------------------

MODULE UI DETAILS

1. HOLIDAY CALENDAR

Columns

Holiday Calendar Code
Holiday Calendar Name
Country Code
Calendar Year
Calendar Type
Hijri Enabled
Weekend Adjustment
Status

Fields

Holiday Calendar Code
Holiday Calendar Name
Country Code optional
Calendar Year
Calendar Type
Hijri Enabled Flag
Weekend Adjustment Flag
Description
Active

Calendar Type values

PUBLIC
COMPANY
LOCATION
ENTITY

Optional if child table included

Holiday Date rows:

Holiday Date
Holiday Name
Holiday Type
Half Day Flag
Active


2. LEAVE TYPE

Columns

Leave Type Code
Leave Type Name
Leave Category
Paid
Supporting Document Required
Gender Applicability
Religion Applicability
Nationalisation Applicability
Status

Fields

Leave Type Code
Leave Type Name
Leave Category
Paid Flag
Supporting Document Required Flag
Gender Applicability optional
Religion Applicability optional
Nationalisation Applicability optional
Description
Active

Leave Category values

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


3. SHIFT

Columns

Shift Code
Shift Name
Shift Type
Start Time
End Time
Break Duration Minutes
Overnight
Grace In
Grace Out
Status

Fields

Shift Code
Shift Name
Shift Type
Start Time
End Time
Break Duration Minutes
Overnight Flag
Grace In Minutes
Grace Out Minutes
Description
Active

Shift Type values

FIXED
ROTATING
FLEXIBLE
SPLIT
ROSTER

Special UI behavior

- for FLEXIBLE shifts, show helper text that actual implementation may vary
- for overnight shifts, clearly label next-day crossing
- time fields should use proper time pickers
- show quick summary preview like:
  "09:00 to 18:00, break 60 mins, overnight no"


4. ATTENDANCE SOURCE

Columns

Attendance Source Code
Attendance Source Name
Source Type
Trusted Source
Manual Override
Status

Fields

Attendance Source Code
Attendance Source Name
Source Type
Trusted Source Flag
Manual Override Flag
Description
Active

Source Type values

BIOMETRIC
MOBILE
WEB
MANUAL
UPLOAD
API
TIMESHEET


5. ONBOARDING TASK TYPE

Columns

Task Type Code
Task Type Name
Task Category
Mandatory
Assignee Type
Status

Fields

Onboarding Task Type Code
Onboarding Task Type Name
Task Category
Mandatory Flag
Assignee Type
Description
Active

Assignee Type values

HR
IT
MANAGER
EMPLOYEE
ADMIN
FACILITIES
SECURITY


6. OFFBOARDING TASK TYPE

Columns

Task Type Code
Task Type Name
Task Category
Mandatory
Assignee Type
Status

Fields

Offboarding Task Type Code
Offboarding Task Type Name
Task Category
Mandatory Flag
Assignee Type
Description
Active

Assignee Type values

HR
IT
MANAGER
EMPLOYEE
ADMIN
FACILITIES
SECURITY
FINANCE


7. EVENT TYPE

Columns

Event Type Code
Event Type Name
Event Group
Status

Fields

Event Type Code
Event Type Name
Event Group
Description
Active

Event Group values

LIFECYCLE
DISCIPLINARY
MOVEMENT
CONTRACT
STATUS
OTHER


8. EMPLOYEE STATUS

Columns

Employee Status Code
Employee Status Name
Employment Active
Self Service Access
Status

Fields

Employee Status Code
Employee Status Name
Employment Active Flag
Self Service Access Flag
Description
Active


9. EMPLOYMENT LIFECYCLE STAGE

Columns

Lifecycle Stage Code
Lifecycle Stage Name
Stage Order
Entry Stage
Exit Stage
Status

Fields

Lifecycle Stage Code
Lifecycle Stage Name
Stage Order
Entry Stage Flag
Exit Stage Flag
Description
Active

----------------------------------------------------

VALIDATIONS

Common

- required code
- required name
- trim whitespace
- code uniqueness
- inline errors
- disable submit if invalid
- prevent duplicate submit
- status change confirmation
- success and error toasts

Holiday Calendar

- code required
- name required
- calendar year required
- calendar year must be valid numeric year
- calendar type required
- country code optional but must be valid if entered

If holiday date child rows are included:
- holiday date required
- holiday name required
- holiday date unique within selected calendar

Leave Type

- code required
- name required
- leave category required
- applicability dropdowns optional but valid if selected
- supporting document required is boolean only

Shift

- code required
- name required
- shift type required
- time values should be valid times when entered
- break duration must be zero or positive
- grace in/out minutes must be zero or positive
- if shift type is FIXED, start and end time usually required
- if overnight is enabled, show helper text and allow end time that implies next-day crossing
- prevent obviously invalid negative durations

Attendance Source

- code required
- name required
- source type required

Onboarding Task Type

- code required
- name required
- assignee type required

Offboarding Task Type

- code required
- name required
- assignee type required

Event Type

- code required
- name required
- event group required

Employee Status

- code required
- name required

Employment Lifecycle Stage

- code required
- name required
- stage order zero or positive
- entry and exit flags allowed together only if UX/business logic permits, otherwise show validation or warning

----------------------------------------------------

SPECIAL UX REQUIREMENTS

Shift screen should be richer than the simple masters.

Group the form into sections:

- Basic Information
- Shift Pattern
- Timing
- Grace Rules
- Summary

Holiday Calendar screen may optionally support a secondary tab:

- Calendar Header
- Holiday Dates

If holiday dates child table is included, support:
- add date row
- edit date row
- activate/deactivate row
- date list under selected calendar

Leave Type screen should provide clear helper text:
- "This screen defines leave categories only, not accrual or payroll rules."

Employee Status and Lifecycle Stage screens should clearly explain the difference:
- current state vs broader stage

----------------------------------------------------

UX REQUIREMENTS

Use consistent table layout
Top right action buttons
Status badges
Confirmation dialogs
Toast notifications
Empty state messages
Loading state
No results state
Search and filters
Reusable admin screen pattern

----------------------------------------------------

API INTEGRATION

Use endpoints

GET /api/hr-lifecycle/{master}
POST /api/hr-lifecycle/{master}
PUT /api/hr-lifecycle/{master}/{id}
PATCH /api/hr-lifecycle/{master}/{id}/status
GET /api/hr-lifecycle/{master}/options

Examples

GET /api/hr-lifecycle/holiday-calendars
GET /api/hr-lifecycle/leave-types
GET /api/hr-lifecycle/shifts
GET /api/hr-lifecycle/attendance-sources
GET /api/hr-lifecycle/onboarding-task-types
GET /api/hr-lifecycle/offboarding-task-types
GET /api/hr-lifecycle/event-types
GET /api/hr-lifecycle/employee-statuses
GET /api/hr-lifecycle/employment-lifecycle-stages

If holiday calendar child rows are included

GET /api/hr-lifecycle/holiday-calendars/{id}/dates
POST /api/hr-lifecycle/holiday-calendars/{id}/dates
PUT /api/hr-lifecycle/holiday-calendars/{id}/dates/{dateId}
PATCH /api/hr-lifecycle/holiday-calendars/{id}/dates/{dateId}/status

Also use option APIs from previous steps where needed for Leave Type:

GET /api/reference/genders/options
GET /api/reference/religions/options
GET /api/compliance/nationalisation-categories/options

----------------------------------------------------

FILTER REQUIREMENTS

List screens should support:

Holiday Calendar
- search
- status
- country code
- calendar year
- calendar type
- hijri enabled
- weekend adjustment

Leave Type
- search
- status
- leave category
- paid flag
- supporting document required
- gender applicability
- religion applicability
- nationalisation applicability

Shift
- search
- status
- shift type
- overnight flag

Attendance Source
- search
- status
- source type
- trusted source
- manual override

Onboarding Task Type
- search
- status
- task category
- mandatory
- assignee type

Offboarding Task Type
- search
- status
- task category
- mandatory
- assignee type

Event Type
- search
- status
- event group

Employee Status
- search
- status
- employment active
- self service access

Employment Lifecycle Stage
- search
- status
- entry stage
- exit stage

----------------------------------------------------

OUTPUT FORMAT

Return answer in this order:

1. screen map
2. reusable UI pattern
3. page specifications
4. form fields
5. validation rules
6. business validation
7. component structure
8. folder structure
9. state management
10. api integration
11. table configs
12. modal vs page design
13. shift screen UI specification
14. holiday calendar screen UI specification
15. sample JSON payloads
16. edge cases
17. reusable component suggestions

Make it implementation-ready.
Do not give generic advice.
If code examples are included, use Flutter style.
