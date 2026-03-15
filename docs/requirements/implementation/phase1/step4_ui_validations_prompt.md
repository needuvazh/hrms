SYSTEM ROLE
You are a senior enterprise UI architect and frontend engineer.

STACK
React
TypeScript
Enterprise admin UI

----------------------------------------------------

MODULE
Step 4 — Job Architecture Masters

Design the UI and validation system for:

1. Designation / Job Title
2. Job Family
3. Job Function
4. Grade
5. Grade Band
6. Position
7. Employment Type
8. Worker Type
9. Employee Category
10. Employee Subcategory
11. Contract Type
12. Probation Policy
13. Notice Period Policy
14. Transfer Type
15. Promotion Type
16. Separation Reason

----------------------------------------------------

UI GOALS

Enterprise HRMS admin interface.

Must support:

- job architecture setup
- position structure setup
- employee categorisation setup
- lifecycle policy setup
- downstream employee assignment readiness
- reusable admin patterns
- strong field validation
- good UX for both simple and complex masters

Important distinction:

Designation / Job Title
- the role title or classification

Position
- the actual approved seat in the organization

UI must make this distinction clear.

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
- Employment Type
- Worker Type
- Transfer Type
- Promotion Type
- Separation Reason
- Job Family
- Job Function
- Employee Category
- Employee Subcategory

Medium masters
- Designation
- Grade Band
- Grade
- Contract Type
- Probation Policy
- Notice Period Policy

Rich masters
- Position

----------------------------------------------------

MODULE UI DETAILS

1. DESIGNATION / JOB TITLE

Columns

Designation Code
Designation Name
Job Family
Job Function
Status

Fields

Designation Code
Designation Name
Short Name
Job Family optional
Job Function optional
Description
Active


2. JOB FAMILY

Columns

Job Family Code
Job Family Name
Status

Fields

Job Family Code
Job Family Name
Description
Active


3. JOB FUNCTION

Columns

Job Function Code
Job Function Name
Job Family
Status

Fields

Job Function Code
Job Function Name
Job Family optional
Description
Active


4. GRADE BAND

Columns

Grade Band Code
Grade Band Name
Band Order
Status

Fields

Grade Band Code
Grade Band Name
Band Order
Description
Active


5. GRADE

Columns

Grade Code
Grade Name
Grade Band
Ranking Order
Salary Scale Min
Salary Scale Max
Status

Fields

Grade Code
Grade Name
Grade Band optional
Ranking Order
Salary Scale Min
Salary Scale Max
Description
Active


6. POSITION

Columns

Position Code
Position Name
Designation
Grade
Legal Entity
Department
Approved Headcount
Filled Headcount
Vacancy Status
Critical Position
Status

Fields

Position Code
Position Name
Designation
Job Family optional
Job Function optional
Grade
Grade Band optional
Legal Entity optional
Branch optional
Business Unit optional
Division optional
Department optional
Section optional
Work Location optional
Cost Center optional
Reporting Unit optional
Reports To Position optional
Approved Headcount
Filled Headcount
Vacancy Status
Critical Position Flag
Description
Active

Vacancy Status values

VACANT
PARTIALLY_FILLED
FILLED
FROZEN


7. EMPLOYMENT TYPE

Columns

Employment Type Code
Employment Type Name
Contract Required
Status

Fields

Employment Type Code
Employment Type Name
Contract Required
Description
Active


8. WORKER TYPE

Columns

Worker Type Code
Worker Type Name
Status

Fields

Worker Type Code
Worker Type Name
Description
Active


9. EMPLOYEE CATEGORY

Columns

Employee Category Code
Employee Category Name
Status

Fields

Employee Category Code
Employee Category Name
Description
Active


10. EMPLOYEE SUBCATEGORY

Columns

Subcategory Code
Subcategory Name
Employee Category
Status

Fields

Employee Subcategory Code
Employee Subcategory Name
Employee Category
Description
Active


11. CONTRACT TYPE

Columns

Contract Type Code
Contract Type Name
Fixed Term
Default Duration Days
Renewal Allowed
Status

Fields

Contract Type Code
Contract Type Name
Fixed Term Flag
Default Duration Days
Renewal Allowed
Description
Active


12. PROBATION POLICY

Columns

Probation Policy Code
Probation Policy Name
Duration Days
Extension Allowed
Max Extension Days
Confirmation Required
Status

