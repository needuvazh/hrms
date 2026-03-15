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

I want you to generate backend design and production-ready code for the following system module.

----------------------------------------------------

MODULE
Step 4 — Job Architecture Masters

These define the enterprise position structure, employee categorisation structure, and lifecycle policy structure.

Modules to implement:

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

BUSINESS CONTEXT

These are tenant-owned masters.

Every record must belong to a tenant.

These modules will later support:

- employee master
- recruitment
- position management
- approved headcount
- vacancy tracking
- reporting lines
- transfers
- promotions
- separations
- probation tracking
- notice period handling
- career structure
- grade and band structure
- organization assignment
- analytics

Important distinction:

Designation / Job Title
- the role name or classification
- example: Software Engineer, HR Executive, Finance Manager

Position
- the real approved seat in the organization
- example: Finance Manager - Muscat Branch - Position 001

Do NOT confuse designation and position.

Position should support:
- organization assignment
- headcount approval
- reporting structure
- vacancy visibility
- cost center linkage
- grade linkage

Design must support real enterprise HRMS usage.

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

All codes should be unique within tenant unless a more precise scoped uniqueness is justified.

----------------------------------------------------

ENTITY DEFINITIONS

1. DESIGNATION / JOB TITLE

Fields

id
tenant_id
designation_code
designation_name
short_name
job_family_id nullable
job_function_id nullable
description
active

Rules

designation_code unique per tenant
designation_name required
job_family_id and job_function_id must belong to same tenant


2. JOB FAMILY

Fields

id
tenant_id
job_family_code
job_family_name
description
active

Rules

job_family_code unique per tenant
job_family_name required


3. JOB FUNCTION

Fields

id
tenant_id
job_function_code
job_function_name
job_family_id nullable
description
active

Rules

job_function_code unique per tenant
job_function_name required
job_family_id must belong to same tenant when provided


4. GRADE BAND

Fields

id
tenant_id
grade_band_code
grade_band_name
band_order
description
active

Rules

grade_band_code unique per tenant
grade_band_name required
band_order must be zero or positive when provided


5. GRADE

Fields

id
tenant_id
grade_code
grade_name
grade_band_id nullable
ranking_order
salary_scale_min nullable
salary_scale_max nullable
description
active

Rules

grade_code unique per tenant
grade_name required
grade_band_id must belong to same tenant when provided
ranking_order must be zero or positive when provided
salary_scale_min <= salary_scale_max when both provided


6. POSITION

Fields

id
tenant_id
position_code
position_name
designation_id
job_family_id nullable
job_function_id nullable
grade_id
grade_band_id nullable
legal_entity_id nullable
branch_id nullable
business_unit_id nullable
division_id nullable
department_id nullable
section_id nullable
work_location_id nullable
cost_center_id nullable
reporting_unit_id nullable
reports_to_position_id nullable
approved_headcount
filled_headcount
vacancy_status
critical_position_flag
description
active

Vacancy status values

VACANT
PARTIALLY_FILLED
FILLED
FROZEN

Rules

position_code unique per tenant
position_name required
designation_id required
grade_id required
approved_headcount >= 0
filled_headcount >= 0
filled_headcount cannot exceed approved_headcount unless explicitly allowed by business rule comment
reports_to_position_id must belong to same tenant
prevent circular reporting position hierarchy
all linked org structure records must belong to same tenant


7. EMPLOYMENT TYPE

Fields

id
tenant_id
employment_type_code
employment_type_name
contract_required
description
active

Rules

employment_type_code unique per tenant
employment_type_name required


8. WORKER TYPE

Fields

id
tenant_id
worker_type_code
worker_type_name
description
active

Rules

worker_type_code unique per tenant
worker_type_name required


9. EMPLOYEE CATEGORY

Fields

id
tenant_id
employee_category_code
employee_category_name
description
active

Rules

employee_category_code unique per tenant
employee_category_name required


10. EMPLOYEE SUBCATEGORY

Fields

id
tenant_id
employee_subcategory_code
employee_subcategory_name
employee_category_id
description
active

Rules

employee_subcategory_code unique per tenant
employee_subcategory_name required
employee_category_id required
employee_category_id must belong to same tenant


11. CONTRACT TYPE

Fields

id
tenant_id
contract_type_code
contract_type_name
fixed_term_flag
default_duration_days nullable
renewal_allowed
description
active

Rules

