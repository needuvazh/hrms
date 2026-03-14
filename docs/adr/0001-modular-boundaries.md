# ADR 0001: Cross-Module Dependencies Must Use API Packages

## Status
Accepted

## Context
The HRMS codebase runs as a modular monolith today, with planned extraction to component apps and selected remote services later.

## Decision
Business modules may depend on other modules only through `com.company.hrms.<module>.api` contracts.
Dependencies on `domain`, `application`, `infrastructure`, `config`, or `events` packages across module boundaries are not allowed.

## Consequences
- Internal refactoring inside a module does not break other modules.
- Local in-process calls can later be replaced by remote adapters without business logic rewrites.
- Architecture tests guard this rule in CI.
