# HRMS SaaS Master Specification - Phase 1

## Purpose

This document is a developer-ready specification for Phase 1 master modules of a multi-country, multi-tenant HRMS SaaS product. It is intended for backend and frontend implementation teams.

## Design Principles

* Every master is tenant-aware unless explicitly global.
* Support soft delete and activate/deactivate instead of hard delete.
* Support effective dating where business rules may change over time.
* Keep audit logs for every create, update, activate, deactivate action.
* Never allow cross-tenant foreign key references.
* Use code + name for business-friendly lookups.
* Support pagination, filtering, and export on list APIs.

## Common Technical Standards

### Standard Columns for Tenant-Scoped Masters

* `id` UUID PK
* `tenant_id` UUID not null
* `code` varchar(50) not null
* `name` varchar(200) not null
* `description` varchar(1000) null
* `effective_from` date null
* `effective_to` date null
* `is_active` boolean not null default true
* `is_system_defined` boolean not null default false
* `created_at` timestamp not null
* `created_by` UUID/String not null
* `updated_at` timestamp not null
* `updated_by` UUID/String not null
* `version_no` integer not null default 1

### Standard APIs

* `POST /api/v1/masters/{entity}`
* `PUT /api/v1/masters/{entity}/{id}`
* `GET /api/v1/masters/{entity}/{id}`
* `POST /api/v1/masters/{entity}/search`
* `PATCH /api/v1/masters/{entity}/{id}/activate`
* `PATCH /api/v1/masters/{entity}/{id}/deactivate`
* `GET /api/v1/masters/{entity}/{id}/audit`

### Standard Search Request

```json
{
  "page": 0,
  "size": 20,
  "sort": [{"field": "updatedAt", "direction": "DESC"}],
  "filters": {
    "code": null,
    "name": null,
    "isActive": true
  }
}
```

### Standard Search Response

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

---

# 1. Tenant Master

## Business Purpose

Represents one customer account in the SaaS platform. All tenant-scoped business data belongs to one tenant.

## Table Name

`plat_tenant`

## Columns

* `id` UUID PK
* `tenant_code` varchar(50) unique not null
* `tenant_name` varchar(200) not null
* `status` varchar(30) not null
* `subscription_plan_id` UUID null
* `default_country_id` UUID null
* `default_currency_id` UUID null
* `default_language_id` UUID null
* `timezone_code` varchar(60) null
* `data_region` varchar(50) null
* `created_at`, `created_by`, `updated_at`, `updated_by`, `version_no`

## Unique Keys

* `uk_plat_tenant_code (tenant_code)`

## Foreign Keys

* `subscription_plan_id -> plat_subscription_plan.id`
* `default_country_id -> ref_country.id`
* `default_currency_id -> ref_currency.id`
* `default_language_id -> ref_language.id`

## Screen

### Tenant List

Columns: code, name, status, default country, plan, last updated

### Tenant Create/Edit

Sections:

1. Basic Info
2. Regional Defaults
3. Subscription
4. Status

Fields:

* tenant code
* tenant name
* status: DRAFT, ACTIVE, INACTIVE, SUSPENDED
* default country
* default currency
* default language
* timezone
* data region
* subscription plan

## Validations

* tenant code must be globally unique
* inactive/suspended tenant cannot log in
* tenant cannot be deleted once business data exists

## APIs

### Create

`POST /api/v1/platform/tenants`

```json
{
  "tenantCode": "ALSAWARI",
  "tenantName": "Al Sawari International Investment Company",
  "status": "ACTIVE",
  "defaultCountryId": "uuid",
  "defaultCurrencyId": "uuid",
  "defaultLanguageId": "uuid",
  "timezoneCode": "Asia/Muscat",
  "dataRegion": "OMAN"
}
```

### Get

`GET /api/v1/platform/tenants/{id}`

### Update

`PUT /api/v1/platform/tenants/{id}`

---

# 2. Country Master

## Business Purpose

Defines countries used across employees, legal entities, compliance rules, holiday calendars, and payroll localization.

