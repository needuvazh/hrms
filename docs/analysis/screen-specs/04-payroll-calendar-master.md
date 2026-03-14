# 04 - Payroll Calendar Master

## Purpose

Define payroll periods, cutoffs, and pay date rules used by runs.

## UI sections

1. Calendar list
2. Calendar setup form
3. Period preview table (next 12 periods)
4. Cutoff validation alerts
5. Audit tab

## Fields

- `calendarCode`
- `calendarName`
- `frequency` (`MONTHLY`, `BI_MONTHLY`, `WEEKLY`)
- `periodStartRule`
- `periodEndRule`
- `attendanceCutoffDay`
- `leaveCutoffDay`
- `variableInputCutoffDay`
- `payDateRule`
- `timezone`
- `effectiveFrom`
- `effectiveTo`
- `status`

## Validations

- cutoff days must fall within period.
- pay date must be after input cutoffs.
- no overlapping active calendars for same payroll group mapping.

## API contracts

- `POST /api/v1/payroll-calendars`
- `GET /api/v1/payroll-calendars`
- `GET /api/v1/payroll-calendars/{calendarId}`
- `PUT /api/v1/payroll-calendars/{calendarId}`
- `POST /api/v1/payroll-calendars/{calendarId}/activate`

## Create example

```json
{
  "calendarCode": "OM-MONTHLY",
  "calendarName": "Oman Monthly Payroll",
  "frequency": "MONTHLY",
  "periodStartRule": "FIRST_DAY",
  "periodEndRule": "LAST_DAY",
  "attendanceCutoffDay": 25,
  "leaveCutoffDay": 25,
  "variableInputCutoffDay": 26,
  "payDateRule": "LAST_WORKING_DAY",
  "timezone": "Asia/Muscat",
  "effectiveFrom": "2026-04-01"
}
```

## DDL draft

```sql
create table payroll.payroll_calendar (
  id uuid primary key,
  tenant_id uuid not null,
  calendar_code varchar(30) not null,
  calendar_name varchar(120) not null,
  frequency varchar(20) not null,
  period_start_rule varchar(30) not null,
  period_end_rule varchar(30) not null,
  attendance_cutoff_day int not null,
  leave_cutoff_day int not null,
  variable_input_cutoff_day int not null,
  pay_date_rule varchar(40) not null,
  timezone varchar(60) not null,
  effective_from date not null,
  effective_to date,
  status varchar(20) not null,
  version int not null,
  created_at timestamptz not null,
  created_by varchar(100) not null,
  updated_at timestamptz not null,
  updated_by varchar(100) not null,
  unique (tenant_id, calendar_code, version)
);
```

## Audit events

- `PAYROLL_CALENDAR_CREATED`
- `PAYROLL_CALENDAR_UPDATED`
- `PAYROLL_CALENDAR_ACTIVATED`

## Test cases

1. create calendar with invalid cutoff sequence -> reject.
2. activate calendar and preview periods -> periods generated accurately.
3. overlapping active calendar for same scope -> reject.
