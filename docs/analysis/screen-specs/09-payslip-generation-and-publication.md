# 09 - Payslip Generation and Publication

## Purpose

Generate legally compliant payslips and deliver them through ESS, email, and secure storage.

## UI sections

1. Run selection header
2. Payslip profile selection (template/language/password policy)
3. Generation status panel
4. Employee delivery status grid
5. Retry and publish actions
6. Document preview panel
7. Audit tab

## Fields and settings

- `runId`
- `payslipProfileId`
- `languageMode` (`EN`, `AR`, `BILINGUAL`)
- `passwordPolicy` (`DOB_LAST4`, `EMP_CODE_DOB`, `CUSTOM_RULE`)
- `watermarkText`
- `deliveryChannels[]` (`ESS`, `EMAIL`)
- `publishDate`

## Validations

- payslip generation allowed only for `LOCKED` runs.
- email delivery requires active employee email.
- template profile must be active and effective-dated.

## API contracts

- `POST /api/v1/payroll-runs/{runId}/payslips/generate`
- `GET /api/v1/payroll-runs/{runId}/payslips/status`
- `POST /api/v1/payroll-runs/{runId}/payslips/publish`
- `POST /api/v1/payroll-runs/{runId}/payslips/delivery/retry`
- `GET /api/v1/employees/{employeeId}/payslips`
- `GET /api/v1/payslips/{payslipId}/download`

## Generate request example

```json
{
  "payslipProfileId": "7b90f2f3-ab28-4de4-9731-4f4c3f7f9728",
  "languageMode": "BILINGUAL",
  "passwordPolicy": "EMP_CODE_DOB",
  "watermarkText": "Confidential",
  "deliveryChannels": ["ESS", "EMAIL"]
}
```

## DDL draft additions

```sql
create table payroll.payslip (
  id uuid primary key,
  tenant_id uuid not null,
  payroll_run_id uuid not null,
  employee_id uuid not null,
  payslip_no varchar(40) not null,
  period_year int not null,
  period_month int not null,
  language_mode varchar(20) not null,
  document_uri text not null,
  checksum varchar(128) not null,
  published_at timestamptz,
  created_at timestamptz not null,
  created_by varchar(100) not null,
  unique (tenant_id, payslip_no)
);

create table payroll.payslip_delivery (
  id uuid primary key,
  tenant_id uuid not null,
  payslip_id uuid not null,
  channel varchar(20) not null,
  status varchar(20) not null,
  attempts int not null,
  last_error text,
  delivered_at timestamptz
);
```

## Audit events

- `PAYSLIP_GENERATION_STARTED`
- `PAYSLIP_GENERATION_COMPLETED`
- `PAYSLIP_PUBLISHED`
- `PAYSLIP_DELIVERY_RETRIED`

## Test cases

1. generate payslips on non-locked run -> reject.
2. publish before generation complete -> reject.
3. employee can download only own payslip in ESS.
4. retry delivery updates attempts and error details.
