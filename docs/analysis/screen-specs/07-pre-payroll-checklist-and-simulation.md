# 07 - Pre-Payroll Checklist and Simulation

## Purpose

Prevent invalid payroll execution through dependency checks, then run simulation before approval.

## UI sections

1. Run context header (period + payroll group)
2. Checklist panel
   - attendance reconciled
   - leave finalized
   - variable inputs locked
   - employee payroll profile completeness
   - statutory masters availability
3. Blocking issues table
4. Simulation action panel
5. Simulation result summary and drilldown
6. Audit and timeline tabs

## Checklist statuses

- `PASS`
- `WARNING`
- `BLOCKER`

`BLOCKER` must stop transition to calculation/approval.

## API contracts

## Create run draft

`POST /api/v1/payroll-runs`

```json
{
  "payrollGroupId": "df9f60b9-bf0a-42f2-a03e-0354fc6a9f5c",
  "periodYear": 2026,
  "periodMonth": 4
}
```

## Checklist

- `GET /api/v1/payroll-runs/{runId}/checklist`
- `POST /api/v1/payroll-runs/{runId}/checklist/recheck`

## Simulation

- `POST /api/v1/payroll-runs/{runId}/simulate`
- `GET /api/v1/payroll-runs/{runId}/simulation-result`

## Approve and lock

- `POST /api/v1/payroll-runs/{runId}/approve`
- `POST /api/v1/payroll-runs/{runId}/lock`

## Simulation result shape

```json
{
  "runId": "2cb64f1c-fda4-420e-81b6-88df4f249f13",
  "employeesProcessed": 84,
  "grossTotal": 48210.550,
  "deductionTotal": 7820.250,
  "netTotal": 40390.300,
  "warnings": 3,
  "blockers": 0
}
```

## DDL draft

```sql
create table payroll.payroll_run (
  id uuid primary key,
  tenant_id uuid not null,
  payroll_group_id uuid not null,
  period_year int not null,
  period_month int not null,
  state varchar(30) not null,
  checklist_json jsonb not null,
  simulation_summary jsonb,
  locked_at timestamptz,
  locked_by varchar(100),
  created_at timestamptz not null,
  created_by varchar(100) not null,
  updated_at timestamptz not null,
  updated_by varchar(100) not null,
  unique (tenant_id, payroll_group_id, period_year, period_month)
);
```

## Transition rules

- `DRAFT -> INPUT_READY` if no blockers.
- `INPUT_READY -> CALCULATED` via simulation.
- `CALCULATED -> APPROVED` via workflow approval.
- `APPROVED -> LOCKED` only when checklist still pass.

## Audit events

- `PAYROLL_RUN_CREATED`
- `PAYROLL_CHECKLIST_EVALUATED`
- `PAYROLL_SIMULATION_COMPLETED`
- `PAYROLL_RUN_APPROVED`
- `PAYROLL_RUN_LOCKED`

## Test cases

1. checklist contains blocker -> approve rejected.
2. simulation totals deterministic with same inputs.
3. lock after approval succeeds and disallows line edits.
4. concurrent lock attempts -> one succeeds, one conflict.