## Table Name

`ref_country`

## Scope

Global reference master

## Columns

* `id` UUID PK
* `country_code` varchar(10) unique not null
* `iso2_code` varchar(2) unique not null
* `iso3_code` varchar(3) unique not null
* `country_name` varchar(150) not null
* `dialing_code` varchar(10) null
* `default_currency_id` UUID null
* `default_language_id` UUID null
* `default_timezone` varchar(60) null
* `is_gcc` boolean default false
* `is_active` boolean default true
* `created_at`, `created_by`, `updated_at`, `updated_by`, `version_no`

## Screen

### Country List

Columns: code, name, ISO2, ISO3, currency, timezone, active

### Country Create/Edit

Fields:

* country code
* ISO2
* ISO3
* country name
* dialing code
* default currency
* default language
* default timezone
* GCC flag
* active

## Validations

* ISO2 unique
* ISO3 unique
* country code unique
* cannot deactivate if referenced by active legal entities unless override allowed

## APIs

* `POST /api/v1/masters/countries`
* `GET /api/v1/masters/countries/{id}`
* `PUT /api/v1/masters/countries/{id}`
* `POST /api/v1/masters/countries/search`

---

# 3. Currency Master

## Business Purpose

Used in salary structures, payroll processing, expense claims, and country defaults.

## Table Name

`ref_currency`

## Scope

Global reference master

## Columns

* `id` UUID PK
* `currency_code` varchar(3) unique not null
* `currency_name` varchar(100) not null
* `symbol` varchar(10) null
* `decimal_places` integer not null default 2
* `is_active` boolean default true
* audit fields

## Screen

Fields:

* code
* name
* symbol
* decimal places
* active

## Validations

* ISO currency code unique
* decimal places between 0 and 6

---

# 4. Language Master

## Business Purpose

Controls UI languages and translation support.

## Table Name

`ref_language`

## Scope

Global reference master

## Columns

* `id` UUID PK
* `language_code` varchar(10) unique not null
* `language_name` varchar(100) not null
* `is_rtl` boolean default false
* `is_active` boolean default true
* audit fields

## Screen

Fields:

* language code
* language name
* right-to-left flag
* active

## Validations

* code unique

---

# 5. Legal Entity Master

## Business Purpose

Represents a company or registered entity under a tenant. Payroll, compliance, and bank setup are usually legal-entity specific.

## Table Name

`org_legal_entity`

## Columns

* common tenant-scoped columns
* `country_id` UUID not null
* `registration_no` varchar(100) null
* `tax_registration_no` varchar(100) null
* `default_currency_id` UUID null
* `default_language_id` UUID null
* `address_line_1` varchar(250) null
* `address_line_2` varchar(250) null
* `city` varchar(100) null
* `state_region` varchar(100) null
* `postal_code` varchar(30) null
* `phone_no` varchar(30) null
* `email` varchar(150) null

## Unique Keys

* unique `(tenant_id, code)`

## Foreign Keys

* `country_id -> ref_country.id`
* `default_currency_id -> ref_currency.id`
* `default_language_id -> ref_language.id`

## Screen

### Legal Entity List

Columns: code, name, country, registration no, active

### Create/Edit

Sections:

1. Basic Info
2. Registration
3. Defaults
4. Address

Fields:

* code
* name
* description
* country
* registration number
* tax registration number
* default currency
* default language
* effective dates
* address fields
* status

## Validations

* code unique within tenant
* country mandatory
* if inactive, warn when active branches or employees exist

## APIs

### Create Request

```json
{
  "code": "ASI-OM",
  "name": "Al Sawari Oman",
  "countryId": "uuid",
  "registrationNo": "CR12345",
  "taxRegistrationNo": "TAX8899",
  "defaultCurrencyId": "uuid",
  "defaultLanguageId": "uuid",
  "effectiveFrom": "2026-04-01",
  "addressLine1": "Muscat",
  "isActive": true
}
```

---

# 6. Branch Master

## Business Purpose

Represents branch or operating location within a legal entity.