contract_type_code unique per tenant
contract_type_name required
default_duration_days must be positive when provided


12. PROBATION POLICY

Fields

id
tenant_id
probation_policy_code
probation_policy_name
duration_days
extension_allowed
max_extension_days nullable
confirmation_required
description
active

Rules

probation_policy_code unique per tenant
probation_policy_name required
duration_days > 0
if extension_allowed = false then max_extension_days should be null or zero
if extension_allowed = true then max_extension_days should be zero or positive


13. NOTICE PERIOD POLICY

Fields

id
tenant_id
notice_policy_code
notice_policy_name
employee_notice_days
employer_notice_days
payment_in_lieu_allowed
garden_leave_allowed
description
active

Rules

notice_policy_code unique per tenant
notice_policy_name required
employee_notice_days >= 0
employer_notice_days >= 0


14. TRANSFER TYPE

Fields

id
tenant_id
transfer_type_code
transfer_type_name
description
active

Rules

transfer_type_code unique per tenant
transfer_type_name required


15. PROMOTION TYPE

Fields

id
tenant_id
promotion_type_code
promotion_type_name
description
active

Rules

promotion_type_code unique per tenant
promotion_type_name required


16. SEPARATION REASON

Fields

id
tenant_id
separation_reason_code
separation_reason_name
separation_category
voluntary_flag
final_settlement_required
description
active

Separation category values

RESIGNATION
TERMINATION
RETIREMENT
CONTRACT_EXPIRY
DEATH
ABSCONDING
OTHER

Rules

separation_reason_code unique per tenant
separation_reason_name required
separation_category required


17. AUDIT LOG

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
parent reassignment
grade band change
position reporting change
headcount change
policy rule change

----------------------------------------------------

DATABASE TABLES

Create tables:

designations
job_families
job_functions
grade_bands
grades
positions
employment_types
worker_types
employee_categories
employee_subcategories
contract_types
probation_policies
notice_period_policies
transfer_types
promotion_types
separation_reasons
audit_logs

Include:

indexes
foreign keys
tenant scoped unique constraints
check constraints where useful

Reference dependencies from previous steps:

Step 3 organization structure masters
- legal_entities
- branches
- business_units
- divisions
- departments
- sections
- work_locations
- cost_centers
- reporting_units

----------------------------------------------------

API ENDPOINTS

Designation

POST /api/job-architecture/designations
GET /api/job-architecture/designations
GET /api/job-architecture/designations/{id}
PUT /api/job-architecture/designations/{id}
PATCH /api/job-architecture/designations/{id}/status
GET /api/job-architecture/designations/options


Job Family

POST /api/job-architecture/job-families
GET /api/job-architecture/job-families
GET /api/job-architecture/job-families/{id}
PUT /api/job-architecture/job-families/{id}
PATCH /api/job-architecture/job-families/{id}/status
GET /api/job-architecture/job-families/options


Job Function

POST /api/job-architecture/job-functions
GET /api/job-architecture/job-functions
GET /api/job-architecture/job-functions/{id}
PUT /api/job-architecture/job-functions/{id}
PATCH /api/job-architecture/job-functions/{id}/status
GET /api/job-architecture/job-functions/options


Grade Band

POST /api/job-architecture/grade-bands
GET /api/job-architecture/grade-bands
GET /api/job-architecture/grade-bands/{id}
PUT /api/job-architecture/grade-bands/{id}
PATCH /api/job-architecture/grade-bands/{id}/status
GET /api/job-architecture/grade-bands/options


Grade

POST /api/job-architecture/grades
GET /api/job-architecture/grades
GET /api/job-architecture/grades/{id}
PUT /api/job-architecture/grades/{id}
PATCH /api/job-architecture/grades/{id}/status
GET /api/job-architecture/grades/options


Position

POST /api/job-architecture/positions
GET /api/job-architecture/positions
GET /api/job-architecture/positions/{id}
PUT /api/job-architecture/positions/{id}
PATCH /api/job-architecture/positions/{id}/status
GET /api/job-architecture/positions/options


Employment Type

POST /api/job-architecture/employment-types
GET /api/job-architecture/employment-types
GET /api/job-architecture/employment-types/{id}
PUT /api/job-architecture/employment-types/{id}
PATCH /api/job-architecture/employment-types/{id}/status
GET /api/job-architecture/employment-types/options


Worker Type

