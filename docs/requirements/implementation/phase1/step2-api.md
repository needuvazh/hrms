You are a senior enterprise SaaS HRMS solution architect and senior Spring Boot developer.

I am building a multi-tenant enterprise HRMS product using Java, Spring Boot, Gradle multi-module architecture, PostgreSQL, JPA/Hibernate, Flyway, Bean Validation, Lombok, MapStruct, and REST APIs.

I want you to design and generate the backend for:

Step 2 — Global Reference Masters

These are reusable global masters across the whole system.

--------------------------------------------------
SCOPE
--------------------------------------------------

Create backend design and implementation for these modules:

1. Country
2. Currency
3. Language
4. Nationality
5. Religion
6. Gender
7. Marital Status
8. Relationship Type
9. Document Type
10. Education Level
11. Certification Type
12. Skill Category
13. Skill

Important business context:
- These are global platform reference masters, not tenant-owned business masters
- They will be reused across employee profile, dependent details, education, certifications, documents, training, recruitment, ESS/MSS, reporting, and compliance
- Some masters are simple lookup masters
- Some masters are richer and contain business behavior, especially:
  - Document Type
  - Skill
  - Skill Category
  - Certification Type
- The system is multi-tenant, but these masters are globally maintained
- Later, tenant-level enablement/mapping may be added, but that is not the main scope now
- This phase is only for reusable reference masters, not payroll processing

--------------------------------------------------
ARCHITECTURE EXPECTATION
--------------------------------------------------

Use clean layered architecture and enterprise best practices.

Generate:
- database schema design
- JPA entities
- Flyway migration scripts
- repositories
- service layer
- DTOs
- mappers
- controllers
- Bean Validation
- audit logging structure
- status management
- pagination/filter/sort support
- reusable option/dropdown APIs
- standard API response model
- exception handling

Use these assumptions:
- UUID primary keys
- active/inactive status support for every master
- created_at, updated_at, created_by, updated_by in all tables
- audit logs for create/update/status changes
- APIs must be future-ready for RBAC
- code should be modular and extensible
- use soft activation/inactivation instead of hard delete by default

--------------------------------------------------
REFERENCE MASTER DETAILS
--------------------------------------------------

1. COUNTRY
Purpose:
Reusable country reference across tenant setup, employee profile, training, education, location, compliance, and future payroll country rules.

Suggested fields:
- id
- countryCode
- countryName
- shortName
- iso2Code
- iso3Code
- phoneCode
- nationalityName
- defaultCurrencyCode
- defaultTimezone
- gccFlag
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- countryCode unique
- iso2Code unique
- iso3Code unique
- defaultCurrencyCode should map to a valid currency when present

2. CURRENCY
Purpose:
Reusable currency reference across compensation, payroll, allowances, claims, and reporting.

Suggested fields:
- id
- currencyCode
- currencyName
- currencySymbol
- decimalPlaces
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- currencyCode unique
- decimalPlaces should be a valid non-negative number

3. LANGUAGE
Purpose:
Reusable language reference across localization, UI settings, tenant languages, notifications, and translation support.

Suggested fields:
- id
- languageCode
- languageName
- nativeName
- rtlEnabled
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- languageCode unique

4. NATIONALITY
Purpose:
Reusable nationality reference across employee profile, recruitment, benefits, travel, and compliance rules.

Suggested fields:
- id
- nationalityCode
- nationalityName
- countryCode
- gccNationalFlag
- omaniFlag
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- nationalityCode unique
- nationalityName unique if appropriate
- countryCode should map to valid country when present

5. RELIGION
Purpose:
Reusable religion reference across employee profile and future religion-based eligibility rules.

Suggested fields:
- id
- religionCode
- religionName
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- religionCode unique
- religionName unique

6. GENDER
Purpose:
Reusable gender reference across employee profile, analytics, reporting, and diversity metrics.

Suggested fields:
- id
- genderCode
- genderName
- displayOrder
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- genderCode unique
- displayOrder optional but should be non-negative when present

7. MARITAL STATUS
Purpose:
Reusable marital status reference across employee profile, benefits, and dependent eligibility.

Suggested fields:
- id
- maritalStatusCode
- maritalStatusName
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- maritalStatusCode unique
- maritalStatusName unique

8. RELATIONSHIP TYPE
Purpose:
Reusable relationship definitions for dependent details, beneficiaries, and emergency contacts.

Suggested fields:
- id
- relationshipTypeCode
- relationshipTypeName
- dependentAllowed
- emergencyContactAllowed
- beneficiaryAllowed
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- relationshipTypeCode unique

9. DOCUMENT TYPE
Purpose:
Reusable document type master with business behavior.

Suggested fields:
- id
- documentTypeCode
- documentTypeName
- shortDescription
- documentFor
- issueDateRequired
- expiryDateRequired
- alertRequired
- alertDaysBefore
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Suggested enum values for documentFor:
- EMPLOYEE
- EMPLOYER
- BOTH

