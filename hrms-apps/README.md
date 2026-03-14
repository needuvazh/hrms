# HRMS Boot Applications

This repository keeps one shared codebase and multiple boot applications for deployment-shape flexibility.

## Applications and module composition

- `hrms-app-monolith`
  - all current platform and business modules

- `hrms-app-core-hr`
  - modules: `tenant`, `auth`, `master-data`, `employee`, `document`

- `hrms-app-workforce`
  - modules: `tenant`, `attendance`, `leave`, `workflow`, `notification`

- `hrms-app-payroll`
  - modules: `tenant`, `payroll`, `wps`, `pasi`, `notification`

- `hrms-app-integration`
  - modules: `tenant`, `integration-hub`, `notification`, `outbox` support

## Notes

- Boot apps contain only packaging/bootstrap concerns.
- Business logic remains in `hrms-modules` for extractability.
- Each app has its own `application.yml` with explicit Flyway locations.
