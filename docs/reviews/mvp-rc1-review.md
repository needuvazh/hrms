# MVP RC1 Review

Date: 2026-03-10

## Scope
Repository-wide review against MVP modules:
- tenant, auth, master-data, employee
- attendance, leave, workflow
- notification, document
- payroll, wps, pasi
- reporting

## Module status

### tenant
- Status: implemented
- Notes: tenant schema and seed present; module/feature enablement tables seeded.

### auth
- Status: implemented baseline
- Notes: reactive auth flow, JWT issuance endpoint, tenant-aware user/role/permission model and seed user.

### master-data
- Status: implemented baseline
- Notes: lookup foundation and seed migration present.

### employee
- Status: implemented baseline
- Notes: create/get/search endpoints, tenant-aware persistence, onboarding orchestration baseline.

### attendance
- Status: implemented baseline
- Notes: shift, assignment, punch and query endpoints with tenant-aware service/repository.

### leave
- Status: implemented baseline
- Notes: leave type/balance/request flow and workflow integration in place.

### workflow
- Status: implemented baseline
- Notes: reusable workflow primitives and approval transitions.

### notification
- Status: implemented baseline
- Notes: notification creation, queue/dispatch flow, local logging dispatcher.

### document
- Status: implemented baseline
- Notes: metadata model and storage abstraction; local adapter and seed migration now present.

### payroll
- Status: implemented baseline
- Notes: payroll period/run lifecycle, approval, finalize, payslip metadata and notification/audit integration.

### wps
- Status: implemented baseline
- Notes: payroll-to-wps batch/export flow; seed migration now present.

### pasi
- Status: implemented baseline
- Notes: contribution rule and period calculation against payroll records.

### reporting
- Status: implemented baseline
- Notes: read-oriented report endpoints over transactional schemas.

## Key consistency fixes applied in this review
- Added missing tenant schema migration location to component apps that use feature-toggle infrastructure.
- Added payroll app migration coverage for employee schema used by PASI local module interaction.
- Added missing seed migrations for document and wps modules.
- Added local infra bootstrap (`docker-compose.yml`) and smoke script (`scripts/mvp-smoke.sh`).
- Consolidated startup documentation for developers.

## MVP readiness assessment
- Functional MVP baseline: **ready for RC1 internal validation**
- Deployment-shape flexibility: **in place** (monolith + component apps)
- Tenant safety: **foundationally in place** (tenant context, tenant-aware repositories, isolation tests)
- Observability/security foundations: **in place**, with room for stricter production policies.

## Recommended next backlog (post-RC1)
1. Add end-to-end integration tests across employee->leave->workflow->notification and payroll->wps->pasi flows using Testcontainers.
2. Expand authorization tests beyond employee endpoints (attendance, leave, payroll/reporting).
3. Add Redis Testcontainers coverage for modules that will rely on reactive redis behavior.
4. Add production profile templates for externalized secrets and stricter actuator exposure.
5. Add OpenAPI-style API reference (or equivalent endpoint catalog) for reviewers and QA.
