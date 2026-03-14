# 12 - EOSB Estimate and Final Settlement (Oman)

## Purpose

Provide legally compliant end-of-service benefit calculations for estimates and final settlements.

## UI sections

1. Employee and separation context
   - exit type, last working day, settlement period
2. EOSB rule preview
   - policy version, service year bands, reduction factors
3. Estimate result panel
   - gratuity, leave encashment, deductions, net settlement
4. Final settlement editor
   - additional recoveries/dues and document checklist
5. Approval workflow panel
6. Settlement document generation panel
7. Audit tab

## Required validations

- employee must have active employment history.
- rule version must match last working day effective date.
- final settlement cannot be approved without mandatory clearance checklist.

## API contracts

- `POST /api/v1/compliance/oman/eosb/estimates`
- `GET /api/v1/compliance/oman/eosb/estimates/{estimateId}`
- `POST /api/v1/compliance/oman/eosb/settlements`
- `GET /api/v1/compliance/oman/eosb/settlements/{settlementId}`
- `POST /api/v1/compliance/oman/eosb/settlements/{settlementId}/submit-approval`
- `POST /api/v1/compliance/oman/eosb/settlements/{settlementId}/approve`
- `POST /api/v1/compliance/oman/eosb/settlements/{settlementId}/generate-document`

## Estimate request example

```json
{
  "employeeId": "123b7ddf-746b-4dba-baf8-715657f595ac",
  "exitType": "RESIGNATION",
  "lastWorkingDay": "2026-04-30",
  "includeLeaveEncashment": true
}
```

## Settlement request example

```json
{
  "estimateId": "5f1754eb-9fdb-4e1c-b23f-0ecf9043b6f8",
  "additionalDeductions": [
    {"code": "ASSET_RECOVERY", "amount": 35.000}
  ],
  "clearanceChecklist": {
    "itAssetsReturned": true,
    "idCardReturned": true,
    "accommodationCleared": true
  }
}
```

## DDL draft additions

```sql
create table payroll.oman_eosb_estimate (
  id uuid primary key,
  tenant_id uuid not null,
  employee_id uuid not null,
  exit_type varchar(30) not null,
  last_working_day date not null,
  policy_id uuid not null,
  gratuity_amount numeric(18,3) not null,
  leave_encashment_amount numeric(18,3) not null,
  deduction_amount numeric(18,3) not null,
  net_amount numeric(18,3) not null,
  created_at timestamptz not null,
  created_by varchar(100) not null
);

create table payroll.oman_eosb_settlement (
  id uuid primary key,
  tenant_id uuid not null,
  estimate_id uuid not null,
  status varchar(30) not null,
  approved_at timestamptz,
  approved_by varchar(100),
  settlement_document_uri text,
  created_at timestamptz not null,
  created_by varchar(100) not null
);
```

## Audit events

- `OMAN_EOSB_ESTIMATE_CREATED`
- `OMAN_EOSB_SETTLEMENT_CREATED`
- `OMAN_EOSB_SETTLEMENT_SUBMITTED_FOR_APPROVAL`
- `OMAN_EOSB_SETTLEMENT_APPROVED`
- `OMAN_EOSB_DOCUMENT_GENERATED`

## Test cases

1. estimate with invalid service dates -> reject.
2. settlement approval without checklist completion -> reject.
3. policy version on estimate must be persisted and reproducible.
4. generated settlement document checksum and URI saved.
