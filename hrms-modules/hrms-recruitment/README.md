# HRMS Recruitment Module

`hrms-recruitment` manages candidate lifecycle before employment and supports candidate-to-employee conversion.

## Endpoints

- `POST /api/v1/recruitment/candidates`
- `GET /api/v1/recruitment/candidates/{id}`
- `GET /api/v1/recruitment/candidates?q=&status=&limit=&offset=`
- `PATCH /api/v1/recruitment/candidates/{id}/status`
- `POST /api/v1/recruitment/candidates/{id}/hire`

## Design notes

- Candidate records reference canonical `person_id`.
- `hire` flow creates employee through `EmployeeModuleApi` and marks candidate `HIRED`.
- Lifecycle status transitions are captured in `candidate_status_history`.
