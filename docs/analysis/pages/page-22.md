# Page 22 - Terms, Conditions, Responsibilities, Liabilities

## Extracted intent

- Legal ownership, implementation assumptions, change request boundaries, liability limitations.

## Requirements

- Record legal acceptance with actor/date/version.
- Record client responsibility acknowledgments where applicable.
- Track change requests as separate objects from core scope.

## Screen requirements

1. `SCR-S-003 Legal Acceptance History`
   - Why: contractual defensibility and audit evidence.
2. Change Request Register (admin/PM workspace)
   - Why: keep out-of-scope changes controlled and billable.

## API contracts

- `POST /api/v1/legal/acceptance`
- `GET /api/v1/legal/acceptance/history`
- `POST /api/v1/change-requests`
- `GET /api/v1/change-requests`

## Rules and validations

- Acceptance records must be immutable and version-linked.
- Change request status flow: draft -> submitted -> approved/rejected -> delivered.

## SaaS forward notes

- Keep legal terms versioned by tenant, country, and effective date.