Rules:
- documentTypeCode unique
- alertDaysBefore cannot be negative
- if alertRequired = false, alertDaysBefore may be null or zero
- if expiryDateRequired = false, expiry-based validation should not be forced later

10. EDUCATION LEVEL
Purpose:
Reusable education level master for employee qualification records.

Suggested fields:
- id
- educationLevelCode
- educationLevelName
- rankingOrder
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- educationLevelCode unique
- rankingOrder optional but should be non-negative when present

11. CERTIFICATION TYPE
Purpose:
Reusable certification type reference for professional certification tracking.

Suggested fields:
- id
- certificationTypeCode
- certificationTypeName
- expiryTrackingRequired
- issuingBodyRequired
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- certificationTypeCode unique
- certificationTypeName unique

12. SKILL CATEGORY
Purpose:
Reusable grouping master for skills.

Suggested fields:
- id
- skillCategoryCode
- skillCategoryName
- description
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- skillCategoryCode unique
- skillCategoryName unique

13. SKILL
Purpose:
Reusable skill master linked to a skill category for competency, skills inventory, and gap analysis.

Suggested fields:
- id
- skillCode
- skillName
- skillCategoryId
- description
- active
- createdAt
- updatedAt
- createdBy
- updatedBy

Rules:
- skillCode unique
- skillName unique within category or globally, choose a clean design and explain in code comments
- skillCategoryId required
- cannot create skill without valid skill category

14. AUDIT LOG
Purpose:
Track all key changes.

Suggested fields:
- id
- entityName
- entityId
- action
- oldValue
- newValue
- changedBy
- changedAt
- ipAddress
- source
- correlationId

Track events:
- create
- update
- status change
- important business flag changes

--------------------------------------------------
DATABASE DESIGN EXPECTATION
--------------------------------------------------

Design normalized PostgreSQL tables for:
- countries
- currencies
- languages
- nationalities
- religions
- genders
- marital_statuses
- relationship_types
- document_types
- education_levels
- certification_types
- skill_categories
- skills
- audit_logs

Include:
- primary keys
- foreign keys
- unique constraints
- check constraints where useful
- indexes for code/name/status lookups
- audit columns

Provide Flyway migration scripts in proper order.

--------------------------------------------------
API REQUIREMENTS
--------------------------------------------------

For each master, generate standard CRUD-style REST APIs:

Create
- POST /api/reference/{master}

List
- GET /api/reference/{master}

Get by id
- GET /api/reference/{master}/{id}

Update
- PUT /api/reference/{master}/{id}

Status change
- PATCH /api/reference/{master}/{id}/status

Option/dropdown API
- GET /api/reference/{master}/options

Use plural resource names like:
- /api/reference/countries
- /api/reference/currencies
- /api/reference/languages
- /api/reference/nationalities
- /api/reference/religions
- /api/reference/genders
- /api/reference/marital-statuses
- /api/reference/relationship-types
- /api/reference/document-types
- /api/reference/education-levels
- /api/reference/certification-types
- /api/reference/skill-categories
- /api/reference/skills

Additional requirements:
- list APIs should support pagination
- list APIs should support search by code/name
- list APIs should support status filter
- skills list should support filtering by skillCategoryId
- options APIs should return only active items by default
- options APIs should be lightweight for dropdown usage

--------------------------------------------------
VALIDATION REQUIREMENTS
--------------------------------------------------

Use Bean Validation and service-level validation.

Must validate:
- required fields
- unique codes
- unique names where appropriate
- valid enums
- valid booleans and numeric ranges
- no duplicate skill category references
- foreign key integrity
- no creation of skill without skill category
- no invalid country-currency or nationality-country linkage
- document type behavioral rules
- status transition sanity

--------------------------------------------------
CODE STRUCTURE EXPECTATION
--------------------------------------------------

Generate clean package structure such as:
- reference/country/domain/entity
- reference/country/domain/repository
- reference/country/application/service
- reference/country/application/dto/request
- reference/country/application/dto/response
- reference/country/application/mapper
- reference/country/interfaces/rest

Or a consistent alternative module structure that fits a Gradle multi-module enterprise project.

Also include:
- base entity
- common status enum
- reusable paged response wrapper
- reusable api response wrapper
- global exception handling
- common audit support
- reusable option DTO

--------------------------------------------------
OUTPUT FORMAT
--------------------------------------------------

I do not want generic explanation only.

I want the answer in this order:
1. final recommended package/module structure
2. database schema design
3. Flyway migration SQL
4. entity classes
5. DTOs
6. repositories
7. service interfaces and implementations
8. mappers
9. controllers
10. validation rules
11. exception handling
12. audit logging approach
13. sample request/response JSON
14. sample option API JSON
15. important future extension notes

Generate production-grade code, not pseudo-code.
Keep the code realistic, consistent, and maintainable.