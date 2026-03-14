# master-data module

## Purpose
Provides master-data capabilities for the HRMS modular monolith.

## Package layout
- api: internal contracts for other modules
- application: use-case orchestration
- domain: core business model
- infrastructure: adapters (R2DBC/Web)
- config: Spring wiring
- events: module event models

## Notes
- Tenant context is required for all business operations.
- Cross-module usage must go through this module's api package.

## Multi-country master tables
- Geography and locale: `currency_master`, `language_master`, `timezone_master`, `country_locale_map`, `nationality_master`
- Organization dimensions: `business_unit_master`, `department_master`, `designation_master`, `grade_master`, `job_family_master`, `job_level_master`, `location_master`, `cost_center_master`, `org_unit_master`
- Workforce and recruitment catalogs: `employment_type_master`, `contract_type_master`, `worker_category_master`, `probation_policy_class_master`, `application_source_master`, `hiring_stage_master`, `interview_type_master`, `offer_status_master`, `rejection_reason_master`
- Payroll catalogs: `pay_component_master`, `deduction_type_master`, `benefit_type_master`, `tax_category_master`
- Shared governance catalogs: `status_master`, `reason_code_master`, `document_type_master`

Each table supports tenant and country scope, effective dating, activation, and versioning for audit-safe historical reporting.
