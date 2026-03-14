# 14 - GL Export and Reconciliation

## Purpose

Generate accounting entries from payroll and support reconciliation with finance systems.

## UI sections

1. Run selector and export profile
2. GL summary cards (debit total, credit total, imbalance)
3. Journal line grid with drill-down
4. Validation panel (missing mappings, invalid accounts)
5. Export action and delivery status
6. Reconciliation status panel
7. Audit tab

## Master dependencies

- active GL mapping
- active cost center mappings
- export profile (CSV/API)

## Validations

- debit and credit totals must match.
- every payroll component used in run must have active GL mapping.
- export only for locked runs.

## API contracts

- `POST /api/v1/gl-exports`
- `GET /api/v1/gl-exports/{exportId}`
- `GET /api/v1/gl-exports/{exportId}/lines?page=0&size=200`
- `GET /api/v1/gl-exports/{exportId}/download`
- `POST /api/v1/gl-exports/{exportId}/mark-reconciled`
- `GET /api/v1/gl-exports?periodYear=2026&periodMonth=4`

## Create request example

```json
{
  "runId": "2cb64f1c-fda4-420e-81b6-88df4f249f13",
  "exportProfile": "CSV_SAP_V1",
  "includeCostCenter": true
}
```

## Mark reconciled example

```json
{
  "referenceNo": "SAP-JRN-2026-04-009",
  "comment": "Posted and matched in finance system"
}
```

## DDL draft additions

```sql
create table payroll.gl_export (
  id uuid primary key,
  tenant_id uuid not null,
  payroll_run_id uuid not null,
  export_profile varchar(40) not null,
  status varchar(30) not null,
  debit_total numeric(18,3) not null,
  credit_total numeric(18,3) not null,
  file_uri text,
  checksum varchar(128),
  reconciled_at timestamptz,
  reconciled_by varchar(100),
  created_at timestamptz not null,
  created_by varchar(100) not null
);

create table payroll.gl_export_line (
  id uuid primary key,
  tenant_id uuid not null,
  gl_export_id uuid not null,
  account_code varchar(40) not null,
  dr_amount numeric(18,3) not null,
  cr_amount numeric(18,3) not null,
  cost_center_code varchar(40),
  narration text
);
```

## Audit events

- `GL_EXPORT_CREATED`
- `GL_EXPORT_FILE_GENERATED`
- `GL_EXPORT_RECONCILED`

## Test cases

1. export with missing component mapping -> reject and list component ids.
2. export on unlocked run -> reject.
3. debit-credit mismatch -> block export.
4. reconciliation updates report status and audit trail.
