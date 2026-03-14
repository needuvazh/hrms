# Codex Prompt - HRMS Implementation Guardrails

Use this prompt for any coding task in this repository.

## Role
You are implementing a multi-tenant HRMS backend in Java 21 using Spring Boot + WebFlux from a single shared codebase that supports:
1. Monolith deployment
2. Component-based deployment
3. Future microservice extraction

Default architecture is a modular monolith with strict boundaries.

## Non-Negotiables
- Keep business modules extraction-ready; no business rewrites required when moving in-process to remote access.
- Use reactive APIs and contracts (`Mono`/`Flux`) across app-facing and module-facing boundaries.
- Never use `ThreadLocal` for tenant context; use Reactor Context.
- Enforce tenant safety in all reads/writes.
- Keep clean dependency direction:
  - apps -> modules
  - modules -> platform abstractions
  - modules must not depend on boot apps
  - platform must not depend on business modules

## Target Repository Shape
```text
hrms-root
  build-logic
  hrms-platform
  hrms-modules
  hrms-apps
```

## Business Module Contract Rules
Every business module must expose an API interface in its `api` package, e.g.:
- `EmployeeModuleApi`
- `AttendanceModuleApi`
- `PayrollModuleApi`

Other modules depend on these APIs only, never on implementation internals or repositories.

## Required Module Package Layout
```text
com.company.hrms.<module>
  api
  application
  domain
  infrastructure
  config
  events
```

## Data and Consistency Rules
- PostgreSQL schema-per-module design (single DB initially).
- Each module owns its migrations and seed data.
- Strong consistency within a module.
- Eventual consistency across modules (events/orchestration; avoid cross-module ACID transactions).

## Multi-Tenancy and Feature Enablement
- Every business operation must be tenant-aware.
- Tenant-level module subscriptions and feature flags are first-class.
- Module capability activation must be conditional per tenant.

## Reactive and Integration Rules
- Avoid blocking calls on event-loop threads.
- Do not use `.block()`, `.subscribe()`, `.toFuture().get()` in application flow.
- Use `WebClient` for remote adapters.
- Isolate external systems in adapters or dedicated integration modules.

## Security, Observability, and Errors
- All business endpoints must be authorization-aware.
- Sensitive data (especially payroll) must have stricter access control.
- Use centralized, structured error handling.
- Use structured logging with correlation ID and tenant ID.

## Testing Baseline
- Unit tests: business rules, calculators, validators, policies.
- Integration tests: repositories, migrations, module APIs, tenant isolation, security boundaries.
- Contract tests: external adapters and generated outputs.
- Use Testcontainers for PostgreSQL and Redis.

## Implementation Priorities
1. Repository structure and build logic
2. Monolith boot app
3. Platform starters
4. Tenant + auth + master-data
5. Employee + org foundation
6. Workflow + notification + document
7. Attendance + leave
8. Payroll + compliance
9. Integration adapters
10. Component boot apps
11. Extraction support for selected modules

## Output Expectations for Every Task
When implementing a change:
1. State assumptions briefly.
2. Keep boundaries explicit (what module owns what).
3. Provide code and tests together.
4. Highlight tenant safety and reactive correctness.
5. Mention how the change remains extraction-ready.

## Anti-Patterns to Reject
- Premature decomposition into many microservices.
- Cross-module repository coupling.
- Shared business logic dumped into generic `utils`.
- Domain logic tied to deployment topology.
- Exposing persistence entities directly as API models.
