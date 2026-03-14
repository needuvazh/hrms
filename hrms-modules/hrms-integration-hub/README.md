# HRMS Integration Hub

`hrms-integration-hub` provides reusable, adapter-based integration foundations for:

- biometric device ingestion
- bank/WPS transport
- email/SMS/WhatsApp providers
- LDAP/SSO connectors
- ERP/accounting connectors

## Core model

- `IntegrationDefinition`
- `IntegrationEndpoint`
- `IntegrationExecution`
- `IntegrationStatus`

## Design

- Tenant-aware execution and persistence.
- Adapter registry resolves provider-specific integration adapters.
- Sample local stub adapter is included for biometric provider type.
- Integration execution attempts and outcomes publish audit events.
