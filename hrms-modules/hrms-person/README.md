# HRMS Person Module

`hrms-person` owns canonical person identity/profile data used across candidate and employee lifecycles.

## Endpoints

- `POST /api/v1/persons`
- `GET /api/v1/persons/{id}`
- `GET /api/v1/persons?q=&limit=&offset=`

## Design notes

- Person identity is tenant-scoped and independent from employment state.
- Candidate and employee modules link to `person_id` to avoid profile duplication.
- Sensitive attributes are designed for masked/tokenized extension via `person_identifiers`.
