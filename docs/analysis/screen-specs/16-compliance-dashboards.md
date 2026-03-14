# 16 - Compliance Dashboards (WPS, PASI, Payroll Controls)

## Purpose

Provide compliance and control visibility for payroll leadership and auditors.

## Dashboard widgets

1. WPS status
   - generated, submitted, accepted, rejected counts
2. PASI filing status
   - pending runs, filed runs, anomalies
3. Payroll control status
   - runs locked, unlocked events, overdue approvals
4. Data quality status
   - missing bank accounts, missing payroll profiles, missing statutory flags
5. Exception trend
   - month-wise blocker and warning counts

## Drill-down screens

- WPS submission list
- PASI run list
- Payroll unlock history
- Data quality issues list

## API contracts

- `GET /api/v1/compliance/dashboard/summary?periodYear=2026&periodMonth=4`
- `GET /api/v1/compliance/wps/summary`
- `GET /api/v1/compliance/pasi/summary`
- `GET /api/v1/compliance/payroll-controls`
- `GET /api/v1/compliance/data-quality-issues?page=0&size=50`

## Summary response example

```json
{
  "period": "2026-04",
  "wps": {"generated": 1, "submitted": 1, "accepted": 1, "rejected": 0},
  "pasi": {"runs": 1, "filed": 1, "pending": 0},
  "controls": {"lockedRuns": 1, "unlockEvents": 0, "overdueApprovals": 0},
  "dataQuality": {"missingBankAccounts": 0, "missingProfiles": 1}
}
```

## DDL draft additions

No mandatory new tables. Prefer materialized views or query projections from audit + transaction tables.

Suggested read models:

- `reporting.compliance_snapshot`
- `reporting.data_quality_issue`

## Audit events (consumed)

- `OMAN_WPS_SUBMISSION_STATUS_UPDATED`
- `OMAN_PASI_FILING_STATUS_UPDATED`
- `PAYROLL_RUN_LOCKED`
- `PAYROLL_RUN_UNLOCKED`

## Test cases

1. dashboard totals must match source transactional counts.
2. tenant A cannot view tenant B compliance metrics.
3. rejected WPS status appears in drill-down with reference number.
