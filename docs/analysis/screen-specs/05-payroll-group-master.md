# 05 - Payroll Group Master

## Purpose

Define employee segmentation for independent payroll runs and approvals.

## UI sections

1. Group list
2. Group builder form
3. Population preview panel
4. Workflow mapping section
5. Audit tab

## Fields

- `groupCode`
- `groupName`
- `calendarId`
- `selectionMode` (`RULE_BASED`, `MANUAL`)
- `filters`:
  - legal entity
  - branch
  - department
  - grade
  - employment type
- `approvalWorkflowId`
- `effectiveFrom`
- `effectiveTo`
- `status`

## Validations

- `groupCode` unique per tenant.
- at least one employee should be eligible in preview before activation.
- mapped workflow must be active.

## API contracts

- `POST /api/v1/payroll-groups`
- `GET /api/v1/payroll-groups`
- `GET /api/v1/payroll-groups/{groupId}`
- `PUT /api/v1/payroll-groups/{groupId}`
- `POST /api/v1/payroll-groups/{groupId}/preview-population`
- `POST /api/v1/payroll-groups/{groupId}/activate`

## Create example

```json
{
  "groupCode": "HQ_STAFF",
  "groupName": "HQ Staff",
  "calendarId": "5b595063-f8b2-4738-b79a-96fcc83f0785",
  "selectionMode": "RULE_BASED",
  "filters": {
    "legalEntityIds": ["a6a54762-1607-4a24-b98f-4f37de9c6a5a"],
    "branchIds": ["3e31dc6d-8e14-4927-a0b6-8cf3f5e28611"],
    "departmentIds": []
  },
  "approvalWorkflowId": "4e0fd2ef-e60f-45de-80cd-77f520131f6f",
  "effectiveFrom": "2026-04-01"
}
```

## DDL draft

```sql
create table payroll.payroll_group (
  id uuid primary key,
  tenant_id uuid not null,
  group_code varchar(30) not null,
  group_name varchar(120) not null,
  calendar_id uuid not null,
  selection_mode varchar(20) not null,
  filter_json jsonb not null,
  approval_workflow_id uuid not null,
  effective_from date not null,
  effective_to date,
  status varchar(20) not null,
  version int not null,
  created_at timestamptz not null,
  created_by varchar(100) not null,
  updated_at timestamptz not null,
  updated_by varchar(100) not null,
  unique (tenant_id, group_code, version)
);
```

## Audit events

- `PAYROLL_GROUP_CREATED`
- `PAYROLL_GROUP_UPDATED`
- `PAYROLL_GROUP_ACTIVATED`

## Test cases

1. preview population with empty result -> activation blocked.
2. update filters and verify preview count changes.
3. tenant isolation for list and details.
