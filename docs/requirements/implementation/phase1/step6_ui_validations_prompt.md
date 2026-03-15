SYSTEM ROLE
You are a senior enterprise UI architect and frontend engineer.

STACK
Flutter
Enterprise admin UI


----------------------------------------------------

MODULE
Step 6 — Document and Policy Masters

Design the UI and validation system for:

1. Document Type
2. Document Category
3. Document Applicability Rule
4. Document Expiry Rule
5. Policy Document Type
6. Policy Acknowledgement Type
7. Attachment Category

----------------------------------------------------

UI GOALS

Enterprise HRMS document and policy configuration interface.

Must support:

- employee document vault setup
- onboarding document requirement setup
- document expiry rule configuration
- policy acknowledgement setup
- attachment upload classification
- reusable admin patterns
- strong field validation
- clear distinction between document definition, applicability rules, and expiry rules

Important distinction:

Document Type
- defines what kind of document exists

Document Applicability Rule
- defines who must provide it

Document Expiry Rule
- defines how expiry is monitored

Policy Document Type
- defines policy classes

Policy Acknowledgement Type
- defines how acknowledgement is collected

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
- Document Category
- Policy Document Type
- Policy Acknowledgement Type
- Attachment Category

Medium masters
- Document Type

Rich masters
- Document Applicability Rule
- Document Expiry Rule

----------------------------------------------------

MODULE UI DETAILS

1. DOCUMENT CATEGORY

Columns

Document Category Code
Document Category Name
Display Order
Status

Fields

Document Category Code
Document Category Name
Description
Display Order
Active


2. DOCUMENT TYPE

Columns

Document Type Code
Document Type Name
Document For
Document Category
Attachment Required
Issue Date Required
Expiry Date Required
Reference No Required
Multiple Allowed
Status

Fields

Document Type Code
Document Type Name
Short Description
Document For
Document Category optional
Attachment Required
Issue Date Required
Expiry Date Required
Reference No Required
Multiple Allowed
Active


3. DOCUMENT APPLICABILITY RULE

Columns

Rule Code
Document Type
Worker Type
Employee Category
Nationalisation Category
Legal Entity
Job Family
Designation
Dependent Type
Mandatory
Onboarding Required
Status

Fields

Applicability Rule Code
Document Type
Worker Type optional
Employee Category optional
Nationalisation Category optional
Legal Entity optional
Job Family optional
Designation optional
Dependent Type optional
Mandatory Flag
Onboarding Required Flag
Description
Active

Special UI behavior

- allow broad rules and targeted rules
- show helper text that at least one applicability filter is recommended unless the rule is tenant-wide default
- support dependent type only when relevant
- clearly show selected filters as rule scope


4. DOCUMENT EXPIRY RULE

Columns

Rule Code
Document Type
Expiry Tracking Required
Renewal Required
Alert Days
Grace Period Days
Block On Expiry
Status

Fields

Expiry Rule Code
Document Type
Expiry Tracking Required
Renewal Required
Alert Days Before
Grace Period Days
Block Transaction On Expiry
Description
Active

Special UI behavior

- alert days input should support multi-value entry
- example chips or tag input: 90, 60, 30, 7, 1
- if expiry tracking required is false, disable renewal and alert inputs
- if renewal required is false, allow alerts only if business logic permits, otherwise explain chosen UX
- validate duplicates and negative values


5. POLICY DOCUMENT TYPE

Columns

Policy Document Type Code
Policy Document Type Name
Version Required
Status

Fields

Policy Document Type Code
Policy Document Type Name
Description
Version Required Flag
Active


6. POLICY ACKNOWLEDGEMENT TYPE

Columns

Acknowledgement Type Code
Acknowledgement Type Name
E-Signature Required
Re-Acknowledge On Version Change
Annual Re-Acknowledge
Status

Fields

