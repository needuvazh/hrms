# 03 - Salary Structure Master

## Purpose

Create reusable salary templates and assign component composition by policy.

## UI sections

1. Structure list
2. Header config (entity, grade, currency)
3. Component lines editor
4. Validation summary (gross/net sanity checks)
5. Version and audit tabs

## Fields

- `structureCode`
- `structureName`
- `legalEntityId`
- `gradeId` (optional)
- `currencyCode`
- `componentLines[]` with:
  - `componentId`
  - `lineMode` (`FIXED`, `FORMULA`, `INPUT`)
  - `amount` (if fixed)
  - `formulaId` (if formula mode)
  - `sequenceNo`
- `effectiveFrom`
- `effectiveTo`
- `status`

## Validations

- at least one active `EARNING` line required.
- each line component unique within structure version.
- fixed line requires amount.
- formula line requires active formula id.

## API contracts

## Create

`POST /api/v1/salary-structures`

```json
{
  "structureCode": "STAFF_STD_OMR",
  "structureName": "Staff Standard OMR",
  "legalEntityId": "a6a54762-1607-4a24-b98f-4f37de9c6a5a",
  "gradeId": "2a5e53df-f988-4d77-8b87-6f1f507fbc78",
  "currencyCode": "OMR",
  "componentLines": [
    {"componentId": "BASIC-ID", "lineMode": "FIXED", "amount": 350.000, "sequenceNo": 10},
    {"componentId": "HRA-ID", "lineMode": "FORMULA", "formulaId": "HRA-40-ID", "sequenceNo": 20}
  ],
  "effectiveFrom": "2026-04-01"
}
```

## Activate

`POST /api/v1/salary-structures/{structureId}/activate`

## DDL draft

```sql
create table payroll.salary_structure (
  id uuid primary key,
  tenant_id uuid not null,
  structure_code varchar(40) not null,
  structure_name varchar(150) not null,
  legal_entity_id uuid not null,
  grade_id uuid,
  currency_code varchar(3) not null,
  effective_from date not null,
  effective_to date,
  status varchar(20) not null,
  version int not null,
  created_at timestamptz not null,
  created_by varchar(100) not null,
  updated_at timestamptz not null,
  updated_by varchar(100) not null,
  unique (tenant_id, structure_code, version)
);

create table payroll.salary_structure_line (
  id uuid primary key,
  tenant_id uuid not null,
  salary_structure_id uuid not null,
  component_id uuid not null,
  line_mode varchar(20) not null,
  amount numeric(18,3),
  formula_id uuid,
  sequence_no int not null
);
```

## Audit events

- `SALARY_STRUCTURE_CREATED`
- `SALARY_STRUCTURE_UPDATED`
- `SALARY_STRUCTURE_ACTIVATED`

## Test cases

1. create with no earnings line -> reject.
2. create fixed line without amount -> reject.
3. activate and verify versioned retrieval by effective date.
4. ensure tenant cannot access another tenant structure code.
