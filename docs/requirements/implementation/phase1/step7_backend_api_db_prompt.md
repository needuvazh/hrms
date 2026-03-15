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

This platform supports enterprise HR operations with onboarding, employee profile management, document workflows, approvals, notifications, and ESS/MSS service requests.

----------------------------------------------------

MODULE
Step 7 — Workflow and Access Masters

These are definition-level masters for access control, workflow definitions, approval routing, notifications, and service request taxonomy.

Modules to implement:

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

BUSINESS CONTEXT

These masters support:

- employee onboarding approvals
- employee profile approvals
- document approvals
- policy acknowledgement workflows
- transfer approvals
- promotion approvals
- separation approvals
- ESS/MSS service requests
- access control for screens, actions, and data visibility
- manager delegation during absence
- notification and reminder templates

Important distinction:

Role
- business or system role used for access assignment
- example: HR Admin, Manager, Employee

Permission
- granular capability or action
- example: employee.view, employee.edit, leave.approve

Workflow Type
- process category
- example: Onboarding, Leave Request, Transfer Approval

Approval Matrix
- routing definition for approvers and sequence
- example: leave request -> reporting manager -> HR

Notification Template
- reusable template for workflow events
- example: Leave Approved, Profile Change Rejected

Service Request Type
- request taxonomy used in ESS/MSS
- example: Salary Certificate Request, Attendance Correction

Delegation Type
- defines what type of acting-on-behalf behavior is allowed
- example: Temporary Approval Delegation

Approval Action Type
- standardized action catalog used by workflows
- example: SUBMIT, APPROVE, REJECT, RETURN_FOR_CORRECTION

These modules define master data only.

Do NOT create workflow runtime or user transaction tables in this step.

This step only defines the master data used later by:

- users
- user_roles
- workflow_instances
- workflow_tasks
- approval_history
- notifications
- service_requests
- delegations

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

Use these ownership rules:

System/global-friendly masters
- Permission
- Workflow Type
- Delegation Type
- Approval Action Type

Tenant-owned masters
- Role
- Role-Permission Mapping
- Approval Matrix
- Notification Template
- Service Request Type

For tenant-owned tables include:

- tenant_id
- created_at
- updated_at
- created_by
- updated_by
- active flag

For global/system masters also include:

- created_at
- updated_at
- created_by
- updated_by
- active flag

Use soft delete through active status.

All codes should be unique within their ownership scope.

----------------------------------------------------

ENTITY DEFINITIONS

1. ROLE

Purpose
Defines access roles for a tenant.

Examples
- HR_ADMIN
- HR_OFFICER
- MANAGER
- EMPLOYEE
- PAYROLL_ADMIN
- PAYROLL_VIEWER

Fields

id
tenant_id
role_code
role_name
role_type
description
active
created_at
updated_at
created_by
updated_by

role_type values

SYSTEM
TENANT
CUSTOM

Rules

role_code unique per tenant
role_name required


2. PERMISSION

Purpose
Defines granular access capabilities.

Examples
- employee.view
- employee.edit
- employee.salary.view
- leave.approve
- document.approve
- policy.publish
- transfer.initiate

Fields

id
permission_code
permission_name
module_code
action_type
scope_type
description
active
created_at
updated_at
created_by
updated_by

action_type values

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

scope_type values

SELF
TEAM
DEPARTMENT
ENTITY
ALL

Rules

permission_code unique globally
permission_name required
module_code required


3. ROLE-PERMISSION MAPPING

Purpose
Maps tenant roles to permissions.

Fields

id
tenant_id
role_id
permission_id
allow_flag
data_scope_override nullable
description
active
created_at
updated_at
created_by
updated_by

data_scope_override values

SELF
TEAM
DEPARTMENT
ENTITY
ALL

Rules

role_id required
permission_id required
role_id must belong to same tenant
unique mapping per tenant for role_id + permission_id


4. WORKFLOW TYPE

Purpose
Defines workflow categories.

Examples
- ONBOARDING
- PROFILE_CHANGE
- DOCUMENT_APPROVAL
- POLICY_ACKNOWLEDGEMENT
- TRANSFER_APPROVAL
- PROMOTION_APPROVAL
- SEPARATION_APPROVAL
- SERVICE_REQUEST
- LEAVE_REQUEST
- TIMESHEET_APPROVAL

Fields

id
workflow_type_code
workflow_type_name
module_name
initiation_channel
approval_required_flag
description
active
created_at
updated_at
created_by
updated_by

initiation_channel values

ESS
MSS
HR
SYSTEM
PAYROLL
ADMIN

Rules

workflow_type_code unique globally
workflow_type_name required


5. APPROVAL MATRIX

Purpose
Defines approval routing rules and levels.

