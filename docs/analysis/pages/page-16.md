# Page 16 - Licensing Details and Employee Limits

## Extracted intent

- Single entity license, employee count cap, unlimited users.

## Requirements

- Tenant subscription metadata.
- Employee utilization meter against licensed cap.

## Screen requirements

1. Subscription and Usage screen
   - Why: avoid operational surprises when employee count exceeds licensed limits.
2. Tenant profile screen extension
   - Why: tie legal entity and license profile together.

## API contracts

- `GET /api/v1/billing/subscription`
- `GET /api/v1/limits/usage`
- `GET /api/v1/tenants/{tenantId}`

## Rules and validations

- Employee activation should warn/block based on policy when cap is exceeded.

## SaaS forward notes

- Keep plan logic generic for future subscription tiers and countries.
