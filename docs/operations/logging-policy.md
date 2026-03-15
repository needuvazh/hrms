# Logging Policy

## Objectives
- Keep logs actionable for production incidents and local debugging.
- Keep logs safe for multi-tenant operations by avoiding sensitive payload leakage.
- Keep logging behavior consistent across monolith and component apps.

## Logging stack
- Runtime logging backend: Log4j2 (`log4j2-spring.xml`) in each boot app.
- API usage in code: SLF4J (`org.slf4j.Logger` / `LoggerFactory`).
- Output target in this phase: console only.

## Level policy
- `ERROR`: failed operations that require intervention or indicate broken behavior.
- `WARN`: recoverable abnormal conditions and policy violations.
- `INFO`: key business and platform milestones (request completion, state transitions, dispatch results).
- `DEBUG`: local troubleshooting details; keep disabled by default in shared environments.

## Security and data policy
- Never log passwords, secrets, tokens, signed headers, OTPs, or raw credentials.
- Never log full request/response bodies by default.
- Mask or omit sensitive employee/payroll personal data.
- Keep tenant isolation visible in logs using tenant identifiers where possible.

## Context requirements
- Include correlation identifier and tenant identifier when available.
- Include application name, logger name, and thread for traceability.
- API responses should return `X-Correlation-Id`, `X-Trace-Id`, and `X-Span-Id` for troubleshooting.

## Local debugging
- Use JVM debug mode when needed:
  - `./gradlew :hrms-apps:hrms-app-monolith:bootRun --debug-jvm`
- Raise log levels temporarily with environment variables:
  - `HRMS_LOG_LEVEL_ROOT=DEBUG`
  - `HRMS_LOG_LEVEL_HRMS=DEBUG`
  - `HRMS_LOG_LEVEL_SPRING=INFO`
- Do not keep `DEBUG` enabled in persistent shared environments.
