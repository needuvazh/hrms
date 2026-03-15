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

This platform supports enterprise HR operations with employee document vault, onboarding workflows, compliance tracking, and policy acknowledgement.

----------------------------------------------------

MODULE
Step 6 — Document and Policy Masters

These are master-data modules required for employee document vault, onboarding, and policy acknowledgement workflows.

Modules to implement:

1. Document Type
2. Document Category
3. Document Applicability Rule
4. Document Expiry Rule
5. Policy Document Type
6. Policy Acknowledgement Type
7. Attachment Category

----------------------------------------------------

BUSINESS CONTEXT

These masters are needed for:

- passport
- visa
- residence card
- labour card
- civil ID
- education certificates
- professional licences
- onboarding document acknowledgement
- employee document vault
- policy acknowledgement workflows
- document expiry monitoring
- upload classification and validation

These modules define the document framework.

They are NOT the employee transaction tables themselves.

Do NOT create employee document transaction tables in this step.

This step only defines the master data used by later modules such as:

- employee_documents
- dependent_documents
- onboarding_document_checklists
- employee_policy_acknowledgements
- attachment storage mappings

Important distinction:

Document Type
- defines what document is being captured
- example: Passport, Visa, Civil ID, Education Certificate

Document Applicability Rule
- defines who must provide the document
- example: Passport required for expatriate employees

Document Expiry Rule
- defines how expiry monitoring works
- example: Passport alerts at 90/60/30/7/1 days before expiry

Policy Document Type
- defines policy classes such as handbook or code of conduct

Policy Acknowledgement Type
- defines how user acknowledgement is collected
- example: checkbox, read-and-accept, e-signature

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

All codes should be unique within tenant unless a stronger scoped uniqueness is justified.

Assume these modules are tenant-owned for maximum flexibility.

----------------------------------------------------

ENTITY DEFINITIONS

1. DOCUMENT CATEGORY

Purpose
Groups document types into business categories.

Examples
- Identity Documents
- Immigration Documents
- Education Documents
- Certification Documents
- Employment Documents
- Medical Documents
- Onboarding Documents
- Compliance Documents

Fields

id
tenant_id
document_category_code
document_category_name
description
display_order
active

Rules

document_category_code unique per tenant
document_category_name required
display_order must be zero or positive when provided


2. DOCUMENT TYPE

Purpose
Defines document kinds used in employee vault and onboarding.

Examples
- Passport
- Visa
- Residence Card
- Labour Card
- Civil ID
- Education Certificate
- Professional Licence
- Offer Letter
- NDA
- Medical Certificate

Fields

id
tenant_id
document_type_code
document_type_name
short_description
document_for
document_category_id nullable
attachment_required
issue_date_required
expiry_date_required
reference_no_required
multiple_allowed
active

document_for values

EMPLOYEE
EMPLOYER
DEPENDENT
BOTH

Rules

document_type_code unique per tenant
document_type_name required
document_category_id must belong to same tenant when provided


3. DOCUMENT APPLICABILITY RULE

Purpose
Defines which employees or dependents must provide which documents.

Examples
- Passport required for expatriate employees
- Civil ID required for Omani nationals
- Work permit required for expatriate employees
- Professional licence required for selected job families
- Family visa document required for sponsored dependents

Fields

id
tenant_id
applicability_rule_code
document_type_id
worker_type_id nullable
employee_category_id nullable
nationalisation_category_id nullable
legal_entity_id nullable
job_family_id nullable
designation_id nullable
dependent_type_id nullable
mandatory_flag
onboarding_required_flag
description
active

Rules

applicability_rule_code unique per tenant
document_type_id required
document_type_id must belong to same tenant
all referenced records must belong to same tenant
at least one applicability dimension should be selected unless the rule is meant to be tenant-wide default
support both broad and targeted rules


4. DOCUMENT EXPIRY RULE

Purpose
Defines expiry monitoring and renewal behavior by document type.

Examples
- Passport alerts at [90,60,30,7,1]
- Visa alerts at [60,30,7]
- Professional licence blocks transactions after expiry

Fields

id
tenant_id
expiry_rule_code
document_type_id
expiry_tracking_required
renewal_required
alert_days_before_json
grace_period_days
block_transaction_on_expiry_flag
description
active

Rules

expiry_rule_code unique per tenant
document_type_id required
document_type_id must belong to same tenant
grace_period_days must be zero or positive
alert_days_before_json should be stored in a PostgreSQL-compatible structure such as jsonb
validate that alert values are non-negative integers in descending or logical order
if expiry_tracking_required is false, renewal_required and alert values should be logically consistent


5. POLICY DOCUMENT TYPE

Purpose
Defines policy document classes used in onboarding and HR acknowledgement flows.

Examples
- Employee Handbook
- Code of Conduct
- Information Security Policy
- Anti-Harassment Policy
- Leave Policy
- Travel Policy
- NDA
- IT Acceptable Use Policy

Fields

id
tenant_id
policy_document_type_code
policy_document_type_name
description
version_required_flag
active

Rules

policy_document_type_code unique per tenant
policy_document_type_name required


6. POLICY ACKNOWLEDGEMENT TYPE

Purpose
Defines how acknowledgement is collected and enforced.

Examples
- Read and Accept
- Checkbox Confirmation
- E-Signature Required
- One-Time Acknowledgement
- Re-Acknowledge on Revision
- Annual Re-Acknowledgement

Fields

id
tenant_id
policy_ack_type_code
policy_ack_type_name
e_signature_required_flag
reack_on_version_change_flag
annual_reack_flag
description
active

Rules

