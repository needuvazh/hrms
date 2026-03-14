# 15 - ESS and MSS Core Flows

## Purpose

Enable self-service employee actions and manager approvals to reduce HR dependency and speed payroll cycle readiness.

## ESS screens

1. ESS dashboard
2. Leave request and balance
3. Payslip history and download
4. Loan/advance request and status
5. Profile change request
6. Letters and certificates request
7. Exit request and settlement tracking

## MSS screens

1. Manager approval inbox
2. Team leave and attendance calendar
3. Delegation setup

## Core rules

- employee can only access own personal/payroll data.
- manager can approve only within reporting scope or delegation scope.
- all actions route through workflow definitions.

## API contracts (ESS)

- `GET /api/v1/ess/dashboard`
- `POST /api/v1/leave-requests`
- `GET /api/v1/leave-balances/me`
- `GET /api/v1/employees/me/payslips`
- `POST /api/v1/loans/requests`
- `POST /api/v1/ess/profile-change-requests`
- `POST /api/v1/employees/me/certificates`
- `POST /api/v1/exit-requests`

## API contracts (MSS)

- `GET /api/v1/approvals/inbox`
- `POST /api/v1/approvals/{approvalId}/actions`
- `GET /api/v1/teams/{teamId}/calendar`
- `POST /api/v1/delegations`
- `GET /api/v1/delegations/me`

## Approval action example

```json
{
  "action": "APPROVE",
  "comment": "Approved after verification"
}
```

## DDL draft additions

```sql
create table workflow.approval_task (
  id uuid primary key,
  tenant_id uuid not null,
  workflow_instance_id uuid not null,
  task_type varchar(40) not null,
  assignee_id varchar(100) not null,
  status varchar(20) not null,
  due_at timestamptz,
  created_at timestamptz not null
);

create table workflow.delegation (
  id uuid primary key,
  tenant_id uuid not null,
  delegator_id varchar(100) not null,
  delegate_id varchar(100) not null,
  from_date date not null,
  to_date date not null,
  scope_json jsonb not null,
  status varchar(20) not null
);
```

## Audit events

- `ESS_REQUEST_SUBMITTED`
- `MSS_APPROVAL_ACTION_TAKEN`
- `DELEGATION_CREATED`
- `DELEGATION_EXPIRED`

## Test cases

1. employee attempts to view another employee payslip -> forbidden.
2. manager with no scope attempts approval -> forbidden.
3. delegation in date range permits approval action.
4. leave approval updates leave balance and calendar projection.