## Table Name

`org_branch`

## Columns

* common tenant-scoped columns
* `legal_entity_id` UUID not null
* `country_id` UUID not null
* `short_name` varchar(100) null
* `address_line_1` varchar(250) null
* `address_line_2` varchar(250) null
* `city` varchar(100) null
* `phone_no` varchar(30) null
* `fax_no` varchar(30) null

## Unique Keys

* unique `(tenant_id, legal_entity_id, code)`

## Foreign Keys

* `legal_entity_id -> org_legal_entity.id`
* `country_id -> ref_country.id`

## Screen

Fields:

* legal entity
* branch code
* branch name
* short name
* country
* address
* phone
* fax
* active

## Validations

* branch code unique within legal entity
* country should normally match legal entity country unless multi-country entity support is intentional
* cannot delete if employees, departments, or positions exist

## Sample Create Request

```json
{
  "code": "MCT-HQ",
  "name": "Muscat Head Office",
  "shortName": "MCT HQ",
  "legalEntityId": "uuid",
  "countryId": "uuid",
  "addressLine1": "Muscat",
  "phoneNo": "+96812345678",
  "isActive": true
}
```

---

# 7. Department Master

## Business Purpose

Represents business departments used in employee assignment, workflow routing, reporting, and payroll analytics.

## Table Name

`org_department`

## Columns

* common tenant-scoped columns
* `legal_entity_id` UUID not null
* `branch_id` UUID null
* `parent_department_id` UUID null
* `manager_position_id` UUID null

## Unique Keys

* unique `(tenant_id, legal_entity_id, code)`

## Foreign Keys

* `legal_entity_id -> org_legal_entity.id`
* `branch_id -> org_branch.id`
* `parent_department_id -> org_department.id`

## Screen

Fields:

* legal entity
* branch optional
* department code
* department name
* parent department
* manager position optional
* effective dates
* active

## Validations

* no circular parent hierarchy
* branch must belong to same legal entity
* manager position must belong to same tenant

## APIs

* standard CRUD/search/audit

---

# 8. Designation Master

## Business Purpose

Represents generic job titles such as HR Manager, Payroll Officer, Software Engineer.

## Table Name

`org_designation`

## Columns

* common tenant-scoped columns
* `job_family` varchar(100) null
* `job_level` varchar(50) null

## Unique Keys

* unique `(tenant_id, code)`

## Screen

Fields:

* code
* name
* description
* job family
* job level
* active

## Validations

* designation code unique within tenant
* do not mix this with actual approved position slots

---

# 9. Grade Master

## Business Purpose

Used for payroll structures, leave eligibility, salary banding, and career levels.

## Table Name

`org_grade`

## Columns

* common tenant-scoped columns
* `level_no` integer null
* `salary_band_min` decimal(18,3) null
* `salary_band_max` decimal(18,3) null
* `currency_id` UUID null

## Unique Keys

* unique `(tenant_id, code)`

## Foreign Keys

* `currency_id -> ref_currency.id`

## Screen

Fields:

* grade code
* grade name
* level number
* salary band min
* salary band max
* currency
* active

## Validations

* if both min and max provided, min <= max
* currency required when salary bands are provided

## Notes

This master will later link to grade leave policy and grade salary structure.

---

# 10. Employee Category Master

## Business Purpose

Represents business classification of employees such as Staff, Labor, Management, Local National, Expatriate Group.

## Table Name

`emp_category`

## Columns

* common tenant-scoped columns
* `category_type` varchar(50) null

## Unique Keys

* unique `(tenant_id, code)`

## Screen

Fields:

* code
* name
* category type
* description
* active

## Validations

* code unique within tenant
* keep separate from nationality and employment type

---

# 11. Document Type Master

## Business Purpose

Defines which document types exist in the system and what metadata and alerts they require.

## Table Name

`ref_document_type`

## Columns

