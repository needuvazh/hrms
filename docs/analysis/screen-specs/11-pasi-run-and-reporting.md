# 11 - PASI Run and Reporting (Oman)

## Purpose

Compute employer/employee PASI contributions and produce filing-ready reports.

## UI sections

1. Run selector and policy version indicator
2. PASI calculation summary
3. Employee contribution grid
4. Variance and exception panel
5. Report generation and export actions
6. Filing status and notes
7. Audit tab

## Required validations

- payroll run must be `LOCKED`.
- PASI policy must be active for run period.
- employee category and eligibility must be present.

## API contracts

- `POST /api/v1/compliance/oman/pasi-runs`
- `GET /api/v1/compliance/oman/pasi-runs/{pasiRunId}`
- `GET /api/v1/compliance/oman/pasi-runs/{pasiRunId}/employees?page=0&size=100`
- `POST /api/v1/compliance/oman/pasi-runs/{pasiRunId}/generate-report`
- `GET /api/v1/compliance/oman/pasi-runs/{pasiRunId}/report/download`
- `PUT /api/v1/compliance/oman/pasi-runs/{pasiRunId}/filing-status`

## Run request example

```json
{
  "runId": "2cb64f1c-fda4-420e-81b6-88df4f249f13",
  "policyId": "ae4d08af-e4af-4f74-b971-21df26ce0544"
}
```

## Filing status update example

```json
{
  "status": "FILED",
  "referenceNo": "PASI-APR-2026-112",
  "comment": "Filed via portal"
}
```

## DDL draft additions

```sql
create table payroll.oman_pasi_run (
  id uuid primary key,
  tenant_id uuid not null,
  payroll_run_id uuid not null,
  policy_id uuid not null,
  status varchar(30) not null,
  employee_total numeric(18,3) not null,
  employer_total numeric(18,3) not null,
  report_uri text,
  created_at timestamptz not null,
  created_by varchar(100) not null
);

create table payroll.oman_pasi_run_line (
  id uuid primary key,
  tenant_id uuid not null,
  pasi_run_id uuid not null,
  employee_id uuid not null,
  contributable_wage numeric(18,3) not null,
  employee_contribution numeric(18,3) not null,
  employer_contribution numeric(18,3) not null,
  status varchar(20) not null,
  issue_message text
);
```

## Audit events

- `OMAN_PASI_RUN_CREATED`
- `OMAN_PASI_REPORT_GENERATED`
- `OMAN_PASI_FILING_STATUS_UPDATED`

## Test cases

1. run PASI without active policy -> reject.
2. verify contribution precision and policy ceiling behavior.
3. report generation includes all eligible employees only.
4. filing status changes reflected in compliance dashboard.
