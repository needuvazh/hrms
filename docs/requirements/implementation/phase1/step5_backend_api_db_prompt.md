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

This platform supports enterprise HR operations across multiple countries with strong compliance requirements.

----------------------------------------------------

MODULE
Step 5 — Compliance-supporting HR Masters

These are HR-side compliance classification masters.  
They support compliance and document tracking but DO NOT implement payroll calculations.

Modules to implement:

1. Visa Type
2. Residence Status
3. Labour Card Type
4. Civil ID Type
5. Passport Type
6. Sponsor Type
7. Work Permit Type
8. Nationalisation Category
9. Social Insurance Eligibility Type
10. Beneficiary Type
11. Dependent Type

----------------------------------------------------

BUSINESS CONTEXT

These masters support compliance classification for employees and dependents.

They directly support:

- Omani national vs expatriate categorisation
- PASI-linked employee classification
- visa and work permit tracking
- labour card tracking
- passport and civil ID tracking
- immigration and residency compliance
- dependent management
- beneficiary designation for insurance or EOSB
- document expiry monitoring
- government reporting segmentation
- Omanisation tracking

These masters are used by:

- Employee master
- Employee immigration records
- Employee document vault
- Dependent records
- Beneficiary records
- Compliance reporting
- HR analytics

These modules are NOT responsible for:

- payroll calculation logic
- contribution calculation
- statutory payment processing

They only define classifications used by later modules.

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

tenant_id  
created_at  
updated_at  
created_by  
updated_by  
active flag

Use soft delete via active flag.

All codes must be unique within tenant.

----------------------------------------------------

ENTITY DEFINITIONS

1. VISA TYPE

Fields

id  
tenant_id  
visa_type_code  
visa_type_name  
visa_category  
applies_to  
renewable_flag  
description  
active  

applies_to values

EMPLOYEE  
DEPENDENT  
BOTH  

Rules

visa_type_code unique per tenant  
visa_type_name required  


2. RESIDENCE STATUS

Fields

id  
tenant_id  
residence_status_code  
residence_status_name  
description  
active  

Rules

residence_status_code unique per tenant  
residence_status_name required  


3. LABOUR CARD TYPE

Fields

id  
tenant_id  
labour_card_type_code  
labour_card_type_name  
expiry_tracking_required  
description  
active  

Rules

labour_card_type_code unique per tenant  
labour_card_type_name required  


4. CIVIL ID TYPE

Fields

id  
tenant_id  
civil_id_type_code  
civil_id_type_name  
applies_to  
expiry_tracking_required  
description  
active  

applies_to values

OMANI  
EXPATRIATE  
BOTH  

Rules

civil_id_type_code unique per tenant  
civil_id_type_name required  


5. PASSPORT TYPE

Fields

id  
tenant_id  
passport_type_code  
passport_type_name  
description  
active  

Rules

passport_type_code unique per tenant  
passport_type_name required  


6. SPONSOR TYPE

Fields

id  
tenant_id  
sponsor_type_code  
sponsor_type_name  
applies_to  
description  
active  

applies_to values

EMPLOYEE  
DEPENDENT  
BOTH  

Rules

sponsor_type_code unique per tenant  
sponsor_type_name required  


7. WORK PERMIT TYPE

Fields

id  
tenant_id  
work_permit_type_code  
work_permit_type_name  
renewable_flag  
description  
active  

Rules

work_permit_type_code unique per tenant  
work_permit_type_name required  


8. NATIONALISATION CATEGORY

Fields

id  
tenant_id  
nationalisation_category_code  
nationalisation_category_name  
omani_flag  
counts_for_omanisation_flag  
description  
active  

Rules

nationalisation_category_code unique per tenant  
nationalisation_category_name required  


9. SOCIAL INSURANCE ELIGIBILITY TYPE

Fields

id  
tenant_id  
social_insurance_type_code  
social_insurance_type_name  
pension_eligible_flag  
occupational_hazard_eligible_flag  
govt_contribution_applicable_flag  
description  
active  

Rules

social_insurance_type_code unique per tenant  
social_insurance_type_name required  


10. BENEFICIARY TYPE

Fields

id  
tenant_id  
beneficiary_type_code  
beneficiary_type_name  
priority_order  
description  
active  

Rules

beneficiary_type_code unique per tenant  
beneficiary_type_name required  
priority_order >= 0  


11. DEPENDENT TYPE

Fields

id  
tenant_id  
dependent_type_code  
dependent_type_name  
insurance_eligible_flag  
family_visa_eligible_flag  
description  
active  

