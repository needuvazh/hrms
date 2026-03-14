# ADR 0003: Outbox for Cross-Module/Event Publishing Reliability

## Status
Accepted

## Context
Business operations require strong consistency inside a module and eventual consistency across module boundaries.

## Decision
Domain operations persist local state and write integration events to the outbox.
A background dispatcher publishes pending outbox events.

## Consequences
- Local transaction integrity is preserved.
- Event delivery retries and failures are explicit and observable.
- Remote broker adoption later does not change module business logic.