* common tenant-scoped columns, except may be global or tenant-scoped based on design
* `country_id` UUID null
* `applies_to` varchar(30) not null
* `requires_issue_date` boolean default false
* `requires_expiry_date` boolean default false
* `requires_attachment` boolean default false
* `alert_required` boolean default false
* `alert_before_days` integer null

## Unique Keys

* unique `(tenant_id, code, country_id)` when tenant-scoped

## Screen

Fields:

* code
* name
* country optional
* applies to: EMPLOYEE, DEPENDENT, BOTH, EMPLOYER
* issue date required
* expiry date required
* attachment required
* alert required
* alert before days
* active

## Validations

* if alert required then alert before days must be > 0
* if expiry date not required then alert cannot be required

## Sample Create Request

```json
{
  "code": "PASSPORT",
  "name": "Passport",
  "countryId": null,
  "appliesTo": "EMPLOYEE",
  "requiresIssueDate": true,
  "requiresExpiryDate": true,
  "requiresAttachment": true,
  "alertRequired": true,
  "alertBeforeDays": 30,
  "isActive": true
}
```

---

# 12. Leave Type Master

## Business Purpose

Defines leave categories and their policy flags. This is one of the most important HR/payroll masters.

## Table Name

`lv_leave_type`

## Columns

* common tenant-scoped columns
* `country_id` UUID null
* `legal_entity_id` UUID null
* `leave_category` varchar(50) not null
* `is_paid` boolean not null
* `pay_percentage` decimal(5,2) not null default 0
* `is_carry_forward` boolean default false
* `is_attachment_required` boolean default false
* `is_encashable` boolean default false
* `is_negative_allowed` boolean default false
* `is_annual_leave` boolean default false
* `eligible_after_days` integer null
* `allowed_gender` varchar(20) null
* `allowed_religion` varchar(50) null
* `allowed_nationality_type` varchar(30) null
* `max_times_per_period` integer null

## Unique Keys

* unique `(tenant_id, legal_entity_id, country_id, code)`

## Foreign Keys

* `country_id -> ref_country.id`
* `legal_entity_id -> org_legal_entity.id`

## Screen

Sections:

1. Basic Info
2. Payment Rules
3. Eligibility Rules
4. Advanced Rules

Fields:

* code
* name
* country optional
* legal entity optional
* leave category
* paid/unpaid
* percentage paid
* annual leave flag
* carry forward
* encashable
* negative allowed
* attachment required
* eligible after days
* gender restriction
* religion restriction
* nationality restriction
* max times per period
* active

## Validations

* if is_paid = false then pay percentage must be 0
* pay percentage between 0 and 100
* if encashable = true, later encashment policy should exist
* legal entity and country must belong to same tenant

## Sample Create Request

```json
{
  "code": "ANNUAL",
  "name": "Annual Leave",
  "countryId": "uuid",
  "legalEntityId": null,
  "leaveCategory": "VACATION",
  "isPaid": true,
  "payPercentage": 100,
  "isCarryForward": true,
  "isEncashable": true,
  "isNegativeAllowed": false,
  "isAnnualLeave": true,
  "eligibleAfterDays": 180,
  "isActive": true
}
```

---

# 13. Shift Master

## Business Purpose

Defines working shifts for attendance, timesheet, and payroll overtime calculations.

## Table Name

`att_shift`

## Columns

* common tenant-scoped columns
* `legal_entity_id` UUID null
* `branch_id` UUID null
* `start_time` time not null
* `end_time` time not null
* `break_minutes` integer default 0
* `expected_work_hours` decimal(5,2) not null
* `late_grace_minutes` integer default 0
* `early_out_grace_minutes` integer default 0
* `is_night_shift` boolean default false

## Unique Keys

* unique `(tenant_id, legal_entity_id, branch_id, code)`

## Screen

Fields:

* legal entity optional
* branch optional
* shift code
* shift name
* start time
* end time
* break minutes
* expected work hours
* late grace minutes
* early out grace minutes
* night shift flag
* active

## Validations

* expected work hours > 0
* start/end time must result in valid duration, support overnight shifts
* branch must belong to same legal entity when both specified

---

