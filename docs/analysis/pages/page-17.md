# Page 17 - Implementation Services, Cloud Option, AMC

## Extracted intent

- Captures service scope and post-go-live support model.

## Requirements

- Track support plan, warranty period, and AMC start.
- Optional cloud subscription metadata.

## Screen requirements

1. Contract and Support Plan screen
   - Why: operational teams need clarity on entitlement windows.
2. Support dashboard linkage (`SCR-S-001`)
   - Why: SLA handling differs between warranty and AMC.

## API contracts

- `GET /api/v1/contracts`
- `GET /api/v1/support/plans`
- `GET /api/v1/support/entitlements`

## Rules and validations

- Ticket severity routing can vary by support plan.

## SaaS forward notes

- Model contracts as tenant-level objects, separate from core HR/payroll data.
