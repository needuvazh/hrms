# 01 - Pay Component Master

## Purpose

Configure reusable payroll components dynamically so payroll logic is not hardcoded.

## UI sections

1. Header
   - title, tenant/legal entity context, create button
2. Filter bar
   - component type, status, effective date, keyword
3. Grid
   - code, name, type, mode, effective range, status, updated by
4. Drawer or form page
   - create/edit fields
5. Version timeline tab
   - all effective-dated versions
6. Audit tab
   - actor, action, before/after diff

## Fields

- `componentCode` (string, 3-30)
- `componentName` (string, 3-120)
- `componentType` (`EARNING`, `DEDUCTION`, `CONTRIBUTION`, `PROVISION`)
- `calculationMode` (`FIXED`, `FORMULA`, `INPUT`, `REFERENCE`)
- `defaultValue` (decimal 18,3, optional)
- `currencyCode` (ISO code)
- `affectsGross` (boolean)
- `affectsNet` (boolean)
- `isTaxable` (boolean)
- `displayOrder` (int)
- `effectiveFrom` (date)
- `effectiveTo` (date, nullable)
- `status` (`DRAFT`, `ACTIVE`, `RETIRED`)

## Validations

- `componentCode` unique per tenant.
- `effectiveTo` must be greater than or equal to `effectiveFrom`.
- `defaultValue` required when `calculationMode = FIXED`.
- cannot retire when referenced by active salary structures unless future-dated replacement exists.

## Permissions

- HR Admin: create/edit draft
- Payroll Admin: activate/retire
- Auditor: read-only

## API contracts

## Create

`POST /api/v1/pay-components`

```json
{
  "componentCode": "BASIC",
  "componentName": "Basic Salary",
  "componentType": "EARNING",
  "calculationMode": "FIXED",
  "defaultValue": 0,
  "currencyCode": "OMR",
  "affectsGross": true,
  "affectsNet": true,
  "isTaxable": false,
  "displayOrder": 10,
  "effectiveFrom": "2026-04-01"
}
```

## List

`GET /api/v1/pay-components?page=0&size=20&type=EARNING&status=ACTIVE`

## Update

`PUT /api/v1/pay-components/{componentId}`

## Activate

`POST /api/v1/pay-components/{componentId}/activate`

## Retire

`POST /api/v1/pay-components/{componentId}/retire`

```json
{
  "reason": "Replaced by new transport policy",
  "effectiveTo": "2026-12-31"
}
```

## DDL draft

```sql
create table payroll.pay_component (
  id uuid primary key,
  tenant_id uuid not null,
  component_code varchar(30) not null,
  component_name varchar(120) not null,
  component_type varchar(30) not null,
  calculation_mode varchar(30) not null,
  default_value numeric(18,3),
  currency_code varchar(3) not null,
  affects_gross boolean not null,
  affects_net boolean not null,
  is_taxable boolean not null,
  display_order int not null,
  effective_from date not null,
  effective_to date,
  status varchar(20) not null,
  version int not null,
  created_at timestamptz not null,
  created_by varchar(100) not null,
  updated_at timestamptz not null,
  updated_by varchar(100) not null,
  unique (tenant_id, component_code, version)
);
```

## Audit events

- `PAY_COMPONENT_CREATED`
- `PAY_COMPONENT_UPDATED`
- `PAY_COMPONENT_ACTIVATED`
- `PAY_COMPONENT_RETIRED`

## Test cases

1. create component with duplicate code -> reject with conflict.
2. activate draft -> status changes to active and audit saved.
3. retire active referenced component without replacement -> reject.
4. list filtering by type/status returns tenant-scoped data only.
