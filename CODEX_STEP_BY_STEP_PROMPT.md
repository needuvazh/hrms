# Codex Step-by-Step Implementation Prompt

Use this as your main "play prompt" in Codex.

```text
You are implementing a new Spring Boot HRMS platform in this repository.

Read and follow AGENTS.md strictly.

## Goal
Create a WebFlux-first, multi-tenant, modular-monolith Spring Boot project that can later support:
1. monolith deployment
2. component-based deployment
3. selective microservice deployment

The codebase must be organized so that deployment topology can change without rewriting business logic.

## High-level requirements
- Java 21
- Gradle multi-module build
- Spring Boot
- Spring WebFlux
- PostgreSQL
- R2DBC
- Flyway
- Reactive Redis support foundation
- Actuator
- OpenTelemetry-ready structure
- Multi-tenant SaaS support
- Schema-per-module database design
- Tenant-aware module enablement
- Explicit internal module APIs
- REST-first external APIs
- No GraphQL for now

## Architectural rules
- Modular monolith first
- Separate boot apps from business modules
- Each business module exposes a clear API interface
- Internal module APIs must be reactive (Mono/Flux)
- Strong consistency inside a module
- Eventual consistency across modules
- Use Reactor Context for tenant propagation
- No ThreadLocal-based tenant context
- Avoid blocking calls in reactive flows
- Keep business code independent from deployment shape

## Deliver the implementation in phases
Do not try to build the full HRMS in one pass.
Implement phase by phase.

---

## Phase 1 — Repository and build foundation
Create the initial multi-module Gradle structure with:
- build-logic
- hrms-platform
- hrms-modules
- hrms-apps

Create convention plugins:
- hrms.java
- hrms.module
- hrms.webflux-app

Set up:
- root settings.gradle
- root build.gradle
- gradle version catalog or centralized dependency management
- standard test configuration
- Java 21 toolchain

Expected output:
- project builds successfully
- empty module structure is valid
- monolith app can start with a basic health endpoint

---

## Phase 2 — Platform starters
Create technical foundation modules under hrms-platform:

- hrms-starter-webflux
- hrms-starter-security
- hrms-starter-tenancy
- hrms-starter-observability
- hrms-starter-error
- hrms-outbox
- hrms-feature-toggle
- hrms-shared-kernel

Implement minimal but clean versions of:
- common exception model
- API error response model
- tenant context abstraction
- Reactor Context tenant propagation support
- correlation ID filter
- basic security skeleton
- actuator configuration
- shared base utilities that are truly technical, not business-specific

Expected output:
- monolith app starts using platform starters
- request context and tenant context foundation exists
- starter modules are reusable

---

## Phase 3 — First boot app
Create hrms-app-monolith.

It should:
- include platform starters
- wire selected business modules
- expose a versioned REST API base path
- expose health/info endpoints
- support local profile
- support PostgreSQL and Flyway configuration
- be runnable locally

Add a simple ping endpoint and startup documentation.

Expected output:
- one runnable monolith boot app
- local developer startup works

---

## Phase 4 — Foundational business modules
Create first business modules under hrms-modules:

- hrms-tenant
- hrms-auth
- hrms-master-data
- hrms-employee

Each module must follow package structure:
- api
- application
- domain
- infrastructure
- config
- events

Each module must expose a clear internal API interface.
Use reactive signatures.

Expected output:
- tenant module supports tenant lookup/config baseline
- auth module supports basic user/role model skeleton
- master-data module supports lookup values skeleton
- employee module supports create/get/search employee basics

---

## Phase 5 — Database design and migrations
Set up schema-per-module Flyway migrations.

Create initial schemas:
- tenant
- auth
- master_data
- employee

Add seed data support for each module.

Ensure:
- tenant-aware table design
- audit-ready columns where needed
- naming conventions are consistent

Expected output:
- app can bootstrap schemas
- app can load minimal seed data
- repository integration tests pass

---

## Phase 6 — Tenant-aware request flow
Implement tenant resolution and propagation.

Requirements:
- resolve tenant from request header for now
- store tenant in Reactor Context
- expose helper for reading tenant context safely
- reject requests without valid tenant where required
- ensure repository/application layer can access tenant context cleanly

Expected output:
- tenant-aware request pipeline working
- tests validating tenant isolation behavior

---

## Phase 7 — Employee API baseline
Implement external REST endpoints for employee module.

Suggested endpoints:
- POST /api/v1/employees
- GET /api/v1/employees/{id}
- GET /api/v1/employees

Implement:
- DTOs
- validation
- mapping
- service orchestration
- R2DBC persistence
- basic tests

Expected output:
- employee CRUD baseline working reactively
- clean separation between API/application/domain/infrastructure

---

## Phase 8 — Architecture quality pass
After the first working modules are done, refactor for quality.

Check:
- module boundary violations
- package consistency
- dependency direction
- duplicated code
- reactive anti-patterns
- configuration clarity
- test quality
- naming consistency

Document major architecture choices in ADR files.

Expected output:
- cleaner structure
- documented architecture decisions

---

## Rules for implementation
- Prefer small, reviewable commits
- Prefer clear package names
- Prefer explicit APIs over hidden magic
- Avoid speculative complexity
- Keep code production-oriented
- Add concise README files where useful
- Add TODOs only when genuinely necessary
- Do not add GraphQL
- Do not add microservice runtime complexity yet

## Deliverables to produce
1. working Gradle multi-module setup
2. runnable monolith app
3. platform starter foundation
4. tenant/auth/master-data/employee starter modules
5. Flyway migrations and seed data
6. tenant-aware reactive request flow
7. employee API baseline
8. architecture documentation

As you work:
- explain what you are creating
- keep changes incremental
- keep the repository runnable after each phase
```
