# Module Extraction Strategy (Local to Remote)

This codebase keeps business logic independent from transport by introducing client seams around module APIs.

## What changed

- `employee`, `attendance`, and `payroll` now expose client interfaces in addition to module APIs:
  - `EmployeeModuleClient`
  - `AttendanceModuleClient`
  - `PayrollModuleClient`
- Default local adapters delegate to in-process module API implementations.
- `pasi` now depends on `EmployeeModuleClient` and `PayrollModuleClient` rather than concrete in-process services.

## Local default behavior

- Monolith mode remains local by default.
- Local adapters are created with `@ConditionalOnMissingBean`, so existing in-process behavior is unchanged.

## Demonstrative remote adapter

- `employee` includes `WebClientEmployeeModuleClient` as a remote-capable adapter.
- Remote mode is enabled only when:
  - `hrms.module.employee.client.mode=remote`
  - `hrms.module.employee.client.base-url=<remote-employee-service-url>`
- Without these properties, local delegation is used.

## Shared remote abstractions

- Shared kernel remote primitives:
  - `ModuleClientMode`
  - `ModuleClientDescriptor`

These are intentionally lightweight so modules can adopt remote client wiring incrementally.

## Extraction path

1. Keep existing business module logic unchanged.
2. Move one module runtime behind HTTP in a separate boot app/service.
3. Switch dependent modules from local adapter to remote adapter via configuration.
4. Preserve internal API contracts and tests while changing only adapter wiring.
