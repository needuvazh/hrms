# ADR 0002: Tenant Propagation Through Reactor Context

## Status
Accepted

## Context
The platform is multi-tenant and WebFlux-first. Request and async execution may move across threads.

## Decision
Tenant identity is resolved at the WebFlux edge and propagated in Reactor `Context`.
`ThreadLocal` is not used for tenant context.

## Consequences
- Tenant resolution is safe with reactive execution.
- Services and repositories read tenant context through `TenantContextAccessor`.
- Tenant isolation tests validate no cross-tenant reads.
