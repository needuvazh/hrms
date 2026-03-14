# Local Development Startup

## Prerequisites
- Java 21
- Docker Desktop (or Docker Engine)
- `curl` and `jq`

## 1. Start infrastructure
```bash
docker compose up -d
```

## 2. Run monolith app
```bash
./gradlew :hrms-apps:hrms-app-monolith:bootRun
```

Default local DB connection values:
- JDBC: `jdbc:postgresql://localhost:5432/hrms`
- R2DBC: `r2dbc:postgresql://localhost:5432/hrms`
- Username/Password: `hrms` / `hrms`

You can override values with env vars from `.env.example`.

## 3. Quick health checks
```bash
curl -fsS http://localhost:8080/api/v1/ping
curl -fsS http://localhost:8080/actuator/health
```

## 4. Seeded auth and sample API flow
Default seed credentials:
- Tenant: `default`
- Username: `admin`
- Password: `admin123`

Run smoke flow:
```bash
./scripts/mvp-smoke.sh
```

This validates:
- app availability
- tenant-aware token issuance
- authenticated employee listing
- module capability check

## 5. Stop infrastructure
```bash
docker compose down
```
