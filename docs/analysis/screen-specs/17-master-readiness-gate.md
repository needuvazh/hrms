# 17 - Master Readiness Gate

## Purpose

Block payroll transactions until mandatory master setup is complete and valid.

This prevents partial configuration from causing incorrect payroll output.

## UI sections

1. Readiness score card
   - overall percentage and status (`NOT_READY`, `PARTIAL`, `READY`)
2. Checklist categories
   - organization
   - payroll definitions
   - statutory setup
   - workflow and security
3. Missing items panel
4. Fix navigation links to related master screens
5. Override history panel (if emergency bypass is allowed)

## Mandatory checks

1. legal entity exists and active
2. payroll calendar active
3. payroll group active with non-zero population
4. pay components active
5. formulas validated and active
6. salary structures active
7. employee payroll profiles complete
8. bank mappings complete for bank transfer employees
9. PASI policy active for period (Oman)
10. WPS profile active for period (Oman)
11. EOSB rules active (Oman)
12. workflow approvals active
13. role permissions configured for payroll actions

## API contracts

- `GET /api/v1/payroll/readiness?periodYear=2026&periodMonth=4&groupId=...`
- `POST /api/v1/payroll/readiness/recheck`
- `GET /api/v1/payroll/readiness/missing-items`
- `POST /api/v1/payroll/readiness/override` (restricted)

## Readiness response example

```json
{
  "status": "PARTIAL",
  "score": 84,
  "blockingItems": [
    {
      "code": "MISSING_EMPLOYEE_BANK_ACCOUNTS",
      "count": 2,
      "navigateTo": "employees/bank-accounts"
    }
  ],
  "warningItems": [
    {
      "code": "LOW_APPROVAL_CAPACITY",
      "count": 1
    }
  ]
}
```

## Override policy

- emergency override only for `PAYROLL_SUPER_ADMIN`.
- mandatory reason and expiry time.
- override action must be visible in workbench and audit.

## DDL draft additions

```sql
create table payroll.readiness_override (
  id uuid primary key,
  tenant_id uuid not null,
  payroll_group_id uuid not null,
  period_year int not null,
  period_month int not null,
  reason text not null,
  expires_at timestamptz not null,
  created_at timestamptz not null,
  created_by varchar(100) not null
);
```

## Audit events

- `PAYROLL_READINESS_EVALUATED`
- `PAYROLL_READINESS_OVERRIDE_CREATED`
- `PAYROLL_READINESS_OVERRIDE_EXPIRED`

## Test cases

1. readiness check with missing PASI policy -> status not ready.
2. readiness check with all masters complete -> status ready.
3. override without privileged role -> forbidden.
4. expired override cannot be used for approval/lock transitions.