Policy Acknowledgement Type Code
Policy Acknowledgement Type Name
E-Signature Required Flag
Re-Acknowledge On Version Change Flag
Annual Re-Acknowledge Flag
Description
Active


7. ATTACHMENT CATEGORY

Columns

Attachment Category Code
Attachment Category Name
MIME Group
Max File Size MB
Status

Fields

Attachment Category Code
Attachment Category Name
MIME Group
Max File Size MB
Description
Active

MIME Group values

PDF
IMAGE
OFFICE_DOC
ARCHIVE
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

Document Category

- display order must be zero or positive if provided

Document Type

- code required
- name required
- document_for required
- selected document category must be valid
- boolean fields must have clear labels and defaults

Document Applicability Rule

- rule code required
- document type required
- selected related masters must come from valid options APIs
- show warning if no applicability dimension is selected
- same-tenant integrity assumed via backend, but UI must only load current tenant options
- if designation is selected, job family can remain optional unless business flow requires it

Document Expiry Rule

- rule code required
- document type required
- grace period must be zero or positive
- alert days values must be integers
- alert days must be non-negative
- no duplicate alert day values
- expiry tracking false should disable dependent fields
- block on expiry toggle should remain available only when expiry tracking is enabled

Policy Document Type

- code required
- name required

Policy Acknowledgement Type

- code required
- name required
- flags must be clearly explained in helper text

Attachment Category

- code required
- name required
- mime group required
- max file size must be greater than zero if entered

----------------------------------------------------

SPECIAL UX REQUIREMENTS

Document Applicability Rule screen should be richer than simple masters.

Group the form into sections:

- Basic Information
- Document Scope
- Workforce Applicability
- Organization Applicability
- Job Applicability
- Dependent Applicability
- Rule Behaviour

Document Expiry Rule screen should support a better UX for alert day management:

- chip/tag style entry for alert days
- helper examples
- duplicate prevention in UI
- ability to quickly add common presets like:
  - 30
  - 60,30,7
  - 90,60,30,7,1

Document Type screen should show a quick preview of selected configuration:

- attachment required yes/no
- issue date required yes/no
- expiry date required yes/no
- reference number required yes/no
- multiple upload allowed yes/no

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

GET /api/document-policy/{master}
POST /api/document-policy/{master}
PUT /api/document-policy/{master}/{id}
PATCH /api/document-policy/{master}/{id}/status
GET /api/document-policy/{master}/options

Examples

GET /api/document-policy/document-categories
GET /api/document-policy/document-types
GET /api/document-policy/document-applicability-rules
GET /api/document-policy/document-expiry-rules
GET /api/document-policy/policy-document-types
GET /api/document-policy/policy-acknowledgement-types
GET /api/document-policy/attachment-categories

Also use option APIs from previous steps where needed for Document Applicability Rule:

GET /api/job-architecture/worker-types/options
GET /api/job-architecture/employee-categories/options
GET /api/job-architecture/job-families/options
GET /api/job-architecture/designations/options
GET /api/organization/legal-entities/options
GET /api/compliance/nationalisation-categories/options
GET /api/compliance/dependent-types/options

----------------------------------------------------

FILTER REQUIREMENTS

List screens should support:

Document Category
- search
- status

Document Type
- search
- status
- document category
- document for

Document Applicability Rule
- search
- status
- document type
- worker type
- employee category
- nationalisation category
- legal entity
- mandatory flag
- onboarding required flag

Document Expiry Rule
- search
- status
- document type
- expiry tracking required
- renewal required
- block on expiry

Policy Document Type
- search
- status
- version required

Policy Acknowledgement Type
- search
- status
- e-signature required
- re-ack on version change
- annual re-ack

Attachment Category
- search
- status
- mime group

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
13. document applicability rule screen UI specification
14. document expiry rule screen UI specification
15. sample JSON payloads
16. edge cases
17. reusable component suggestions

Make it implementation-ready.
Do not give generic advice.
If code examples are included, use Flutter style