Rules

dependent_type_code unique per tenant  
dependent_type_name required  


----------------------------------------------------

DATABASE TABLES

Create tables:

visa_types  
residence_statuses  
labour_card_types  
civil_id_types  
passport_types  
sponsor_types  
work_permit_types  
nationalisation_categories  
social_insurance_eligibility_types  
beneficiary_types  
dependent_types  
audit_logs  

Include:

indexes  
tenant scoped unique constraints  
check constraints where applicable  

----------------------------------------------------

API ENDPOINTS

Visa Type

POST /api/compliance/visa-types  
GET /api/compliance/visa-types  
GET /api/compliance/visa-types/{id}  
PUT /api/compliance/visa-types/{id}  
PATCH /api/compliance/visa-types/{id}/status  
GET /api/compliance/visa-types/options  


Residence Status

POST /api/compliance/residence-statuses  
GET /api/compliance/residence-statuses  
GET /api/compliance/residence-statuses/{id}  
PUT /api/compliance/residence-statuses/{id}  
PATCH /api/compliance/residence-statuses/{id}/status  
GET /api/compliance/residence-statuses/options  


Labour Card Type

POST /api/compliance/labour-card-types  
GET /api/compliance/labour-card-types  
GET /api/compliance/labour-card-types/{id}  
PUT /api/compliance/labour-card-types/{id}  
PATCH /api/compliance/labour-card-types/{id}/status  
GET /api/compliance/labour-card-types/options  


Civil ID Type

POST /api/compliance/civil-id-types  
GET /api/compliance/civil-id-types  
GET /api/compliance/civil-id-types/{id}  
PUT /api/compliance/civil-id-types/{id}  
PATCH /api/compliance/civil-id-types/{id}/status  
GET /api/compliance/civil-id-types/options  


Passport Type

POST /api/compliance/passport-types  
GET /api/compliance/passport-types  
GET /api/compliance/passport-types/{id}  
PUT /api/compliance/passport-types/{id}  
PATCH /api/compliance/passport-types/{id}/status  
GET /api/compliance/passport-types/options  


Sponsor Type

POST /api/compliance/sponsor-types  
GET /api/compliance/sponsor-types  
GET /api/compliance/sponsor-types/{id}  
PUT /api/compliance/sponsor-types/{id}  
PATCH /api/compliance/sponsor-types/{id}/status  
GET /api/compliance/sponsor-types/options  


Work Permit Type

POST /api/compliance/work-permit-types  
GET /api/compliance/work-permit-types  
GET /api/compliance/work-permit-types/{id}  
PUT /api/compliance/work-permit-types/{id}  
PATCH /api/compliance/work-permit-types/{id}/status  
GET /api/compliance/work-permit-types/options  


Nationalisation Category

POST /api/compliance/nationalisation-categories  
GET /api/compliance/nationalisation-categories  
GET /api/compliance/nationalisation-categories/{id}  
PUT /api/compliance/nationalisation-categories/{id}  
PATCH /api/compliance/nationalisation-categories/{id}/status  
GET /api/compliance/nationalisation-categories/options  


Social Insurance Eligibility Type

POST /api/compliance/social-insurance-types  
GET /api/compliance/social-insurance-types  
GET /api/compliance/social-insurance-types/{id}  
PUT /api/compliance/social-insurance-types/{id}  
PATCH /api/compliance/social-insurance-types/{id}/status  
GET /api/compliance/social-insurance-types/options  


Beneficiary Type

POST /api/compliance/beneficiary-types  
GET /api/compliance/beneficiary-types  
GET /api/compliance/beneficiary-types/{id}  
PUT /api/compliance/beneficiary-types/{id}  
PATCH /api/compliance/beneficiary-types/{id}/status  
GET /api/compliance/beneficiary-types/options  


Dependent Type

POST /api/compliance/dependent-types  
GET /api/compliance/dependent-types  
GET /api/compliance/dependent-types/{id}  
PUT /api/compliance/dependent-types/{id}  
PATCH /api/compliance/dependent-types/{id}/status  
GET /api/compliance/dependent-types/options  

----------------------------------------------------

VALIDATION REQUIREMENTS

Validate:

required code  
required name  
code uniqueness per tenant  
valid enum values  
priority order >= 0  
no duplicate code within tenant  

----------------------------------------------------

OUTPUT FORMAT

Return answer in this order:

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
13 sample JSON requests and responses  
14 option API response samples  
15 future extension notes  

Produce production-ready code.
