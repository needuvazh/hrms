# XVP Gap Checklist

## High Priority
- [ ] Gap item: App-level Spring/Flyway/security config duplication across boot apps.
- [ ] Fix recommendation: Introduce shared app defaults and keep only app-specific overrides in each app config.
- [ ] Owner suggestion: Platform/Architecture team.

- [ ] Gap item: Security policy and authorization tests are uneven outside employee endpoints.
- [ ] Fix recommendation: Add explicit authorization rules and dedicated tests for attendance, leave, payroll, reporting endpoints.
- [ ] Owner suggestion: Security + Module owners.

- [ ] Gap item: Cross-module end-to-end test coverage is limited.
- [ ] Fix recommendation: Add Testcontainers-backed integration suites for onboarding and payroll chains.
- [ ] Owner suggestion: QA automation + Module owners.

## Medium Priority
- [ ] Gap item: Domain-level coupling to foreign module API DTOs in selected modules (e.g., WPS/PASI with payroll views).
- [ ] Fix recommendation: Introduce local mapping boundaries and module-owned internal models where appropriate.
- [ ] Owner suggestion: Module owners (Payroll/WPS/PASI).

- [ ] Gap item: Observability structure is present but structured logging policy is not fully standardized.
- [ ] Fix recommendation: Define and enforce common log event schema and redaction policy across apps.
- [ ] Owner suggestion: Platform observability owner.

- [ ] Gap item: Redis integration testing baseline is limited.
- [ ] Fix recommendation: Add Redis Testcontainers tests for reactive cache/context/integration scenarios.
- [ ] Owner suggestion: Platform + module owners using Redis.

## Low Priority
- [ ] Gap item: Profile strategy is functional but basic for production hardening.
- [ ] Fix recommendation: Add explicit prod profile templates for secret sourcing and actuator exposure policy.
- [ ] Owner suggestion: DevOps/Platform team.

- [ ] Gap item: Reviewer-facing API reference is still minimal.
- [ ] Fix recommendation: Publish endpoint catalog (or OpenAPI equivalent) for MVP modules.
- [ ] Owner suggestion: API/Platform documentation owner.