Examples
- leave request -> manager -> HR
- onboarding -> HR -> business head
- transfer -> department head -> HR -> director

Fields

id
tenant_id
approval_matrix_code
workflow_type_id
matrix_name
legal_entity_id nullable
branch_id nullable
department_id nullable
employee_category_id nullable
worker_type_id nullable
service_request_type_id nullable
min_amount nullable
max_amount nullable
level_no
approver_source_type
approver_role_id nullable
approver_user_ref nullable
approval_action_type_id nullable
escalation_days nullable
delegation_allowed_flag
description
active
created_at
updated_at
created_by
updated_by

approver_source_type values

ROLE
USER
REPORT_TO_POSITION
DEPARTMENT_HEAD
WORKFLOW_INITIATOR_MANAGER

Rules

approval_matrix_code unique per tenant
workflow_type_id required
workflow_type_id must be valid
level_no required and must be >= 1
escalation_days must be >= 0 when provided
if approver_source_type = ROLE then approver_role_id required
if approver_source_type = USER then approver_user_ref required
all referenced tenant-owned entities must belong to same tenant
min_amount <= max_amount when both provided


6. NOTIFICATION TEMPLATE

Purpose
Defines reusable templates for events and workflow actions.

Examples
- LEAVE_SUBMITTED
- LEAVE_APPROVED
- PROFILE_CHANGE_REJECTED
- DOCUMENT_EXPIRY_ALERT
- POLICY_ACKNOWLEDGEMENT_REMINDER
- ONBOARDING_TASK_ASSIGNED

Fields

id
tenant_id
template_code
template_name
event_code
channel_type
subject_template
body_template
language_code
description
active
created_at
updated_at
created_by
updated_by

channel_type values

EMAIL
SMS
PUSH
IN_APP

Rules

template_code unique per tenant
template_name required
event_code required
channel_type required
language_code optional but if provided should support valid language code conventions


7. SERVICE REQUEST TYPE

Purpose
Defines request types available in ESS/MSS or internal HR service workflows.

Examples
- SALARY_CERTIFICATE
- NOC_REQUEST
- ATTENDANCE_CORRECTION
- BANK_LETTER
- TRAVEL_REQUEST
- IT_REQUEST
- ADMIN_REQUEST
- GRIEVANCE
- DOCUMENT_UPLOAD_REQUEST

Fields

id
tenant_id
service_request_type_code
service_request_type_name
category
workflow_type_id nullable
attachment_required_flag
auto_close_allowed_flag
description
active
created_at
updated_at
created_by
updated_by

Rules

service_request_type_code unique per tenant
service_request_type_name required
workflow_type_id must be valid when provided


8. DELEGATION TYPE

Purpose
Defines delegation behavior.

Examples
- FULL_APPROVAL_DELEGATION
- TEMPORARY_APPROVAL_DELEGATION
- VIEW_ONLY_DELEGATION
- BACKUP_APPROVER
- ACTING_MANAGER

Fields

id
delegation_type_code
delegation_type_name
approval_allowed_flag
action_allowed_flag
view_allowed_flag
temporary_only_flag
description
active
created_at
updated_at
created_by
updated_by

Rules

delegation_type_code unique globally
delegation_type_name required


9. APPROVAL ACTION TYPE

Purpose
Defines standardized workflow actions.

Examples
- SUBMIT
- APPROVE
- REJECT
- RETURN_FOR_CORRECTION
- CANCEL
- ESCALATE
- DELEGATE
- ACKNOWLEDGE

Fields

id
approval_action_type_code
approval_action_type_name
final_action_flag
description
active
created_at
updated_at
created_by
updated_by

Rules

approval_action_type_code unique globally
approval_action_type_name required


10. AUDIT LOG

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
mapping change
approval matrix reassignment
notification template content change
delegation rule change

----------------------------------------------------

DATABASE TABLES

Create tables:

roles
permissions
role_permission_mappings
workflow_types
approval_matrices
notification_templates
service_request_types
delegation_types
approval_action_types
audit_logs

Include:

- indexes
- foreign keys
- ownership scoped unique constraints
- check constraints where useful

Reference dependencies from previous steps when needed:

Step 3 organization masters
- legal_entities
- branches
- departments

Step 4 job architecture masters
- worker_types
- employee_categories

----------------------------------------------------

API ENDPOINTS

Role

POST /api/workflow-access/roles
GET /api/workflow-access/roles
GET /api/workflow-access/roles/{id}
PUT /api/workflow-access/roles/{id}
PATCH /api/workflow-access/roles/{id}/status
GET /api/workflow-access/roles/options


Permission

POST /api/workflow-access/permissions
GET /api/workflow-access/permissions
GET /api/workflow-access/permissions/{id}
PUT /api/workflow-access/permissions/{id}
PATCH /api/workflow-access/permissions/{id}/status
GET /api/workflow-access/permissions/options