policy_ack_type_code unique per tenant
policy_ack_type_name required


7. ATTACHMENT CATEGORY

Purpose
Defines upload categories and file handling groups.

Examples
- Employee Photo
- Identity Scan
- Visa Copy
- Certificate Copy
- Signed Policy PDF
- Medical Report
- Contract Attachment
- Supporting Document

Fields

id
tenant_id
attachment_category_code
attachment_category_name
mime_group
max_file_size_mb
description
active

mime_group values

PDF
IMAGE
OFFICE_DOC
ARCHIVE
OTHER

Rules

attachment_category_code unique per tenant
attachment_category_name required
max_file_size_mb must be greater than zero when provided


8. AUDIT LOG

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
rule reassignment
expiry rule update
policy behaviour change

----------------------------------------------------

DATABASE TABLES

Create tables:

document_categories
document_types
document_applicability_rules
document_expiry_rules
policy_document_types
policy_acknowledgement_types
attachment_categories
audit_logs

Include:

- indexes
- foreign keys
- tenant scoped unique constraints
- check constraints where useful
- jsonb or equivalent for alert_days_before_json if using PostgreSQL

Reference dependencies from previous steps when needed:

Step 3 organization masters
- legal_entities

Step 4 job architecture masters
- designations
- job_families
- worker_types
- employee_categories

Step 5 compliance masters
- nationalisation_categories
- dependent_types

----------------------------------------------------

API ENDPOINTS

Document Category

POST /api/document-policy/document-categories
GET /api/document-policy/document-categories
GET /api/document-policy/document-categories/{id}
PUT /api/document-policy/document-categories/{id}
PATCH /api/document-policy/document-categories/{id}/status
GET /api/document-policy/document-categories/options


Document Type

POST /api/document-policy/document-types
GET /api/document-policy/document-types
GET /api/document-policy/document-types/{id}
PUT /api/document-policy/document-types/{id}
PATCH /api/document-policy/document-types/{id}/status
GET /api/document-policy/document-types/options


Document Applicability Rule

POST /api/document-policy/document-applicability-rules
GET /api/document-policy/document-applicability-rules
GET /api/document-policy/document-applicability-rules/{id}
PUT /api/document-policy/document-applicability-rules/{id}
PATCH /api/document-policy/document-applicability-rules/{id}/status
GET /api/document-policy/document-applicability-rules/options


Document Expiry Rule

POST /api/document-policy/document-expiry-rules
GET /api/document-policy/document-expiry-rules
GET /api/document-policy/document-expiry-rules/{id}
PUT /api/document-policy/document-expiry-rules/{id}
PATCH /api/document-policy/document-expiry-rules/{id}/status
GET /api/document-policy/document-expiry-rules/options


Policy Document Type

POST /api/document-policy/policy-document-types
GET /api/document-policy/policy-document-types
GET /api/document-policy/policy-document-types/{id}
PUT /api/document-policy/policy-document-types/{id}
PATCH /api/document-policy/policy-document-types/{id}/status
GET /api/document-policy/policy-document-types/options


Policy Acknowledgement Type

POST /api/document-policy/policy-acknowledgement-types
GET /api/document-policy/policy-acknowledgement-types
GET /api/document-policy/policy-acknowledgement-types/{id}
PUT /api/document-policy/policy-acknowledgement-types/{id}
PATCH /api/document-policy/policy-acknowledgement-types/{id}/status
GET /api/document-policy/policy-acknowledgement-types/options


Attachment Category

POST /api/document-policy/attachment-categories
GET /api/document-policy/attachment-categories
GET /api/document-policy/attachment-categories/{id}
PUT /api/document-policy/attachment-categories/{id}
PATCH /api/document-policy/attachment-categories/{id}/status
GET /api/document-policy/attachment-categories/options

----------------------------------------------------

LIST API REQUIREMENTS

All list APIs should support:

- tenant scoping
- pagination
- sorting
- search by code and name
- active/inactive filter

Additional filters:

Document Type
- documentCategoryId
- documentFor

Document Applicability Rule
- documentTypeId
- workerTypeId
- employeeCategoryId
- nationalisationCategoryId
- legalEntityId
- jobFamilyId
- designationId
- dependentTypeId
- mandatoryFlag
- onboardingRequiredFlag

Document Expiry Rule
- documentTypeId
- expiryTrackingRequired
- renewalRequired
- blockTransactionOnExpiryFlag

Attachment Category
- mimeGroup

Options APIs should return active items by default.

----------------------------------------------------

VALIDATION REQUIREMENTS

Use Bean Validation and service-level validation.

Validate:

- required fields
- code uniqueness per tenant
- same-tenant references
- valid enums
- display_order >= 0
- grace_period_days >= 0
- max_file_size_mb > 0 when provided
- alert_days_before_json contains only non-negative integers
- alert_days_before_json should not contain duplicates
- expiry rule consistency
- applicability rule reference integrity
- document type category integrity
- attachment category mime group validity

Add extension points/comments for future validations such as:

- cannot deactivate if already used by employee document records
- cannot change expiry rule on a live document type without impact analysis
- cannot remove mandatory onboarding document already assigned to active onboarding workflows

----------------------------------------------------

PACKAGE / MODULE STRUCTURE

Use clean modular package structure like:

documentpolicy/documenttype/domain/entity
documentpolicy/documenttype/domain/repository
documentpolicy/documenttype/application/service
documentpolicy/documenttype/application/dto/request
documentpolicy/documenttype/application/dto/response
documentpolicy/documenttype/application/mapper
documentpolicy/documenttype/interfaces/rest

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
