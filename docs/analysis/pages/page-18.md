# Page 18 - Optional Add-on Modules

## Extracted intent

- Optional modules and custom development rates are explicitly listed.

## Requirements

- Add-on catalog and activation flow per tenant.
- Feature activation without deployment fork.

## Screen requirements

1. Add-on Catalog screen
   - Why: productized upsell model and controlled module rollout.
2. Module activation history under `SCR-A-005`
   - Why: track who enabled what and when.

## API contracts

- `GET /api/v1/addons`
- `POST /api/v1/tenants/{tenantId}/addons/{addonKey}/activate`
- `POST /api/v1/tenants/{tenantId}/addons/{addonKey}/deactivate`

## Rules and validations

- Deactivation blocked if dependent active transactions exist.

## SaaS forward notes

- Add-ons should be policy/feature driven, not hard-coded in UI navigation.
