# AGENTS.md
## Project Purpose
This repository contains a multi-tenant HRMS platform built with Spring Boot and WebFlux.

The system must support three deployment modes from a single shared codebase:

1. **Monolith deployment** — all modules deployed in one application
2. **Component-based deployment** — selected module groups deployed together
3. **Microservice deployment** — selected modules deployed independently

The architecture must be designed so that deployment topology can change without major business-code rewrites.

---

## Core Architectural Principles

### 1. Modular monolith first
The default implementation approach is a **modular monolith** with strict module boundaries.

Do not begin with distributed microservices unless explicitly requested for a module.

### 2. One shared codebase, multiple boot apps
The repository must support:
- a single monolith boot application
- multiple component boot applications
- future service-specific boot applications

Business modules must remain reusable across these runtime shapes.

### 3. WebFlux-first
All application-facing APIs and internal module contracts must be reactive:
- use `Mono` and `Flux`
- use Spring WebFlux
- use `WebClient` for remote calls
- avoid blocking code on reactive threads

### 4. Explicit module contracts
Each business module must expose a clear API interface for internal use.

Example:
- `EmployeeModuleApi`
- `AttendanceModuleApi`
- `PayrollModuleApi`

Other modules must depend on these interfaces, not on implementation details.

### 5. Strong consistency inside a module, eventual consistency across modules
Within a module:
- normal transactional behavior is allowed

Across modules:
- prefer orchestration and events
- avoid large cross-module ACID transactions

### 6. Schema-per-module PostgreSQL design
Use one PostgreSQL database initially, with separate schemas per module.

Examples:
- `tenant`
- `auth`
- `employee`
- `attendance`
- `leave`
- `payroll`

Each module owns:
- its schema objects
- its Flyway migrations
- its seed data

### 7. Multi-tenancy is first-class
Every business operation must be tenant-aware.

Use:
- tenant resolution from request/auth context
- Reactor Context for tenant propagation
- tenant-aware persistence
- tenant-level module enablement

Do **not** use `ThreadLocal` for tenant context.

### 8. Feature/module enablement
Different customers may enable different modules.

The system must support:
- tenant-level module subscriptions
- tenant-level feature flags
- conditional activation of module capabilities

### 9. Clean dependency direction
Follow this direction wherever possible:

- app modules depend on business modules
- business modules depend on platform/shared abstractions
- business modules do not depend on boot apps
- platform modules do not depend on business modules

### 10. Extraction-ready design
Modules must be designed so that:
- they can run in-process today
- they can later be accessed remotely through adapters
- business logic stays unchanged during extraction

---

## Repository Structure

Expected high-level structure:

```text
hrms-root
  build-logic
  hrms-platform
  hrms-modules
  hrms-apps
````

### build-logic

Contains Gradle convention plugins.

Examples:

* `hrms.java`
* `hrms.module`
* `hrms.webflux-app`

### hrms-platform

Contains shared technical foundations only.

Examples:

* security starter
* tenancy starter
* observability starter
* error handling starter
* outbox support
* feature toggle support
* shared kernel

### hrms-modules

Contains business/domain modules.

Examples:

* tenant
* auth
* master-data
* employee
* attendance
* leave
* payroll
* workflow
* notification
* reporting
* integration-hub

### hrms-apps

Contains bootstrapping applications.

Examples:

* `hrms-app-monolith`
* `hrms-app-core-hr`
* `hrms-app-payroll`
* `hrms-app-integration`

---

## Module Internal Structure

Each business module should follow this package layout:

```text
com.company.hrms.<module>
  api
  application
  domain
  infrastructure
  config
  events
```

### Package responsibilities

#### api

Public contract exposed to other modules.
Contains:

* module API interfaces
* DTOs meant for internal module interaction
* command/query request models where appropriate

#### application

Use-case orchestration.
Contains:

* application services
* command handlers
* query handlers
* workflow coordination inside the module

#### domain

Core business model and rules.
Contains:

* aggregates/entities
* value objects
* domain services
* business policies
* pure business validation

#### infrastructure

Technical implementation details.
Contains:

* repository implementations
* R2DBC adapters
* Redis adapters
* event publishers
* external integration adapters

#### config

Module-specific Spring configuration.

#### events

Domain events and integration event mappings.

---

## Coding Rules

### General

* Use Java 21
* Prefer constructor injection
* Prefer immutable request/response models where practical
* Keep methods small and intention-revealing
* Avoid god classes

### Reactive rules

* Never call blocking I/O on event-loop threads
* Avoid `.block()`, `.subscribe()`, `.toFuture().get()`, or similar inside application code
* Use reactive repositories/adapters where possible
* If a blocking library is unavoidable, isolate it behind an adapter and schedule appropriately

### API rules

* External APIs should be REST-first
* Internal module communication should use Java interfaces first
* Remote calls should be adapter-based, not embedded into business logic

### Error handling

* Use centralized error handling
* Use structured error responses
* Keep business exceptions explicit and meaningful

### Validation

* Validate at API boundaries
* Keep domain invariants inside the domain layer

### Logging and observability

* Structured logging only
* Include correlation ID and tenant ID where appropriate
* Instrument critical flows
* Expose Actuator endpoints safely

### Security

* All business endpoints must be authorization-aware
* Payroll and sensitive employee data require stricter access handling
* Never leak tenant data across requests

---

## Data and Persistence Rules

### PostgreSQL

* One schema per module
* Migrations per module
* Seed data per module

### Multi-tenancy

* Tenant ownership must be explicit in data design
* Repositories must not return cross-tenant data
* Tenant filtering must not be optional

### Audit

Capture audit events for:

* authentication
* sensitive reads
* approvals
* payroll actions
* configuration changes
* tenant/module enablement changes

---

## Integration Rules

External integrations must be isolated in dedicated adapters or modules.

Examples:

* biometric devices
* bank/WPS
* PASI/statutory services
* email/SMS/WhatsApp
* LDAP/SSO
* ERP/accounting

Do not spread external client code across business modules.

---

## Testing Rules

Minimum required test coverage approach:

### Unit tests

* business rules
* calculators
* validators
* policy logic

### Integration tests

* repositories
* migrations
* module APIs
* tenant isolation
* security boundaries

### Contract tests

* external integration adapters
* generated files such as payroll/WPS outputs

Use Testcontainers for:

* PostgreSQL
* Redis

---

## Implementation Priorities

Build in this order unless explicitly told otherwise:

1. repository structure and build logic
2. monolith boot app
3. platform starters
4. tenant + auth + master-data
5. employee + org foundation
6. workflow + notification + document
7. attendance + leave
8. payroll + compliance modules
9. integration adapters
10. component boot apps
11. extraction support for selected modules

---

## What Not To Do

* Do not start with dozens of microservices
* Do not create shared “utils” modules for business logic
* Do not couple modules directly to each other’s repositories
* Do not use ThreadLocal for tenant context
* Do not mix deployment concerns into domain logic
* Do not expose database entities directly as external API models
* Do not introduce GraphQL unless explicitly requested later
* Do not add framework complexity without clear need

---

## Decision Standard

When making design decisions, prefer:

1. clean module boundaries
2. tenant safety
3. deployment flexibility
4. operational simplicity
5. future extractability
6. consistency with reactive design

When uncertain, choose the option that preserves modularity and avoids premature distribution.