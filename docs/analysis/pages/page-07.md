# Page 07 - Payroll Engine Configuration and Controls

## Extracted intent

- Configurable payroll engine with simulation, grouping, lock controls, and pre-checks.

## Requirements

- Payroll workbench with controlled lifecycle.
- Group-wise run capability.
- Pre-payroll readiness checklist.
- Lock/unlock with full audit and authorization.

## Screen requirements

1. `SCR-PT-003 Pre-Payroll Checklist`
   - Why: prevents processing on incomplete attendance/leave inputs.
2. `SCR-PT-004 Payroll Simulation`
   - Why: detect anomalies before financial commitment.
3. `SCR-PT-005 Payroll Run Workbench`
   - Why: central control point for calculate/validate/approve/lock.
4. `SCR-PM-001 Payroll Calendar Master` and `SCR-PM-002 Payroll Group Master`
   - Why: processing windows and group segmentation are foundational.

## API contracts

- `POST /api/v1/payroll-runs/simulate`
- `POST /api/v1/payroll-runs`
- `POST /api/v1/payroll-runs/{runId}/validate`
- `POST /api/v1/payroll-runs/{runId}/approve`
- `POST /api/v1/payroll-runs/{runId}/lock`

## Rules and validations

- Lock action allowed only after all checklist items pass.
- Unlock action requires elevated role and mandatory reason.

## Data objects touched

- `PayrollCalendar`
- `PayrollGroup`
- `PayrollRun`
- `PayrollChecklistResult`

## Acceptance criteria samples

- Given checklist item failure (for example pending leave approvals), when payroll lock is attempted, then lock is rejected with blocking items.
- Given approved payroll run, when lock is executed, then run becomes immutable for line-item edits and audit event is stored.

## SaaS forward notes

- Preserve immutable snapshot of run inputs for legal reproducibility.
