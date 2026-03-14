# Page 06 - Core HRMS Foundations (Employee, Org, Leave)

## Extracted intent

- Rich employee profile and document lifecycle.
- Multi-level org structure.
- Leave rules aligned with labour law.

## Requirements

- Full employee master with statutory and payroll relevant attributes.
- Document expiry alerts with alert windows.
- Org hierarchy with payroll dimensions.
- Leave policy master with encashment and approval chains.

## Screen requirements

1. `SCR-H-001 Employee Master`
   - Fields: demographic, employment, statutory category, payroll links.
   - Why: payroll accuracy starts from employee master integrity.
2. `SCR-H-007 Employee Document Vault`
   - Fields: type, expiry date, renewal status.
   - Why: expired documents can block work and salary operations.
3. `SCR-A-003 Branch/Location Master` and `SCR-A-004 Department/Cost Center Master`
   - Why: cost allocations and approvals depend on org structure.
4. `SCR-H-006 Leave Type and Leave Policy Master`
   - Why: leave affects payable days, encashment, and final settlements.

## API contracts

- `POST /api/v1/employees`
- `POST /api/v1/employees/{employeeId}/documents`
- `GET /api/v1/documents/expiring`
- `POST /api/v1/leave-types`
- `POST /api/v1/leave-policies`

## Rules and validations

- Employee bank account and payroll profile cannot activate before mandatory KYC fields.
- Leave policy versioning must be effective-dated.

## Data objects touched

- `Employee`
- `EmployeeDocument`
- `LeaveType`
- `LeavePolicy`
- `OrgUnit`

## Acceptance criteria samples

- Given an employee with missing mandatory payroll fields, when HR attempts activation, then system blocks activation with exact missing field list.
- Given a document nearing expiry, when alert scheduler runs, then configured reminder windows (90/60/30/7) are generated.

## SaaS forward notes

- Keep document type and leave types configurable by country pack.