POST /api/job-architecture/worker-types
GET /api/job-architecture/worker-types
GET /api/job-architecture/worker-types/{id}
PUT /api/job-architecture/worker-types/{id}
PATCH /api/job-architecture/worker-types/{id}/status
GET /api/job-architecture/worker-types/options


Employee Category

POST /api/job-architecture/employee-categories
GET /api/job-architecture/employee-categories
GET /api/job-architecture/employee-categories/{id}
PUT /api/job-architecture/employee-categories/{id}
PATCH /api/job-architecture/employee-categories/{id}/status
GET /api/job-architecture/employee-categories/options


Employee Subcategory

POST /api/job-architecture/employee-subcategories
GET /api/job-architecture/employee-subcategories
GET /api/job-architecture/employee-subcategories/{id}
PUT /api/job-architecture/employee-subcategories/{id}
PATCH /api/job-architecture/employee-subcategories/{id}/status
GET /api/job-architecture/employee-subcategories/options


Contract Type

POST /api/job-architecture/contract-types
GET /api/job-architecture/contract-types
GET /api/job-architecture/contract-types/{id}
PUT /api/job-architecture/contract-types/{id}
PATCH /api/job-architecture/contract-types/{id}/status
GET /api/job-architecture/contract-types/options


Probation Policy

POST /api/job-architecture/probation-policies
GET /api/job-architecture/probation-policies
GET /api/job-architecture/probation-policies/{id}
PUT /api/job-architecture/probation-policies/{id}
PATCH /api/job-architecture/probation-policies/{id}/status
GET /api/job-architecture/probation-policies/options


Notice Period Policy

POST /api/job-architecture/notice-period-policies
GET /api/job-architecture/notice-period-policies
GET /api/job-architecture/notice-period-policies/{id}
PUT /api/job-architecture/notice-period-policies/{id}
PATCH /api/job-architecture/notice-period-policies/{id}/status
GET /api/job-architecture/notice-period-policies/options


Transfer Type

POST /api/job-architecture/transfer-types
GET /api/job-architecture/transfer-types
GET /api/job-architecture/transfer-types/{id}
PUT /api/job-architecture/transfer-types/{id}
PATCH /api/job-architecture/transfer-types/{id}/status
GET /api/job-architecture/transfer-types/options


Promotion Type

POST /api/job-architecture/promotion-types
GET /api/job-architecture/promotion-types
GET /api/job-architecture/promotion-types/{id}
PUT /api/job-architecture/promotion-types/{id}
PATCH /api/job-architecture/promotion-types/{id}/status
GET /api/job-architecture/promotion-types/options


Separation Reason

POST /api/job-architecture/separation-reasons
GET /api/job-architecture/separation-reasons
GET /api/job-architecture/separation-reasons/{id}
PUT /api/job-architecture/separation-reasons/{id}
PATCH /api/job-architecture/separation-reasons/{id}/status
GET /api/job-architecture/separation-reasons/options

----------------------------------------------------

LIST API REQUIREMENTS

All list APIs should support:

- tenant scoping
- pagination
- sorting
- search by code and name
- active/inactive filter

Additional filters:

Designation
- jobFamilyId
- jobFunctionId

Job Function
- jobFamilyId

Grade
- gradeBandId

Position
- designationId
- gradeId
- gradeBandId
- legalEntityId
- branchId
- departmentId
- costCenterId
- vacancyStatus
- criticalPositionFlag

Employee Subcategory
- employeeCategoryId

Options APIs should return active items by default.

----------------------------------------------------

VALIDATION REQUIREMENTS

Use Bean Validation and service-level validation.

Validate:

required fields
unique codes per tenant
same-tenant parent references
salary range correctness
positive durations where required
probation extension rule consistency
position headcount logic
no circular reports_to_position hierarchy
valid enums
position org references must all belong to same tenant
employee subcategory must belong to category in same tenant
designation family/function links must belong to same tenant

Add extension points/comments for future validations such as:
- cannot deactivate if already used by employee records
- cannot change critical structural fields after live employee assignment without checks

----------------------------------------------------

PACKAGE / MODULE STRUCTURE

Use clean modular package structure like:

jobarchitecture/designation/domain/entity
jobarchitecture/designation/domain/repository
jobarchitecture/designation/application/service
jobarchitecture/designation/application/dto/request
jobarchitecture/designation/application/dto/response
jobarchitecture/designation/application/mapper
jobarchitecture/designation/interfaces/rest

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
