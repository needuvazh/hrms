SYSTEM ROLE
You are a senior enterprise UI architect and frontend engineer.

STACK
Flutter
Enterprise admin UI

----------------------------------------------------

MODULE
Step 7 — Workflow and Access Masters

Design the UI and validation system for:

1. Role
2. Permission
3. Role-Permission Mapping
4. Workflow Type
5. Approval Matrix
6. Notification Template
7. Service Request Type
8. Delegation Type
9. Approval Action Type

----------------------------------------------------

UI GOALS

Enterprise HRMS workflow and access configuration interface.

Must support:

- access role setup
- permission catalog management
- role-permission assignment
- workflow type setup
- approval matrix configuration
- notification template setup
- ESS/MSS service request taxonomy
- delegation behavior configuration
- standardized workflow action catalog

Important distinction:

Role
- defines who the user is from an access perspective

Permission
- defines what action can be performed

Role-Permission Mapping
- defines what a role is allowed to do

Workflow Type
- defines what kind of process exists

Approval Matrix
- defines who approves and in what sequence

Notification Template
- defines what message is sent

Service Request Type
- defines what request category can be raised

Delegation Type
- defines how authority can be delegated

Approval Action Type
- defines standardized action names used by workflows

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
- Role
- Workflow Type
- Service Request Type
- Delegation Type
- Approval Action Type

Medium masters
- Permission
- Notification Template

Rich masters
- Role-Permission Mapping
- Approval Matrix

----------------------------------------------------

MODULE UI DETAILS

1. ROLE

Columns

Role Code
Role Name
Role Type
Status

Fields

Role Code
Role Name
Role Type
Description
Active

Role Type values

SYSTEM
TENANT
CUSTOM


2. PERMISSION

Columns

Permission Code
Permission Name
Module Code
Action Type
Scope Type
Status

Fields

Permission Code
Permission Name
Module Code
Action Type
Scope Type
Description
Active

Action Type values

VIEW
CREATE
EDIT
DELETE
APPROVE
REJECT
EXPORT
PUBLISH
ACKNOWLEDGE
INITIATE

Scope Type values

SELF
TEAM
DEPARTMENT
ENTITY
ALL


3. ROLE-PERMISSION MAPPING

Columns

Role
Permission
Allow
Data Scope Override
Status

Fields

Role
Permission
Allow Flag
Data Scope Override optional
Description
Active

Special UI behavior

- permission dropdown searchable
- role dropdown searchable
- prevent duplicate role-permission mapping in UI
- show effective scope clearly


4. WORKFLOW TYPE

Columns

Workflow Type Code
Workflow Type Name
Module Name
Initiation Channel
Approval Required
Status

Fields

Workflow Type Code
Workflow Type Name
Module Name
Initiation Channel
Approval Required Flag
Description
Active

Initiation Channel values

ESS
MSS
HR
SYSTEM
PAYROLL
ADMIN


5. APPROVAL MATRIX

Columns

Matrix Code
Workflow Type
Matrix Name
Level No
Approver Source Type
Approver Role
Legal Entity
Department
Delegation Allowed
Status

Fields

Approval Matrix Code
Workflow Type
Matrix Name
Legal Entity optional
Branch optional
Department optional
Employee Category optional
Worker Type optional
Service Request Type optional
Min Amount optional
Max Amount optional
Level No
Approver Source Type
Approver Role optional
Approver User Reference optional
Approval Action Type optional
Escalation Days optional
Delegation Allowed Flag
Description
Active

Approver Source Type values

ROLE
USER
REPORT_TO_POSITION
DEPARTMENT_HEAD
WORKFLOW_INITIATOR_MANAGER

Special UI behavior

- show/hide approver fields based on approver source type
- if source type = ROLE, require role field
- if source type = USER, require user reference field
- allow optional scoping by legal entity, branch, department, worker type, employee category
- level number should be clearly visible
- approval matrix should support multiple rows by level
- show rule scope summary in the form


6. NOTIFICATION TEMPLATE

Columns

Template Code
Template Name
Event Code
Channel Type
Language Code
Status

Fields

Template Code
Template Name
Event Code
Channel Type
Subject Template
Body Template
Language Code
Description
Active

Channel Type values

EMAIL
SMS
PUSH
IN_APP

Special UI behavior

- subject field can be emphasized for EMAIL
- body supports template variables preview area
- show helper text for placeholder usage
- provide sample placeholder chips like {{employeeName}} and {{requestId}}


7. SERVICE REQUEST TYPE

Columns

