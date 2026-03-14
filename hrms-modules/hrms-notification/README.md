# notification module

## Purpose
Provides notification capabilities for the HRMS modular monolith.

## Package layout
- api: internal contracts for other modules
- application: use-case orchestration
- domain: core business model
- infrastructure: adapters (R2DBC/Web)
- config: Spring wiring
- events: module event models

## Notes
- Tenant context is required for all business operations.
- Cross-module usage must go through this module's api package.
