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
Step 3 — Organization Structure Masters

These define the customer's enterprise structure.

Modules to implement:

1. Legal Entity
2. Branch
3. Business Unit
4. Division
5. Department
6. Section
7. Work Location
8. Cost Center
9. Reporting Unit

----------------------------------------------------

BUSINESS CONTEXT

These are tenant-owned organization masters.

Every record must belong to a tenant.

Employees must belong somewhere in the organization.

These modules will later support:

- employee assignment
- organization chart
- approval routing
- payroll grouping
- reporting
- cost allocation
- attendance / location control
- analytics

The hierarchy should remain flexible.

Do NOT enforce a rigid structure.

Example structure:

Tenant
  └── Legal Entity
        └── Branch
              └── Business Unit
                    └── Division
                          └── Department
                                └── Section

Parallel assignment dimensions:

Employee
  ├── Work Location
  ├── Cost Center
  └── Reporting Unit

Some tenants may only use:

Legal Entity → Branch → Department

Design must allow this.

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
- organization tree API

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

----------------------------------------------------

ENTITY DEFINITIONS

LEGAL ENTITY

Fields:

id
tenant_id
legal_entity_code
legal_entity_name
short_name
registration_no
tax_no
country_code
base_currency_code
default_language_code
contact_email
contact_phone
address_line1
address_line2
city
state
postal_code
active

Rules

legal_entity_code unique per tenant
country_code references Country
base_currency_code references Currency
default_language_code references Language


BRANCH

Fields

id
tenant_id
legal_entity_id
branch_code
branch_name
branch_short_name
address_line1
address_line2
city
state
country_code
postal_code
phone
fax
email
active

Rules

branch_code unique per tenant
legal_entity_id required


BUSINESS UNIT

Fields

id
tenant_id
legal_entity_id nullable
business_unit_code
business_unit_name
description
active

Rules

business_unit_code unique per tenant


DIVISION

Fields

id
tenant_id
legal_entity_id nullable
business_unit_id nullable
branch_id nullable
division_code
division_name
description
active

Rules

division_code unique per tenant


DEPARTMENT

Fields

id
tenant_id
legal_entity_id nullable
business_unit_id nullable
division_id nullable
branch_id nullable
department_code
department_name
short_name
description
active

Rules

department_code unique per tenant


SECTION

Fields

id
tenant_id
department_id
section_code
section_name
description
active

Rules

department_id required
section_code unique per tenant


WORK LOCATION

Fields

id
tenant_id
legal_entity_id nullable
branch_id nullable
location_code
location_name
location_type
address_line1
address_line2
city
state
country_code
postal_code
latitude
longitude
geofence_radius
active

Location types

OFFICE
SITE
PLANT
WAREHOUSE
REMOTE
CLIENT_SITE


COST CENTER

Fields

id
tenant_id
legal_entity_id nullable
cost_center_code
cost_center_name
description
gl_account_code
parent_cost_center_id
active

Rules

prevent circular hierarchy


REPORTING UNIT

Fields

id
tenant_id
reporting_unit_code
reporting_unit_name
parent_reporting_unit_id
description
active

Rules

prevent circular hierarchy


----------------------------------------------------

DATABASE TABLES

Create tables:

legal_entities
branches
business_units
divisions
departments
sections
work_locations
cost_centers
reporting_units
audit_logs

Include:

indexes
foreign keys
tenant scoped unique constraints

----------------------------------------------------

API ENDPOINTS

Legal Entity

POST /api/organization/legal-entities
GET /api/organization/legal-entities
GET /api/organization/legal-entities/{id}
PUT /api/organization/legal-entities/{id}
PATCH /api/organization/legal-entities/{id}/status
GET /api/organization/legal-entities/options


Branch

POST /api/organization/branches
GET /api/organization/branches
GET /api/organization/branches/{id}
PUT /api/organization/branches/{id}
PATCH /api/organization/branches/{id}/status
GET /api/organization/branches/options


Business Unit

POST /api/organization/business-units
GET /api/organization/business-units
GET /api/organization/business-units/{id}
PUT /api/organization/business-units/{id}
PATCH /api/organization/business-units/{id}/status
GET /api/organization/business-units/options


Division

POST /api/organization/divisions
GET /api/organization/divisions
GET /api/organization/divisions/{id}
PUT /api/organization/divisions/{id}
PATCH /api/organization/divisions/{id}/status
GET /api/organization/divisions/options


Department

POST /api/organization/departments
GET /api/organization/departments
GET /api/organization/departments/{id}
PUT /api/organization/departments/{id}
PATCH /api/organization/departments/{id}/status
GET /api/organization/departments/options


Section

POST /api/organization/sections
GET /api/organization/sections
GET /api/organization/sections/{id}
PUT /api/organization/sections/{id}
PATCH /api/organization/sections/{id}/status
GET /api/organization/sections/options


Work Location

POST /api/organization/work-locations
GET /api/organization/work-locations
GET /api/organization/work-locations/{id}
PUT /api/organization/work-locations/{id}
PATCH /api/organization/work-locations/{id}/status
GET /api/organization/work-locations/options


Cost Center

POST /api/organization/cost-centers
GET /api/organization/cost-centers
GET /api/organization/cost-centers/{id}
PUT /api/organization/cost-centers/{id}
PATCH /api/organization/cost-centers/{id}/status
GET /api/organization/cost-centers/options


Reporting Unit

POST /api/organization/reporting-units
GET /api/organization/reporting-units
GET /api/organization/reporting-units/{id}
PUT /api/organization/reporting-units/{id}
PATCH /api/organization/reporting-units/{id}/status
GET /api/organization/reporting-units/options


Additional APIs

GET /api/organization/tree
GET /api/organization/chart

----------------------------------------------------

VALIDATION REQUIREMENTS

Use Bean Validation.

Validate

required fields
unique codes per tenant
email format
phone format
latitude longitude ranges
geofence radius >= 0
parent-child same tenant
no circular cost center hierarchy
no circular reporting unit hierarchy

----------------------------------------------------

OUTPUT FORMAT REQUIRED

Return answer in this order

1 package structure
2 database schema
3 Flyway migrations
4 entity classes
5 DTO classes
6 repositories
7 services
8 mappers
9 controllers
10 validation rules
11 exception handling
12 audit logging
13 organization tree response design
14 sample JSON requests
15 future extension notes

Produce production-ready code.