Request Type Code
Request Type Name
Category
Workflow Type
Attachment Required
Auto Close Allowed
Status

Fields

Service Request Type Code
Service Request Type Name
Category
Workflow Type optional
Attachment Required Flag
Auto Close Allowed Flag
Description
Active


8. DELEGATION TYPE

Columns

Delegation Type Code
Delegation Type Name
Approval Allowed
Action Allowed
View Allowed
Temporary Only
Status

Fields

Delegation Type Code
Delegation Type Name
Approval Allowed Flag
Action Allowed Flag
View Allowed Flag
Temporary Only Flag
Description
Active


9. APPROVAL ACTION TYPE

Columns

Action Type Code
Action Type Name
Final Action
Status

Fields

Approval Action Type Code
Approval Action Type Name
Final Action Flag
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

Role

- code required
- name required
- role type required

Permission

- code required
- name required
- module code required
- action type required
- scope type required

Role-Permission Mapping

- role required
- permission required
- duplicate role-permission mapping must be blocked in UI
- data scope override optional but must be valid if chosen

Workflow Type

- code required
- name required
- initiation channel required
- approval required must be explicit

Approval Matrix

- matrix code required
- workflow type required
- matrix name required
- level no required and must be >= 1
- min amount and max amount numeric when provided
- min amount cannot exceed max amount
- approver source type required
- if source = ROLE then approver role required
- if source = USER then approver user reference required
- escalation days must be zero or positive
- selected scope filters must come from valid options APIs

Notification Template

- code required
- name required
- event code required
- channel type required
- body template required
- subject template recommended or required for EMAIL depending on UX policy
- language code optional unless multilingual strategy requires it

Service Request Type

- code required
- name required
- category required
- workflow type optional unless request must always be workflow-driven in your chosen UX

Delegation Type

- code required
- name required
- at least one of approval/action/view allowed should be true, if enforcing stricter UX
- temporary only flag optional

Approval Action Type

- code required
- name required

----------------------------------------------------

SPECIAL UX REQUIREMENTS

Approval Matrix screen should be richer than simple masters.

Group the form into sections:

- Basic Information
- Workflow Scope
- Organizational Scope
- Workforce Scope
- Approval Routing
- Escalation and Delegation
- Summary

Role-Permission Mapping screen should support:

- side-by-side mapping UX or modal-based creation
- quick search by role
- quick search by permission
- clear display of allowed scope
- optional bulk assignment pattern recommendation

Notification Template screen should support:

- template preview panel
- placeholder tokens helper
- channel-specific field visibility guidance
- language selection for multilingual tenants

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

GET /api/workflow-access/{master}
POST /api/workflow-access/{master}
PUT /api/workflow-access/{master}/{id}
PATCH /api/workflow-access/{master}/{id}/status
GET /api/workflow-access/{master}/options

Examples

GET /api/workflow-access/roles
GET /api/workflow-access/permissions
GET /api/workflow-access/role-permission-mappings
GET /api/workflow-access/workflow-types
GET /api/workflow-access/approval-matrices
GET /api/workflow-access/notification-templates
GET /api/workflow-access/service-request-types
GET /api/workflow-access/delegation-types
GET /api/workflow-access/approval-action-types

Also use option APIs from previous steps where needed for Approval Matrix:

GET /api/organization/legal-entities/options
GET /api/organization/branches/options
GET /api/organization/departments/options
GET /api/job-architecture/employee-categories/options
GET /api/job-architecture/worker-types/options

----------------------------------------------------

FILTER REQUIREMENTS

List screens should support:

Role
- search
- status
- role type

Permission
- search
- status
- module code
- action type
- scope type

Role-Permission Mapping
- search
- status
- role
- permission
- allow flag
- data scope override

Workflow Type
- search
- status
- module name
- initiation channel
- approval required flag

Approval Matrix
- search
- status
- workflow type
- legal entity
- branch
- department
- employee category
- worker type
- service request type
- approver source type
- level no
- delegation allowed flag

Notification Template
- search
- status
- event code
- channel type
- language code

Service Request Type
- search
- status
- category
- workflow type
- attachment required
- auto close allowed

Delegation Type
- search
- status
- approval allowed
- action allowed
- view allowed
- temporary only

Approval Action Type
- search
- status
- final action flag

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
13. approval matrix screen UI specification
14. role-permission mapping screen UI specification
15. notification template screen UI specification
16. sample JSON payloads
17. edge cases
18. reusable component suggestions

Make it implementation-ready.
Do not give generic advice.
If code examples are included, use Flutter style.
