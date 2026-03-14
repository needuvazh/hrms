# 10 - WPS Generation and Submission Tracking (Oman)

## Purpose

Generate compliant WPS SIF files and track submission lifecycle to banks/regulators.

## UI sections

1. Run and WPS profile selector
2. Pre-validation panel
   - missing bank account
   - invalid IBAN/account
   - zero-net salary anomalies
3. File generation summary
4. Submission tracker grid
5. Response status update panel
6. Archive and download tab
7. Audit tab

## Required validations

- payroll run must be `LOCKED`.
- all included employees must have valid payment accounts.
- WPS profile version must be active for run period.

## API contracts

- `POST /api/v1/compliance/oman/wps-files/generate`
- `GET /api/v1/compliance/oman/wps-files/{wpsFileId}`
- `GET /api/v1/compliance/oman/wps-files/{wpsFileId}/download`
- `POST /api/v1/compliance/oman/wps-submissions`
- `PUT /api/v1/compliance/oman/wps-submissions/{submissionId}/status`
- `GET /api/v1/compliance/oman/wps-submissions?periodYear=2026&periodMonth=4`

## Generate request example

```json
{
  "runId": "2cb64f1c-fda4-420e-81b6-88df4f249f13",
  "wpsProfileId": "3176c165-cf5a-4835-b133-b7c9d5ec8f7f",
  "includeZeroNet": false
}
```

## Submission status update example

```json
{
  "status": "SUBMITTED",
  "referenceNo": "BANK-REF-2026-04-0093",
  "comment": "Uploaded to bank portal"
}
```

## DDL draft additions

```sql
create table payroll.oman_wps_file (
  id uuid primary key,
  tenant_id uuid not null,
  payroll_run_id uuid not null,
  wps_profile_id uuid not null,
  file_name varchar(255) not null,
  file_uri text not null,
  checksum varchar(128) not null,
  employee_count int not null,
  total_net_amount numeric(18,3) not null,
  created_at timestamptz not null,
  created_by varchar(100) not null
);

create table payroll.oman_wps_submission (
  id uuid primary key,
  tenant_id uuid not null,
  wps_file_id uuid not null,
  status varchar(30) not null,
  reference_no varchar(120),
  comment text,
  updated_at timestamptz not null,
  updated_by varchar(100) not null
);
```

## Audit events

- `OMAN_WPS_FILE_GENERATED`
- `OMAN_WPS_SUBMISSION_CREATED`
- `OMAN_WPS_SUBMISSION_STATUS_UPDATED`

## Test cases

1. generate WPS with invalid bank details -> reject with employee list.
2. generate WPS for unlocked run -> reject.
3. update submission status to rejected and verify dashboard reflects issue.
4. downloaded file checksum must match persisted checksum.
