# HRMS Platform

Multi-tenant HRMS backend built with Java 21, Spring Boot, and WebFlux.

## Architecture Summary
- Modular monolith first, with explicit internal module APIs (`api` packages).
- Reactive-first design (`Mono`/`Flux`) and Reactor Context tenant propagation.
- Strong consistency inside a module, eventual consistency across modules (outbox/events).
- PostgreSQL schema-per-module migrations via Flyway.
- Shared platform foundations: tenancy, security, observability, outbox, feature-toggle, audit.

## Module List (MVP)
- `tenant`
- `auth`
- `master-data`
- `person`
- `employee`
- `recruitment`
- `attendance`
- `leave`
- `workflow`
- `notification`
- `document`
- `payroll`
- `wps`
- `pasi`
- `reporting`
- `integration-hub`

## Boot Apps
- `hrms-app-monolith`
  - Full MVP in one runtime.
- `hrms-app-core-hr`
  - tenant, auth, master-data, person, employee, recruitment, document.
- `hrms-app-workforce`
  - tenant, attendance, leave, workflow, notification.
- `hrms-app-payroll`
  - tenant, payroll, wps, pasi, notification.
- `hrms-app-integration`
  - tenant, integration-hub, notification, outbox support.

## Deployment Model Explanation
- Monolith deployment:
  - Run `hrms-app-monolith` to host all modules together.
- Component deployment:
  - Run selected component apps (`core-hr`, `workforce`, `payroll`, `integration`) from the same codebase.
- Future selective microservice extraction:
  - Modules keep interface-driven APIs and adapter seams so local module calls can later be replaced by remote clients with minimal business-logic changes.

## Local Startup Steps
1. Prerequisites:
   - Java 21
   - Docker
   - `curl`, `jq`
2. Start local infrastructure:
   - `docker compose up -d`
3. Run monolith:
   - `./gradlew :hrms-apps:hrms-app-monolith:bootRun`
4. Validate health:
   - `curl -fsS http://localhost:8080/api/v1/ping`
   - `curl -fsS http://localhost:8080/actuator/health`
5. Run seeded smoke flow:
   - `./scripts/mvp-smoke.sh`
6. Stop infrastructure:
   - `docker compose down`

Detailed startup guide: [docs/startup/local-development.md](/Users/praveenkumar/Documents/Project/Freelance/hrms/docs/startup/local-development.md)

## Docs for Reviewers
- MVP completion review: [docs/reviews/mvp-rc1-review.md](/Users/praveenkumar/Documents/Project/Freelance/hrms/docs/reviews/mvp-rc1-review.md)
- ADRs: `/docs/adr`
- Observability/health: [docs/operations/observability-and-health.md](/Users/praveenkumar/Documents/Project/Freelance/hrms/docs/operations/observability-and-health.md)
- Logging policy: [docs/operations/logging-policy.md](/Users/praveenkumar/Documents/Project/Freelance/hrms/docs/operations/logging-policy.md)
- Module extraction strategy: [docs/architecture/module-extraction-strategy.md](/Users/praveenkumar/Documents/Project/Freelance/hrms/docs/architecture/module-extraction-strategy.md)

## Next-Step Backlog Notes
1. Expand authorization coverage and tests for attendance/leave/payroll/reporting endpoints.
2. Add deeper end-to-end Testcontainers flows across modules.
3. Add stricter production profiles (secrets, actuator exposure, policy defaults).
4. Add formal API reference for reviewer and QA use.