# 14. Payroll Group Master

## Business Purpose

Groups employees for payroll processing by company, frequency, category, or population.

## Table Name

`pay_payroll_group`

## Columns

* common tenant-scoped columns
* `legal_entity_id` UUID not null
* `payroll_frequency` varchar(20) not null
* `cutoff_day` integer null
* `pay_day` integer null
* `currency_id` UUID null

## Unique Keys

* unique `(tenant_id, legal_entity_id, code)`

## Foreign Keys

* `legal_entity_id -> org_legal_entity.id`
* `currency_id -> ref_currency.id`

## Screen

Fields:

* legal entity
* code
* name
* payroll frequency: MONTHLY, BI_WEEKLY, WEEKLY, ADHOC
* cutoff day
* pay day
* currency
* active

## Validations

* pay day and cutoff day should be between 1 and 31 when monthly
* frequency mandatory

---

# 15. Salary Component Group Master

## Business Purpose

Logical grouping for salary components, such as ALLOWANCE, DEDUCTION, OVERTIME, CONTRIBUTION.

## Table Name

`pay_component_group`

## Columns

* common tenant-scoped columns
* `group_type` varchar(30) not null

## Screen

Fields:

* code
* name
* group type
* description
* active

## Validations

* group type values controlled by reference list

---

# 16. Salary Component Master

## Business Purpose

Defines earnings, deductions, and employer contributions used in payroll structures and payroll runs.

## Table Name

`pay_component`

## Columns

* common tenant-scoped columns
* `legal_entity_id` UUID null
* `country_id` UUID null
* `component_nature` varchar(30) not null
* `component_group_id` UUID not null
* `formula_id` UUID null
* `calculation_basis` varchar(30) null
* `include_in_gross` boolean default false
* `include_in_net` boolean default true
* `include_in_gratuity` boolean default false
* `include_in_leave_salary` boolean default false
* `sort_order` integer default 0

## Unique Keys

* unique `(tenant_id, legal_entity_id, country_id, code)`

## Foreign Keys

* `component_group_id -> pay_component_group.id`
* `formula_id -> pay_formula.id`
* `legal_entity_id -> org_legal_entity.id`
* `country_id -> ref_country.id`

## Screen

Sections:

1. Basic Info
2. Calculation
3. Flags
4. Applicability

Fields:

* code
* name
* component nature: EARNING, DEDUCTION, EMPLOYER_CONTRIBUTION
* group
* formula optional
* calculation basis
* include in gross
* include in net
* include in gratuity
* include in leave salary
* sort order
* country optional
* legal entity optional
* active

## Validations

* component nature mandatory
* formula required when basis is FORMULA
* sort order >= 0

## Sample Create Request

```json
{
  "code": "BASIC",
  "name": "Basic Salary",
  "componentNature": "EARNING",
  "componentGroupId": "uuid",
  "calculationBasis": "FIXED",
  "includeInGross": true,
  "includeInNet": true,
  "includeInGratuity": true,
  "includeInLeaveSalary": true,
  "sortOrder": 10,
  "isActive": true
}
```

---

# 17. Formula Master

## Business Purpose

Stores payroll calculation formulas in a safe configurable way.

## Table Name

`pay_formula`

## Columns

* common tenant-scoped columns
* `formula_type` varchar(30) not null
* `expression_text` varchar(2000) not null
* `expression_json` text null
* `rounding_mode` varchar(30) null
* `decimal_places` integer null

## Unique Keys

* unique `(tenant_id, code)`

## Screen

Fields:

* code
* name
* formula type
* expression text
* rounding mode
* decimal places
* active

## Validations

* expression required
* expression must be validated by formula parser before save
* no unsafe script execution allowed

## Technical Note

Use a controlled expression parser, not raw script eval.

---

# 18. Salary Structure Master

## Business Purpose

Defines a reusable salary package template containing one or more salary components.

## Table Name

`pay_salary_structure`

## Parent Columns

* common tenant-scoped columns
* `legal_entity_id` UUID null
* `country_id` UUID null
* `grade_id` UUID null
* `currency_id` UUID not null

