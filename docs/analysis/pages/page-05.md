# Page 05 - In-Scope Integrations, Services, and Responsibilities

## Extracted intent

- Required integrations: WPS, PASI, email, optional GL.
- Required services: migration, setup, UAT support.

## Requirements

- Data import pipelines for employee and payroll opening data.
- Integration setup/test screens.
- Migration validation reports.

## Screen requirements

1. `SCR-A-011 Integration Connector Setup`
   - Connectors: SMTP, biometric, bank file profiles, GL export.
   - Why: go-live depends on integration readiness.
2. `SCR-H-001 Employee Master` (bulk import tab)
   - Why: proposal expects Excel-based migration.
3. `SCR-PT-002 Variable Inputs Upload`
   - Why: monthly variable data ingestion from files.

## API contracts

- `POST /api/v1/imports/employees`
- `POST /api/v1/imports/payroll-opening-balances`
- `POST /api/v1/integrations/email/test`
- `POST /api/v1/gl-exports`

## Rules and validations

- Import must support dry-run mode with row-level errors.
- Opening balance import allowed only before first production payroll lock.

## SaaS forward notes

- Maintain connector configs per tenant and per legal entity.
