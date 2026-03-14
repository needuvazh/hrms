# Page 04 - Proposal Highlights and Module Scope

## Extracted intent

- Defines included vs optional modules.
- Signals configurable deployment and support details.

## Requirements

- Tenant-level module enablement and feature flags.
- Optional modules must be activatable without code forks.

## Screen requirements

1. `SCR-A-005 Module and Feature Enablement`
   - Fields: module key, status, start date, billing tag.
   - Why: supports single-client now and add-on monetization later.
2. `SCR-A-001 Tenant Master`
   - Fields: tenant code, region, default language, country pack.
   - Why: base identity for SaaS isolation.

## API contracts

- `GET /api/v1/tenants/{tenantId}/modules`
- `PUT /api/v1/tenants/{tenantId}/modules/{moduleKey}`
- `GET /api/v1/features`

## Rules and validations

- Disable action blocked if module has active transactions not archived.
- Feature toggle changes must create audit records.

## SaaS forward notes

- Keep module enablement independent from deployment topology.
