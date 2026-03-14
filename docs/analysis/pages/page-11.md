# Page 11 - HR Admin Portal and Technical Stack Context

## Extracted intent

- Centralized HR admin operations and configurable workflow/reporting.

## Requirements

- Bulk import/export with validation.
- No-code workflow configuration.
- Role-based access and audit logs.
- Report builder and publication controls.

## Screen requirements

1. `SCR-A-007 Workflow Definition Builder`
   - Why: avoids code-level changes for each process change.
2. `SCR-A-006 Role and Permission Matrix`
   - Why: sensitive data requires strict access control.
3. `SCR-A-012 Audit Explorer`
   - Why: compliance-grade traceability.
4. Report definition screen (under reporting module)
   - Why: tenant-custom reporting without developer dependency.

## API contracts

- `POST /api/v1/workflows`
- `PUT /api/v1/roles/{roleId}/permissions`
- `GET /api/v1/audit-events`
- `POST /api/v1/reports/definitions`

## Rules and validations

- Workflow publish blocked if there is no final approval state.
- Permission updates require dual-control role in production tenants.

## SaaS forward notes

- Keep report definitions isolated per tenant and optionally per legal entity.
