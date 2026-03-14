# Page 08 - Earnings, Deductions, WPS, PASI, EOSB, Air Ticket

## Extracted intent

- Main statutory and compensation mechanics are defined here.

## Requirements

- Configurable salary component framework.
- WPS SIF generation and submission tracking.
- PASI auto-calculation and reporting.
- EOSB and air-ticket calculations.

## Screen requirements

1. `SCR-PM-003 Salary Component Master`
   - Why: avoid hardcoded payroll elements.
2. `SCR-PT-008 WPS File Generation and Tracking`
   - Why: mandatory Oman salary disbursement format.
3. `SCR-PT-009 PASI Run and Reporting`
   - Why: mandatory employer/employee contribution compliance.
4. `SCR-PT-010 EOSB Estimate and Final Settlement`
   - Why: legally sensitive separation payouts.
5. `SCR-PT-012 Air Ticket Entitlement and Encashment`
   - Why: contractual expat benefit with payroll impact.

## API contracts

- `POST /api/v1/salary-components`
- `POST /api/v1/payroll-runs/{runId}/wps-files`
- `POST /api/v1/pasi/runs`
- `POST /api/v1/eosb/estimates`
- `POST /api/v1/air-ticket/encashments`

## Rules and validations

- WPS generation blocked if employee bank data is invalid.
- PASI rates must resolve by effective date and employee category.
- EOSB calculation must persist formula version used.

## Data objects touched

- `SalaryComponent`
- `WpsFile`
- `PasiRun`
- `EosbSettlement`
- `AirTicketEntitlement`

## Acceptance criteria samples

- Given one payroll run with valid bank data, when WPS generation is executed, then a signed/traceable SIF artifact is created and attached to run.
- Given PASI policy effective date update, when next run is processed, then new rate applies only from effective period onward.

## SaaS forward notes

- Keep WPS/PASI/EOSB in country policy pack interfaces for future country expansion.
