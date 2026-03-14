# 08 - Payroll Run Workbench

## Purpose

Provide a single control console for payroll execution lifecycle from draft to lock.

## UI sections

1. Run selector
   - payroll group, period, run status
2. Summary cards
   - employee count, gross, deductions, net, warnings, blockers
3. Lifecycle timeline
   - draft, checklist, simulation, approval, lock, publish
4. Employee grid
   - employee-wise computed totals and status flags
5. Exceptions panel
   - unresolved issues and recommended actions
6. Action bar
   - validate, submit for approval, approve, lock, unlock (privileged)
7. Audit tab

## Key actions

- refresh run data
- re-run calculation for selected employees (controlled)
- mark exception resolved
- submit approval workflow
- lock run

## Validations

- cannot submit for approval if blockers > 0.
- cannot lock unless state = `APPROVED`.
- unlock requires `PAYROLL_SUPER_ADMIN` role and reason.
- recalculation blocked after lock.

## API contracts

- `GET /api/v1/payroll-runs/{runId}`
- `GET /api/v1/payroll-runs/{runId}/employees?page=0&size=50`
- `POST /api/v1/payroll-runs/{runId}/validate`
- `POST /api/v1/payroll-runs/{runId}/submit-approval`
- `POST /api/v1/payroll-runs/{runId}/approve`
- `POST /api/v1/payroll-runs/{runId}/lock`
- `POST /api/v1/payroll-runs/{runId}/unlock`
- `POST /api/v1/payroll-runs/{runId}/employees/{employeeId}/recalculate`
- `GET /api/v1/payroll-runs/{runId}/exceptions`
- `POST /api/v1/payroll-runs/{runId}/exceptions/{exceptionId}/resolve`

## Approve request example

```json
{
  "comment": "Validated and ready for lock"
}
```

## Unlock request example

```json
{
  "reason": "Critical bank account correction after lock"
}
```

## DDL draft additions

```sql
create table payroll.payroll_run_exception (
  id uuid primary key,
  tenant_id uuid not null,
  payroll_run_id uuid not null,
  employee_id uuid,
  issue_code varchar(60) not null,
  issue_message text not null,
  severity varchar(20) not null,
  status varchar(20) not null,
  created_at timestamptz not null,
  resolved_at timestamptz,
  resolved_by varchar(100)
);

create table payroll.payroll_run_state_transition (
  id uuid primary key,
  tenant_id uuid not null,
  payroll_run_id uuid not null,
  from_state varchar(30) not null,
  to_state varchar(30) not null,
  actor_id varchar(100) not null,
  reason text,
  created_at timestamptz not null
);
```

## Audit events

- `PAYROLL_RUN_VALIDATED`
- `PAYROLL_RUN_SUBMITTED_FOR_APPROVAL`
- `PAYROLL_RUN_APPROVED`
- `PAYROLL_RUN_LOCKED`
- `PAYROLL_RUN_UNLOCKED`
- `PAYROLL_EMPLOYEE_RECALCULATED`
- `PAYROLL_EXCEPTION_RESOLVED`

## Test cases

1. approval with blockers -> reject.
2. lock without approved state -> reject.
3. unlock without reason -> reject.
4. employee recalculation after lock -> reject.
5. state transition history must match action sequence.