Fields

Probation Policy Code
Probation Policy Name
Duration Days
Extension Allowed
Max Extension Days
Confirmation Required
Description
Active


13. NOTICE PERIOD POLICY

Columns

Notice Policy Code
Notice Policy Name
Employee Notice Days
Employer Notice Days
Payment In Lieu
Garden Leave
Status

Fields

Notice Policy Code
Notice Policy Name
Employee Notice Days
Employer Notice Days
Payment In Lieu Allowed
Garden Leave Allowed
Description
Active


14. TRANSFER TYPE

Columns

Transfer Type Code
Transfer Type Name
Status

Fields

Transfer Type Code
Transfer Type Name
Description
Active


15. PROMOTION TYPE

Columns

Promotion Type Code
Promotion Type Name
Status

Fields

Promotion Type Code
Promotion Type Name
Description
Active


16. SEPARATION REASON

Columns

Separation Reason Code
Separation Reason Name
Separation Category
Voluntary
Final Settlement Required
Status

Fields

Separation Reason Code
Separation Reason Name
Separation Category
Voluntary Flag
Final Settlement Required
Description
Active

Separation Category values

RESIGNATION
TERMINATION
RETIREMENT
CONTRACT_EXPIRY
DEATH
ABSCONDING
OTHER

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

Designation

- code required
- name required
- selected job family and job function must load from options API

Job Function

- code required
- name required
- optional job family must be valid

Grade Band

- band order must be zero or positive

Grade

- ranking order zero or positive
- salary scale min and max numeric
- min cannot exceed max

Position

- position code required
- position name required
- designation required
- grade required
- approved headcount must be zero or positive integer
- filled headcount must be zero or positive integer
- filled headcount should not exceed approved headcount
- reports to position cannot equal current position
- reports to position dropdown should exclude current record on edit
- selected org structure references must be valid options
- vacancy status required
- show warning if position is marked filled but approved headcount is zero

Employee Subcategory

- employee category required

Contract Type

- if fixed term is true and default duration is blank, allow or enforce based on chosen UX strategy, but explain clearly
- default duration cannot be negative

Probation Policy

- duration days must be greater than zero
- if extension allowed is false, max extension days should be disabled and reset
- if extension allowed is true, max extension days must be zero or positive

Notice Period Policy

- employee notice days zero or positive
- employer notice days zero or positive

Separation Reason

- separation category required

----------------------------------------------------

SPECIAL UX REQUIREMENTS

Position screen must be richer than others.

Position form should support grouped sections:

- Basic Information
- Job Structure
- Organization Assignment
- Reporting Structure
- Headcount and Vacancy
- Additional Information

Use dropdowns loaded from option APIs for:

- designations
- job families
- job functions
- grades
- grade bands
- legal entities
- branches
- business units
- divisions
- departments
- sections
- work locations
- cost centers
- reporting units
- positions

Make Position a full page form, not a small modal.

Other modules can use modal, drawer, or page depending on complexity.

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

GET /api/job-architecture/{master}
POST /api/job-architecture/{master}
PUT /api/job-architecture/{master}/{id}
PATCH /api/job-architecture/{master}/{id}/status
GET /api/job-architecture/{master}/options

Examples

GET /api/job-architecture/designations
GET /api/job-architecture/job-families
GET /api/job-architecture/job-functions
GET /api/job-architecture/grades
GET /api/job-architecture/grade-bands
GET /api/job-architecture/positions

Also use option APIs from previous steps where needed for Position:

GET /api/organization/legal-entities/options
GET /api/organization/branches/options
GET /api/organization/business-units/options
GET /api/organization/divisions/options
GET /api/organization/departments/options
GET /api/organization/sections/options
GET /api/organization/work-locations/options
GET /api/organization/cost-centers/options
GET /api/organization/reporting-units/options

----------------------------------------------------

FILTER REQUIREMENTS

List screens should support:

Designation
- search
- status
- job family
- job function

Job Function
- search
- status
- job family

Grade
- search
- status
- grade band

Position
- search
- status
- designation
- grade
- grade band
- legal entity
- branch
- department
- vacancy status
- critical position flag

Employee Subcategory
- search
- status
- employee category

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
13. position screen UI specification
14. sample JSON payloads
15. edge cases
16. reusable component suggestions

Make it implementation-ready.
Do not give generic advice.
If code examples are included, use React + TypeScript style.