Role-Permission Mapping

POST /api/workflow-access/role-permission-mappings
GET /api/workflow-access/role-permission-mappings
GET /api/workflow-access/role-permission-mappings/{id}
PUT /api/workflow-access/role-permission-mappings/{id}
PATCH /api/workflow-access/role-permission-mappings/{id}/status
GET /api/workflow-access/role-permission-mappings/options


Workflow Type

POST /api/workflow-access/workflow-types
GET /api/workflow-access/workflow-types
GET /api/workflow-access/workflow-types/{id}
PUT /api/workflow-access/workflow-types/{id}
PATCH /api/workflow-access/workflow-types/{id}/status
GET /api/workflow-access/workflow-types/options


Approval Matrix

POST /api/workflow-access/approval-matrices
GET /api/workflow-access/approval-matrices
GET /api/workflow-access/approval-matrices/{id}
PUT /api/workflow-access/approval-matrices/{id}
PATCH /api/workflow-access/approval-matrices/{id}/status
GET /api/workflow-access/approval-matrices/options


Notification Template

POST /api/workflow-access/notification-templates
GET /api/workflow-access/notification-templates
GET /api/workflow-access/notification-templates/{id}
PUT /api/workflow-access/notification-templates/{id}
PATCH /api/workflow-access/notification-templates/{id}/status
GET /api/workflow-access/notification-templates/options


Service Request Type

POST /api/workflow-access/service-request-types
GET /api/workflow-access/service-request-types
GET /api/workflow-access/service-request-types/{id}
PUT /api/workflow-access/service-request-types/{id}
PATCH /api/workflow-access/service-request-types/{id}/status
GET /api/workflow-access/service-request-types/options


Delegation Type

POST /api/workflow-access/delegation-types
GET /api/workflow-access/delegation-types
GET /api/workflow-access/delegation-types/{id}
PUT /api/workflow-access/delegation-types/{id}
PATCH /api/workflow-access/delegation-types/{id}/status
GET /api/workflow-access/delegation-types/options


Approval Action Type

POST /api/workflow-access/approval-action-types
GET /api/workflow-access/approval-action-types
GET /api/workflow-access/approval-action-types/{id}
PUT /api/workflow-access/approval-action-types/{id}
PATCH /api/workflow-access/approval-action-types/{id}/status
GET /api/workflow-access/approval-action-types/options

----------------------------------------------------

LIST API REQUIREMENTS

All list APIs should support:

- ownership scoping
- pagination
- sorting
- search by code and name
- active/inactive filter

Additional filters:

Role
- roleType

Permission
- moduleCode
- actionType
- scopeType

Role-Permission Mapping
- roleId
- permissionId
- allowFlag
- dataScopeOverride

Workflow Type
- moduleName
- initiationChannel
- approvalRequiredFlag

Approval Matrix
- workflowTypeId
- legalEntityId
- branchId
- departmentId
- employeeCategoryId
- workerTypeId
- serviceRequestTypeId
- approverSourceType
- levelNo
- delegationAllowedFlag

Notification Template
- eventCode
- channelType
- languageCode

Service Request Type
- category
- workflowTypeId
- attachmentRequiredFlag
- autoCloseAllowedFlag

Delegation Type
- approvalAllowedFlag
- actionAllowedFlag
- viewAllowedFlag
- temporaryOnlyFlag

Approval Action Type
- finalActionFlag

Options APIs should return active items by default.

----------------------------------------------------

VALIDATION REQUIREMENTS

Use Bean Validation and service-level validation.

Validate:

- required fields
- code uniqueness in proper scope
- valid enums
- same-tenant reference integrity for tenant-owned references
- role-permission uniqueness
- approval matrix conditional approver rules
- approval matrix min_amount <= max_amount
- level_no >= 1
- escalation_days >= 0
- subject_template required for EMAIL when your design chooses so
- notification body required
- service request workflow reference validity
- approval action type code uniqueness
- delegation type boolean consistency
- permission module/action/scope validity

Add extension points/comments for future validations such as:

- cannot deactivate role if assigned to active users
- cannot deactivate permission if mapped to active role and required by runtime security
- cannot change approval matrix that is already in use by live workflow instances without versioning
- cannot modify notification template code after live usage without migration policy

----------------------------------------------------

PACKAGE / MODULE STRUCTURE

Use clean modular package structure like:

workflowaccess/role/domain/entity
workflowaccess/role/domain/repository
workflowaccess/role/application/service
workflowaccess/role/application/dto/request
workflowaccess/role/application/dto/response
workflowaccess/role/application/mapper
workflowaccess/role/interfaces/rest

Use a consistent equivalent structure for all modules.

Also include:

- base entity
- base tenant-aware entity for tenant-owned tables
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