## Child Table

`pay_salary_structure_line`

* `id` UUID PK
* `salary_structure_id` UUID not null
* `component_id` UUID not null
* `line_type` varchar(20) not null
* `amount` decimal(18,3) null
* `formula_id` UUID null
* `sequence_no` integer not null
* `is_active` boolean default true

## Unique Keys

* parent unique `(tenant_id, code)`
* child unique `(salary_structure_id, component_id, sequence_no)`

## Screen

### Header Fields

* structure code
* structure name
* country optional
* legal entity optional
* grade optional
* currency
* effective dates
* active

### Line Grid

* sequence
* component
* line type: FIXED, FORMULA
* amount
* formula

## Validations

* at least one line required
* FIXED line requires amount
* FORMULA line requires formula id
* component cannot repeat unless allowed by sequence-based logic

## Sample Create Request

```json
{
  "code": "G5-STD-OM",
  "name": "Grade 5 Standard Oman",
  "countryId": "uuid",
  "currencyId": "uuid",
  "gradeId": "uuid",
  "lines": [
    {
      "sequenceNo": 1,
      "componentId": "uuid-basic",
      "lineType": "FIXED",
      "amount": 500
    },
    {
      "sequenceNo": 2,
      "componentId": "uuid-housing",
      "lineType": "FIXED",
      "amount": 200
    }
  ]
}
```

---

# 19. Bank Master

## Business Purpose

Used for company bank setup, employee bank details, salary transfer, and WPS.

## Table Name

`fin_bank`

## Columns

* common tenant-scoped columns, though may also be global by country
* `country_id` UUID not null
* `swift_code` varchar(20) null
* `supports_wps` boolean default false

## Unique Keys

* unique `(tenant_id, country_id, code)`

## Screen

Fields:

* country
* bank code
* bank name
* swift code
* supports WPS
* active

## Validations

* country mandatory
* code unique within country and tenant

---

# 20. Loan Type Master

## Business Purpose

Defines loan/advance products available to employees.

## Table Name

`pay_loan_type`

## Columns

* common tenant-scoped columns
* `legal_entity_id` UUID null
* `interest_method` varchar(30) null
* `interest_rate` decimal(8,4) null
* `max_amount` decimal(18,3) null
* `max_tenure_months` integer null
* `recovery_start_rule` varchar(30) null

## Unique Keys

* unique `(tenant_id, legal_entity_id, code)`

## Screen

Fields:

* legal entity optional
* code
* name
* interest method: NONE, FLAT, REDUCING
* interest rate
* max amount
* max tenure months
* recovery start rule
* active

## Validations

* if interest method != NONE then interest rate required
* max amount > 0 when provided
* max tenure months > 0 when provided

## Sample Create Request

```json
{
  "code": "CAR_LOAN",
  "name": "Car Loan",
  "legalEntityId": "uuid",
  "interestMethod": "FLAT",
  "interestRate": 4.5,
  "maxAmount": 5000,
  "maxTenureMonths": 24,
  "recoveryStartRule": "NEXT_PAYROLL",
  "isActive": true
}
```

---

# Frontend Build Checklist

For all Phase 1 masters, implement:

* list page with filters
* create page
* edit page
* view page
* activate/deactivate actions
* validation messages
* audit history drawer/tab
* lookup dropdowns with search

# Backend Build Checklist

For all Phase 1 masters, implement:

* request DTOs
* response DTOs
* repository layer
* service layer with validation
* audit log publishing
* search specification/filter builder
* optimistic locking using `version_no`
* soft delete or deactivate only

# Security Rules

* tenant users can only access their tenant data
* platform admin endpoints must be separated from tenant admin endpoints
* role-based permission checks required for create/update/activate actions

# Recommended Next Phase

After Phase 1, build these next:

* Holiday Calendar
* Overtime Type
* Employee Status
* Employment Type
* Position
* Workflow Definition
* Notification Template
* Social Insurance Scheme
* WPS File Format
