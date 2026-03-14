# Page 03 - Executive Summary and Key Drivers

## Extracted intent

- Core business value: compliance, automation, ESS, integration, analytics, scalability.
- Oman-specific statutory promise: WPS, PASI, Oman labour alignment.

## Requirements

- Mandatory compliance dashboard (WPS, PASI, payroll run health).
- Attendance-to-payroll automation.
- Configurable workflows and approvals.
- Integration status visibility.

## Screen requirements

1. `SCR-R-003 WPS Compliance Dashboard`
   - Key widgets: pending submissions, rejected files, bank-wise status.
   - Why: WPS failure blocks salary compliance.
2. `SCR-R-004 PASI Compliance Dashboard`
   - Key widgets: due contributions, variance, filing readiness.
   - Why: statutory penalties are costly and reputationally risky.
3. `SCR-PT-001 Attendance Import + Reconciliation`
   - Key actions: import, validate, fix mismatches.
   - Why: attendance defects cause salary disputes.
4. `SCR-A-007 Workflow Definition Builder`
   - Key actions: per transaction approval flow setup.
   - Why: each tenant has different governance requirements.

## API contracts

- `GET /api/v1/compliance/wps/summary`
- `GET /api/v1/compliance/pasi/summary`
- `POST /api/v1/attendance/imports`
- `POST /api/v1/workflows`
- `GET /api/v1/integrations/status`

## Rules and validations

- Attendance import must reject duplicate employee/day entries.
- Compliance summaries must be period-bound and tenant-scoped.
- Workflow activation requires at least one approver tier.

## Data objects touched

- `ComplianceSnapshot`
- `AttendanceImportBatch`
- `WorkflowDefinition`
- `IntegrationStatus`

## Acceptance criteria samples

- Given a selected payroll period, when compliance dashboard loads, then WPS and PASI summary totals are shown for the same tenant and period.
- Given an attendance file with duplicate rows, when import is executed, then duplicates are rejected with row-level error details.

## SaaS forward notes

- Keep compliance rules as country policy adapters (`OmanPolicyPack`) and dashboards policy-driven.
