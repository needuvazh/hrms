# 13 - Loan and Advance Lifecycle

## Purpose

Support employee loan and advance workflows with configurable eligibility, approvals, disbursement, and payroll recovery.

## UI sections

1. Loan and advance request inbox
2. Eligibility and policy preview panel
3. Request form (employee or HR initiated)
4. Approval workflow timeline
5. Disbursement panel
6. Recovery schedule panel
7. Outstanding and settlement actions
8. Audit tab

## Master dependencies

- `loan_type` master active
- payroll profile active for employee
- workflow definition active for loan requests
- component mapping for recovery deductions configured

## Fields

- `requestType` (`LOAN`, `ADVANCE`)
- `loanTypeId` (for loan)
- `employeeId`
- `requestAmount`
- `requestedTenureMonths` (loan)
- `interestRate` (optional policy override)
- `reason`
- `requestedDisbursementDate`
- `recoveryStartPeriod`
- `status`

## Validations

- amount must be within loan type min/max.
- eligibility must satisfy tenure, grade, and outstanding balance rules.
- recovery start period must be after approval date.
- disbursement cannot happen before approval.

## API contracts

- `POST /api/v1/loans/requests`
- `GET /api/v1/loans/requests?status=PENDING&page=0&size=20`
- `GET /api/v1/loans/requests/{requestId}`
- `POST /api/v1/loans/requests/{requestId}/submit-approval`
- `POST /api/v1/loans/requests/{requestId}/approve`
- `POST /api/v1/loans/requests/{requestId}/reject`
- `POST /api/v1/loans/{loanId}/disburse`
- `GET /api/v1/loans/{loanId}/schedule`
- `POST /api/v1/loans/{loanId}/restructure`
- `POST /api/v1/loans/{loanId}/close`

## Create request example

```json
{
  "requestType": "LOAN",
  "loanTypeId": "27b4978b-bd66-48db-95fc-81e3809132e8",
  "employeeId": "a0c3c13f-61f1-4638-8a32-2f8f4fdd76ee",
  "requestAmount": 600.000,
  "requestedTenureMonths": 12,
  "reason": "Education support",
  "requestedDisbursementDate": "2026-05-05",
  "recoveryStartPeriod": "2026-06"
}
```

## DDL draft additions

```sql
create table payroll.loan_request (
  id uuid primary key,
  tenant_id uuid not null,
  request_type varchar(20) not null,
  loan_type_id uuid,
  employee_id uuid not null,
  request_amount numeric(18,3) not null,
  requested_tenure_months int,
  interest_rate numeric(8,5),
  reason text,
  requested_disbursement_date date,
  recovery_start_period varchar(7),
  status varchar(30) not null,
  workflow_instance_id uuid,
  created_at timestamptz not null,
  created_by varchar(100) not null,
  updated_at timestamptz not null,
  updated_by varchar(100) not null
);

create table payroll.loan_schedule (
  id uuid primary key,
  tenant_id uuid not null,
  loan_id uuid not null,
  installment_no int not null,
  period_code varchar(7) not null,
  principal_amount numeric(18,3) not null,
  interest_amount numeric(18,3) not null,
  total_amount numeric(18,3) not null,
  recovery_status varchar(20) not null
);
```

## Audit events

- `LOAN_REQUEST_CREATED`
- `LOAN_REQUEST_SUBMITTED_FOR_APPROVAL`
- `LOAN_REQUEST_APPROVED`
- `LOAN_REQUEST_REJECTED`
- `LOAN_DISBURSED`
- `LOAN_SCHEDULE_RESTRUCTURED`
- `LOAN_CLOSED`

## Test cases

1. request above maximum policy amount -> reject.
2. approve and disburse -> schedule generated correctly.
3. payroll deduction consumes next due installment.
4. close loan marks future installments canceled.
