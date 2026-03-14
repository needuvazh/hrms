# HRMS Reporting Module

`hrms-reporting` is a read-oriented module for tenant-scoped reporting queries.

## Endpoints

- `GET /api/v1/reports/employees/summary`
- `GET /api/v1/reports/attendance/summary?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD`
- `GET /api/v1/reports/leave/summary?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD`
- `GET /api/v1/reports/payroll-runs/summary?fromDate=YYYY-MM-DD&toDate=YYYY-MM-DD`

## Design notes

- Read-only reporting queries; no transactional writes.
- Tenant ID is resolved from Reactor Context via tenancy starter abstractions.
- Query logic remains isolated in the reporting module to avoid embedding report concerns in transactional modules.
