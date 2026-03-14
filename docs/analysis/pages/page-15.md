# Page 15 - Training Plan

## Extracted intent

- Role-based training expectations and delivery format.

## Requirements

- In-app help references and role-aware onboarding guides.
- Basic knowledge base for ESS/MSS and payroll admin tasks.

## Screen requirements

1. Knowledge Center screen (optional but recommended)
   - Why: reduces repetitive support tickets.
2. Contextual help panels on high-risk screens (payroll run, WPS generation)
   - Why: reduces operator errors.

## API contracts

- `GET /api/v1/knowledge-base/articles`
- `GET /api/v1/training/modules`

## Rules and validations

- Help content should be versioned and role-filtered.

## SaaS forward notes

- Training content should be country-pack aware (law and compliance differences).
