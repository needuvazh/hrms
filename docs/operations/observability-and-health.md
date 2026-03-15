# Observability and Operational Hardening

This project now provides safer and more consistent operational defaults across monolith and component apps.

## Structured request observability

- Correlation ID is resolved from `X-Correlation-Id` or generated when absent.
- Correlation ID is returned in response header and propagated through Reactor Context.
- Trace and span identifiers are returned in response headers as `X-Trace-Id` and `X-Span-Id`.
- Tenant ID is propagated through Reactor Context via tenancy filter.
- Request completion logs include:
  - method
  - route
  - status
  - duration
  - correlation ID
  - tenant ID (when present)

No request/response payloads are logged by default.

## Metrics hooks

### HTTP requests

- `hrms.http.server.requests` (timer)
- Tags: `method`, `route`, `status`, `outcome`, `tenant_present`

### Workflow transitions

- `hrms.workflow.transitions` (counter)
- Tags: `workflow_key`, `transition`, `result`

### Payroll lifecycle

- `hrms.payroll.run.lifecycle` (counter)
- Tags: `transition`, `status`

### Notification dispatch

- `hrms.notification.created` (counter)
- `hrms.notification.dispatch` (counter)
- `hrms.notification.dispatch.duration` (timer)
- Tags include `channel` and `status`

## Actuator hardening

All app configurations now use:

- `management.endpoints.web.exposure.include=health,info,metrics`
- `management.endpoint.health.show-details=when_authorized`
- `management.endpoint.health.probes.enabled=true`
- `management.endpoint.env.enabled=false`
- `management.endpoint.configprops.enabled=false`

By default, only health/info are publicly permitted; other endpoints remain authenticated by security policy.

## OpenTelemetry-friendly preparation

- Tracing properties are added in app configs under `management.tracing.*`.
- Outbound `WebClient` calls propagate correlation and tenant headers from Reactor Context using a starter customizer.

## Startup and health checks

For each app:

1. Start app (example):
   - `./gradlew :hrms-apps:hrms-app-monolith:bootRun`
2. Verify readiness:
   - `GET /actuator/health`
3. Verify base API path:
   - `GET /api/v1/ping`

Component app startup commands:

- `./gradlew :hrms-apps:hrms-app-core-hr:bootRun`
- `./gradlew :hrms-apps:hrms-app-workforce:bootRun`
- `./gradlew :hrms-apps:hrms-app-payroll:bootRun`
- `./gradlew :hrms-apps:hrms-app-integration:bootRun`
