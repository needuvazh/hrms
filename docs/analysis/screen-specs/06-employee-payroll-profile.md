# 06 - Employee Payroll Profile

## Purpose

Bind each employee to payroll settings, statutory eligibility, and payment channel.

## UI sections

1. Employee search header
2. Current payroll profile card
3. Edit form
4. History/version timeline
5. Audit tab

## Fields

- `employeeId`
- `payrollGroupId`
- `salaryStructureId`
- `paymentMode` (`BANK_TRANSFER`, `CASH`, `CHEQUE`)
- `bankAccountId` (required for bank transfer)
- `pasiEligible`
- `eosbEligible`
- `airTicketEligible`
- `loanRecoveryEnabled`
- `effectiveFrom`
- `effectiveTo`
- `status`

## Validations

- employee must be active and assigned to tenant legal entity.
- bank account required for `BANK_TRANSFER`.
- salary structure must be active on effective date.
- no overlapping active profile versions per employee.

## API contracts

- `PUT /api/v1/employees/{employeeId}/payroll-profile`
- `GET /api/v1/employees/{employeeId}/payroll-profile`
- `GET /api/v1/employees/{employeeId}/payroll-profile/history`

## Update example

```json
{
  "payrollGroupId": "df9f60b9-bf0a-42f2-a03e-0354fc6a9f5c",
  "salaryStructureId": "1f06be57-92d6-4ec7-b2cf-84cb7371cf6c",
  "paymentMode": "BANK_TRANSFER",
  "bankAccountId": "1cc8717d-7500-460f-a80a-fd53ad95f63f",
  "pasiEligible": true,
  "eosbEligible": true,
  "airTicketEligible": true,
  "loanRecoveryEnabled": true,
  "effectiveFrom": "2026-04-01"
}
```

## DDL draft

```sql
create table payroll.employee_payroll_profile (
  id uuid primary key,
  tenant_id uuid not null,
  employee_id uuid not null,
  payroll_group_id uuid not null,
  salary_structure_id uuid not null,
  payment_mode varchar(20) not null,
  bank_account_id uuid,
  pasi_eligible boolean not null,
  eosb_eligible boolean not null,
  air_ticket_eligible boolean not null,
  loan_recovery_enabled boolean not null,
  effective_from date not null,
  effective_to date,
  status varchar(20) not null,
  version int not null,
  created_at timestamptz not null,
  created_by varchar(100) not null,
  updated_at timestamptz not null,
  updated_by varchar(100) not null,
  unique (tenant_id, employee_id, version)
);
```

## Audit events

- `EMPLOYEE_PAYROLL_PROFILE_ASSIGNED`
- `EMPLOYEE_PAYROLL_PROFILE_UPDATED`

## Test cases

1. update with inactive salary structure -> reject.
2. bank transfer mode without bank account -> reject.
3. profile history retrieval returns ordered versions with effective dates.
