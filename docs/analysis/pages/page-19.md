# Page 19 - Payment Terms and Milestones

## Extracted intent

- Project milestone-based payment model and delay charges.

## Requirements

- Optional implementation-project billing tracker.
- Capture milestone sign-offs and invoice readiness status.

## Screen requirements

1. Project Billing Milestones screen (implementation layer)
   - Why: delivery governance for SI-style rollouts.

## API contracts

- `GET /api/v1/project-billing/milestones`
- `POST /api/v1/project-billing/milestones/{milestoneId}/ack`

## Rules and validations

- Final milestone should require UAT completion evidence.

## SaaS forward notes

- Keep project billing separate from recurring SaaS billing engine.
