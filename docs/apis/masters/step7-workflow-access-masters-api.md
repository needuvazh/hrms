# Step 7 - Workflow and Access Masters API

Implementation module: `hrms-modules/hrms-master-data`

Base path:

- `/api/workflow-access`

Resources:

- `roles`
- `permissions`
- `role-permission-mappings`
- `workflow-types`
- `approval-matrices`
- `notification-templates`
- `service-request-types`
- `delegation-types`
- `approval-action-types`

Common endpoints per resource:

- `POST /api/workflow-access/{resource}`
- `PUT /api/workflow-access/{resource}/{id}`
- `GET /api/workflow-access/{resource}/{id}`
- `GET /api/workflow-access/{resource}`
- `PATCH /api/workflow-access/{resource}/{id}/status`
- `GET /api/workflow-access/{resource}/options`

Common list query params:

- `q`, `active`, `limit`, `offset`, `sort`

Additional list filters:

- Role: `roleType`
- Permission: `moduleCode`, `actionType`, `scopeType`
- Role-Permission Mapping: `roleId`, `permissionId`, `allowFlag`, `dataScopeOverride`
- Workflow Type: `moduleName`, `initiationChannel`, `approvalRequiredFlag`
- Approval Matrix: `workflowTypeId`, `legalEntityId`, `branchId`, `departmentId`, `employeeCategoryId`, `workerTypeId`, `serviceRequestTypeId`, `approverSourceType`, `levelNo`, `delegationAllowedFlag`
- Notification Template: `eventCode`, `channelType`, `languageCode`
- Service Request Type: `category`, `workflowTypeId`, `attachmentRequiredFlag`, `autoCloseAllowedFlag`
- Delegation Type: `approvalAllowedFlag`, `actionAllowedFlag`, `viewAllowedFlag`, `temporaryOnlyFlag`
- Approval Action Type: `finalActionFlag`

Options query params:

- `q`, `limit`, `activeOnly` (default `true`)

Status payload:

```json
{ "active": true }
```

## Request model (`MasterRequest`)

Unified request contract is used for all resources; send only resource-relevant fields.

Core fields:

- `code`, `name`, `description`, `active`

Role fields:

- `roleType` (`SYSTEM`, `TENANT`, `CUSTOM`)

Permission fields:

- `moduleCode`
- `actionType` (`VIEW`, `CREATE`, `EDIT`, `DELETE`, `APPROVE`, `REJECT`, `EXPORT`, `PUBLISH`, `ACKNOWLEDGE`, `INITIATE`)
- `scopeType` (`SELF`, `TEAM`, `DEPARTMENT`, `ENTITY`, `ALL`)

Role-Permission Mapping fields:

- `roleId`, `permissionId`, `allowFlag`, `dataScopeOverride`

Workflow Type fields:

- `moduleName`, `initiationChannel` (`ESS`, `MSS`, `HR`, `SYSTEM`, `PAYROLL`, `ADMIN`), `approvalRequiredFlag`

Approval Matrix fields:

- `workflowTypeId`, `matrixName`
- `legalEntityId`, `branchId`, `departmentId`, `employeeCategoryId`, `workerTypeId`, `serviceRequestTypeId`
- `minAmount`, `maxAmount`, `levelNo`
- `approverSourceType` (`ROLE`, `USER`, `REPORT_TO_POSITION`, `DEPARTMENT_HEAD`, `WORKFLOW_INITIATOR_MANAGER`)
- `approverRoleId`, `approverUserRef`, `approvalActionTypeId`, `escalationDays`, `delegationAllowedFlag`

Notification Template fields:

- `eventCode`, `channelType` (`EMAIL`, `SMS`, `PUSH`, `IN_APP`), `subjectTemplate`, `bodyTemplate`, `languageCode`

Service Request Type fields:

- `category`, `workflowTypeId`, `attachmentRequiredFlag`, `autoCloseAllowedFlag`

Delegation Type fields:

- `approvalAllowedFlag`, `actionAllowedFlag`, `viewAllowedFlag`, `temporaryOnlyFlag`

Approval Action Type fields:

- `finalActionFlag`

`GET /api/workflow-access/{resource}` response is paged and returns:

```json
{
  "items": [
    {
      "id": "uuid",
      "tenantId": "default",
      "code": "HR_ADMIN",
      "name": "HR Admin",
      "active": true,
      "createdAt": "2026-01-01T00:00:00Z",
      "updatedAt": "2026-01-01T00:00:00Z",
      "createdBy": "system",
      "updatedBy": "system"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Each item in `items` is `MasterViewDto`.

## Response model (`MasterViewDto`)

Response includes a superset payload:

- Identity/audit: `id`, `tenantId`, `active`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`
- Core: `code`, `name`, `description`
- Resource-specific properties (role/permission/workflow/matrix/template/service-request/delegation/action fields)

## Sample requests

Role:

```json
{
  "code": "HR_ADMIN",
  "name": "HR Admin",
  "roleType": "TENANT",
  "description": "Tenant HR administrator",
  "active": true
}
```

Permission:

```json
{
  "code": "employee.view",
  "name": "Employee View",
  "moduleCode": "employee",
  "actionType": "VIEW",
  "scopeType": "ALL",
  "description": "View employee profiles",
  "active": true
}
```

Role-Permission Mapping:

```json
{
  "roleId": "uuid",
  "permissionId": "uuid",
  "allowFlag": true,
  "dataScopeOverride": "DEPARTMENT",
  "description": "Department-scoped access",
  "active": true
}
```

Approval Matrix:

```json
{
  "code": "LEAVE_STD",
  "name": "Leave Matrix",
  "workflowTypeId": "uuid",
  "matrixName": "Leave Standard Matrix",
  "levelNo": 1,
  "approverSourceType": "ROLE",
  "approverRoleId": "uuid",
  "approvalActionTypeId": "uuid",
  "delegationAllowedFlag": true,
  "active": true
}
```

Notification Template:

```json
{
  "code": "LEAVE_APPROVED",
  "name": "Leave Approved",
  "eventCode": "LEAVE_APPROVED",
  "channelType": "EMAIL",
  "subjectTemplate": "Leave approved",
  "bodyTemplate": "Your leave request has been approved.",
  "languageCode": "en",
  "active": true
}
```

Options response:

```json
[
  { "id": "uuid", "code": "HR_ADMIN", "name": "HR Admin" }
]
```
