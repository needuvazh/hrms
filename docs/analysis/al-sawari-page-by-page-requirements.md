# Al Sawari Payroll Program - Page-by-Page Requirements (Screen + API + Why)

## 1. Purpose

This document converts the proposal `docs/requirements/HRMS Payroll ESS Proposal for Al Sawari Intl.Invt.pdf` into implementation-ready requirements.

It is designed for:

- immediate single-client rollout (Al Sawari)
- future multi-tenant SaaS rollout
- future multi-country expansion

The focus is explicit on:

- individual screens (including all masters)
- individual APIs
- why each screen is necessary
- end-to-end UI and API development flow

---

## 2. Product Direction (Non-Negotiable)

- Build as a modular monolith first, with extraction-ready module boundaries.
- Keep tenant context mandatory in every request and persistence query.
- Keep country compliance logic in pluggable policy packs (start with Oman).
- Treat masters as first-class features, not secondary setup pages.
- Use workflow + approval + audit on all critical financial actions.

---

## 3. Screen Taxonomy Used Here

- `M` Master (reference/config)
- `T` Transaction (day-to-day operations)
- `R` Report/Register
- `A` Admin/System

Each listed screen includes:

- why it is required
- core UI actions
- API contract set

---

## 4. End-to-End Screen Universe (Master-Inclusive)

## 4.1 Global Platform and Tenant Admin

1. `SCR-A-001` Tenant Master (`M`)
   - Why: future SaaS requires tenant isolation from day 1.
   - APIs: `POST /api/v1/tenants`, `GET /api/v1/tenants/{tenantId}`, `PUT /api/v1/tenants/{tenantId}`

2. `SCR-A-002` Legal Entity Master (`M`)
   - Why: payroll/legal reporting is entity-specific.
   - APIs: `POST /api/v1/legal-entities`, `GET /api/v1/legal-entities`, `PUT /api/v1/legal-entities/{id}`

3. `SCR-A-003` Branch/Location Master (`M`)
   - Why: attendance, payroll groups, and compliance often vary by branch.
   - APIs: `POST /api/v1/branches`, `GET /api/v1/branches`, `PUT /api/v1/branches/{id}`

4. `SCR-A-004` Department/Cost Center Master (`M`)
   - Why: payroll posting, budgets, and reports require cost-center attribution.
   - APIs: `POST /api/v1/departments`, `POST /api/v1/cost-centers`, `GET /api/v1/cost-centers`

5. `SCR-A-005` Module and Feature Enablement (`A`)
   - Why: control paid/optional capabilities per tenant as SaaS scales.
   - APIs: `GET /api/v1/tenants/{tenantId}/modules`, `PUT /api/v1/tenants/{tenantId}/modules/{moduleKey}`

6. `SCR-A-006` Role and Permission Matrix (`A`)
   - Why: payroll is sensitive; field-level access is needed for finance/HR segregation.
   - APIs: `POST /api/v1/roles`, `PUT /api/v1/roles/{roleId}/permissions`, `GET /api/v1/roles/{roleId}/permissions`

7. `SCR-A-007` Workflow Definition Builder (`A`)
   - Why: approvals differ by client, country, and process type.
   - APIs: `POST /api/v1/workflows`, `PUT /api/v1/workflows/{workflowId}`, `GET /api/v1/workflows`

8. `SCR-A-008` Notification Template Master (`M`)
   - Why: ESS/MSS relies on alerts for approvals and payroll events.
   - APIs: `POST /api/v1/notification-templates`, `PUT /api/v1/notification-templates/{id}`, `GET /api/v1/notification-templates`

9. `SCR-A-009` Document Template Master (`M`)
   - Why: payslip/certificate formats differ per tenant and country.
   - APIs: `POST /api/v1/document-templates`, `GET /api/v1/document-templates`, `PUT /api/v1/document-templates/{id}`

10. `SCR-A-010` Security Policy Screen (`A`)
    - Why: enforce MFA, session timeout, and IP controls for payroll security.
    - APIs: `GET /api/v1/security/policies`, `PUT /api/v1/security/policies`, `POST /api/v1/auth/mfa/enroll`

11. `SCR-A-011` Integration Connector Setup (`A`)
    - Why: WPS, SMTP, biometric, and GL require managed credentials/config.
    - APIs: `PUT /api/v1/integrations/{integrationKey}/credentials`, `POST /api/v1/integrations/{integrationKey}/test`, `GET /api/v1/integrations/status`

12. `SCR-A-012` Audit Explorer (`R`)
    - Why: payroll decisions must be traceable for legal and financial audit.
    - APIs: `GET /api/v1/audit-events`, `GET /api/v1/audit-events/{eventId}`

## 4.2 Employee and HR Masters (Payroll Dependencies)

13. `SCR-H-001` Employee Master (`M`)
    - Why: all payroll calculations and compliance are employee-centric.
    - APIs: `POST /api/v1/employees`, `GET /api/v1/employees/{employeeId}`, `PUT /api/v1/employees/{employeeId}`

14. `SCR-H-002` Dependent/Beneficiary Master (`M`)
    - Why: insurance/EOSB and statutory records need dependent linkage.
    - APIs: `POST /api/v1/employees/{employeeId}/dependents`, `GET /api/v1/employees/{employeeId}/dependents`

15. `SCR-H-003` Contract/Employment Type Master (`M`)
    - Why: rules vary by contract class (expat/local/full-time/other).
    - APIs: `POST /api/v1/employment-types`, `GET /api/v1/employment-types`

16. `SCR-H-004` Shift Master (`M`)
    - Why: attendance and overtime derive from shifts.
    - APIs: `POST /api/v1/shifts`, `GET /api/v1/shifts`, `PUT /api/v1/shifts/{shiftId}`

17. `SCR-H-005` Holiday Calendar Master (`M`)
    - Why: paid/unpaid day handling and OT rates depend on holiday flags.
    - APIs: `POST /api/v1/holiday-calendars`, `GET /api/v1/holiday-calendars/{id}`

18. `SCR-H-006` Leave Type and Leave Policy Master (`M`)
    - Why: leave balance affects payroll and encashment.
    - APIs: `POST /api/v1/leave-types`, `POST /api/v1/leave-policies`, `GET /api/v1/leave-policies`

19. `SCR-H-007` Employee Document Vault (`T`)
    - Why: visa/labor/passport expiry impacts employability and payroll continuity.
    - APIs: `POST /api/v1/employees/{employeeId}/documents`, `GET /api/v1/documents/expiring`

20. `SCR-H-008` Bank and Bank Branch Master (`M`)
    - Why: WPS file generation and salary transfers need accurate bank references.
    - APIs: `POST /api/v1/banks`, `POST /api/v1/banks/{bankId}/branches`, `GET /api/v1/banks`

21. `SCR-H-009` Employee Bank Account Master (`M`)
    - Why: incorrect IBAN/account data causes WPS failures.
    - APIs: `POST /api/v1/employees/{employeeId}/bank-accounts`, `PUT /api/v1/employees/{employeeId}/bank-accounts/{id}`

## 4.3 Payroll Masters

22. `SCR-PM-001` Payroll Calendar Master (`M`)
    - Why: processing windows and cutoff logic must be controlled.
    - APIs: `POST /api/v1/payroll-calendars`, `GET /api/v1/payroll-calendars`

23. `SCR-PM-002` Payroll Group Master (`M`)
    - Why: separate runs by grade/department/site reduce operational risk.
    - APIs: `POST /api/v1/payroll-groups`, `GET /api/v1/payroll-groups`

24. `SCR-PM-003` Salary Component Master (`M`)
    - Why: earnings/deductions must be configurable and reusable.
    - APIs: `POST /api/v1/salary-components`, `GET /api/v1/salary-components`, `PUT /api/v1/salary-components/{id}`

25. `SCR-PM-004` Salary Structure Master (`M`)
    - Why: standardized pay architecture ensures consistent payroll.
    - APIs: `POST /api/v1/salary-structures`, `GET /api/v1/salary-structures/{id}`

26. `SCR-PM-005` Employee Payroll Profile (`M`)
    - Why: links employee to structure, statutory flags, and recurring values.
    - APIs: `PUT /api/v1/employees/{employeeId}/payroll-profile`, `GET /api/v1/employees/{employeeId}/payroll-profile`

27. `SCR-PM-006` Statutory Rules Master (PASI/Oman pack) (`M`)
    - Why: effective-dated compliance rates avoid code changes on legal updates.
    - APIs: `POST /api/v1/statutory/policies/pasi`, `GET /api/v1/statutory/policies/pasi`

28. `SCR-PM-007` WPS File Profile Master (`M`)
    - Why: bank/file profile controls output format and validation rules.
    - APIs: `POST /api/v1/wps/profiles`, `GET /api/v1/wps/profiles`

29. `SCR-PM-008` EOSB Rule Master (`M`)
    - Why: end-of-service formula must be versioned and legal-grade.
    - APIs: `POST /api/v1/eosb/rules`, `GET /api/v1/eosb/rules`

30. `SCR-PM-009` Loan Type Master (`M`)
    - Why: configurable tenure/eligibility/interest for policy-controlled lending.
    - APIs: `POST /api/v1/loan-types`, `GET /api/v1/loan-types`

31. `SCR-PM-010` Air Ticket Entitlement Rule Master (`M`)
    - Why: expat benefits require precise entitlement and encashment logic.
    - APIs: `POST /api/v1/air-ticket/rules`, `GET /api/v1/air-ticket/rules`

32. `SCR-PM-011` GL Mapping Master (`M`)
    - Why: accounting integration depends on deterministic mapping.
    - APIs: `POST /api/v1/gl-mappings`, `GET /api/v1/gl-mappings`

33. `SCR-PM-012` Payslip Profile Master (`M`)
    - Why: branding/password/watermark/bilingual layout are tenant-specific.
    - APIs: `POST /api/v1/payslip/profiles`, `GET /api/v1/payslip/profiles`

## 4.4 Payroll Transactions

34. `SCR-PT-001` Attendance Import + Reconciliation (`T`)
    - Why: payroll accuracy starts with attendance correctness.
    - APIs: `POST /api/v1/attendance/imports`, `GET /api/v1/attendance/imports/{importId}`, `POST /api/v1/attendance/reconciliation/actions`

35. `SCR-PT-002` Variable Inputs Upload (OT/bonus/adjustments) (`T`)
    - Why: monthly variable pay cannot be hardcoded.
    - APIs: `POST /api/v1/payroll-variable-inputs/imports`, `GET /api/v1/payroll-variable-inputs`

36. `SCR-PT-003` Pre-Payroll Checklist (`T`)
    - Why: prevents invalid payroll runs and compliance errors.
    - APIs: `GET /api/v1/payroll-runs/{runId}/checklist`, `POST /api/v1/payroll-runs/{runId}/checklist/recheck`

37. `SCR-PT-004` Payroll Simulation (`T`)
    - Why: dry-run catches errors before irreversible payout processing.
    - APIs: `POST /api/v1/payroll-runs/simulate`, `GET /api/v1/payroll-runs/simulations/{simulationId}`

38. `SCR-PT-005` Payroll Run Workbench (`T`)
    - Why: central orchestrator for calculate/validate/approve/lock.
    - APIs: `POST /api/v1/payroll-runs`, `GET /api/v1/payroll-runs/{runId}`, `POST /api/v1/payroll-runs/{runId}/validate`, `POST /api/v1/payroll-runs/{runId}/approve`, `POST /api/v1/payroll-runs/{runId}/lock`

39. `SCR-PT-006` Payroll Exceptions and Overrides (`T`)
    - Why: controlled corrections are needed without full rerun.
    - APIs: `GET /api/v1/payroll-runs/{runId}/exceptions`, `POST /api/v1/payroll-runs/{runId}/exceptions/{exceptionId}/resolve`

40. `SCR-PT-007` Payslip Generation and Delivery (`T`)
    - Why: legal proof of salary + employee transparency.
    - APIs: `POST /api/v1/payroll-runs/{runId}/payslips/generate`, `POST /api/v1/payroll-runs/{runId}/payslips/publish`, `GET /api/v1/employees/{employeeId}/payslips`

41. `SCR-PT-008` WPS File Generation and Tracking (`T`)
    - Why: core Oman compliance artifact for salary disbursement.
    - APIs: `POST /api/v1/payroll-runs/{runId}/wps-files`, `GET /api/v1/wps-files/{wpsFileId}`, `POST /api/v1/wps-submissions/{submissionId}/status`

42. `SCR-PT-009` PASI Run and Reporting (`T`)
    - Why: statutory contributions must be calculated and reportable.
    - APIs: `POST /api/v1/pasi/runs`, `GET /api/v1/pasi/runs/{runId}`, `GET /api/v1/pasi/runs/{runId}/report`

43. `SCR-PT-010` EOSB Estimate and Final Settlement (`T`)
    - Why: legal exits require transparent and reproducible calculations.
    - APIs: `POST /api/v1/eosb/estimates`, `POST /api/v1/eosb/settlements`, `GET /api/v1/eosb/settlements/{settlementId}`

44. `SCR-PT-011` Loan and Advance Lifecycle (`T`)
    - Why: controlled employee financing with payroll recovery.
    - APIs: `POST /api/v1/loans/requests`, `POST /api/v1/loans/{loanId}/approve`, `GET /api/v1/loans/{loanId}/schedule`, `POST /api/v1/loans/{loanId}/disburse`

45. `SCR-PT-012` Air Ticket Entitlement and Encashment (`T`)
    - Why: expatriate benefits affect payroll and final settlement.
    - APIs: `GET /api/v1/air-ticket/entitlements`, `POST /api/v1/air-ticket/encashments`

46. `SCR-PT-013` Payroll Adjustments and Arrears (`T`)
    - Why: retrospective corrections are unavoidable in real payroll.
    - APIs: `POST /api/v1/payroll-adjustments`, `GET /api/v1/payroll-adjustments`

47. `SCR-PT-014` GL Export (`T`)
    - Why: payroll finance posting must reconcile with accounting systems.
    - APIs: `POST /api/v1/gl-exports`, `GET /api/v1/gl-exports/{exportId}`

## 4.5 ESS and MSS

48. `SCR-ESS-001` ESS Dashboard (`T`)
    - Why: self-service reduces HR workload and request cycle times.
    - APIs: `GET /api/v1/ess/dashboard`

49. `SCR-ESS-002` My Profile Change Request (`T`)
    - Why: employee-owned data updates with approval control.
    - APIs: `POST /api/v1/ess/profile-change-requests`, `GET /api/v1/ess/profile-change-requests`

50. `SCR-ESS-003` Leave Request and Balance (`T`)
    - Why: leave directly impacts payroll and team planning.
    - APIs: `POST /api/v1/leave-requests`, `GET /api/v1/leave-balances/me`

51. `SCR-ESS-004` Payslip and Salary Letters (`T`)
    - Why: self-access to salary documents cuts manual HR overhead.
    - APIs: `GET /api/v1/employees/me/payslips`, `POST /api/v1/employees/me/certificates`

52. `SCR-ESS-005` Loan/Advance Requests (`T`)
    - Why: transparent request flow with eligibility awareness.
    - APIs: `POST /api/v1/loans/requests`, `GET /api/v1/loans/requests/me`

53. `SCR-ESS-006` Exit Request and Settlement Tracking (`T`)
    - Why: employee visibility reduces disputes during separation.
    - APIs: `POST /api/v1/exit-requests`, `GET /api/v1/exit-requests/me`

54. `SCR-MSS-001` Manager Approval Inbox (`T`)
    - Why: all requests require timely approvals to avoid payroll delays.
    - APIs: `GET /api/v1/approvals/inbox`, `POST /api/v1/approvals/{approvalId}/actions`

55. `SCR-MSS-002` Team Calendar and Team Attendance (`R`)
    - Why: managers need workforce visibility for approval decisions.
    - APIs: `GET /api/v1/teams/{teamId}/calendar`, `GET /api/v1/teams/{teamId}/attendance`

56. `SCR-MSS-003` Delegation Setup (`T`)
    - Why: prevents approval bottlenecks during manager absence.
    - APIs: `POST /api/v1/delegations`, `GET /api/v1/delegations/me`

## 4.6 Reports, Compliance, Support

57. `SCR-R-001` Payroll Register (`R`)
    - Why: official monthly payroll statement for HR/finance.
    - APIs: `GET /api/v1/reports/payroll-register`

58. `SCR-R-002` Payroll Variance Report (`R`)
    - Why: detect outliers and unintended changes month over month.
    - APIs: `GET /api/v1/reports/payroll-variance`

59. `SCR-R-003` WPS Compliance Dashboard (`R`)
    - Why: track submission completeness and rejection risk.
    - APIs: `GET /api/v1/compliance/wps/summary`

60. `SCR-R-004` PASI Compliance Dashboard (`R`)
    - Why: statutory traceability and filing confidence.
    - APIs: `GET /api/v1/compliance/pasi/summary`

61. `SCR-R-005` EOSB Liability and Provision Report (`R`)
    - Why: finance needs liabilities for planning and accrual.
    - APIs: `GET /api/v1/reports/eosb-liability`

62. `SCR-R-006` Loan Outstanding Register (`R`)
    - Why: risk tracking and payroll recovery planning.
    - APIs: `GET /api/v1/reports/loan-outstanding`

63. `SCR-R-007` Air Ticket Due Report (`R`)
    - Why: proactive entitlement management avoids surprise costs.
    - APIs: `GET /api/v1/reports/air-ticket-due`

64. `SCR-S-001` Support Ticket + SLA Tracker (`A`)
    - Why: production operations need measurable service delivery.
    - APIs: `POST /api/v1/support/tickets`, `GET /api/v1/support/tickets/{ticketId}`

65. `SCR-S-002` Regulatory Update Center (`R`)
    - Why: country compliance updates should be visible to admins.
    - APIs: `GET /api/v1/compliance/updates`

66. `SCR-S-003` Legal Acceptance History (`R`)
    - Why: legal and terms acceptance must be auditable.
    - APIs: `POST /api/v1/legal/acceptance`, `GET /api/v1/legal/acceptance/history`

---

## 5. Page-by-Page Requirement Mapping (Proposal -> Build)

## Page 1
- Functional requirements: none.
- Build impact: none.

## Page 2
- Functional requirements: none.
- Build impact: none.

## Page 3
- Requirements: Oman compliance, ESS, attendance integration, dashboards.
- Screens: `SCR-R-003`, `SCR-R-004`, `SCR-PT-001`, `SCR-ESS-001`, `SCR-A-007`.
- Why: defines core value proposition and compliance obligations.

## Page 4
- Requirements: module scope and optional add-ons.
- Screens: `SCR-A-005`.
- Why: tenant-level package control is needed for SaaS evolution.

## Page 5
- Requirements: WPS/PASI integration, SMTP, GL export, migration.
- Screens: `SCR-A-011`, data import view under `SCR-H-001` and `SCR-PT-002`.
- Why: integration readiness and clean migration drive go-live success.

## Page 6
- Requirements: employee information, org structure, leave policies.
- Screens: `SCR-H-001`, `SCR-H-002`, `SCR-H-006`, `SCR-H-007`, `SCR-A-003`, `SCR-A-004`.
- Why: payroll correctness depends on complete HR master quality.

## Page 7
- Requirements: EOSB and payroll lifecycle (simulate, lock, checklist).
- Screens: `SCR-PT-003`, `SCR-PT-004`, `SCR-PT-005`, `SCR-PT-010`.
- Why: control and auditability before salary disbursement.

## Page 8
- Requirements: earnings/deductions, WPS, PASI, EOSB, air ticket.
- Screens: `SCR-PM-003`, `SCR-PT-008`, `SCR-PT-009`, `SCR-PT-010`, `SCR-PT-012`.
- Why: statutory and compensation core execution scope.

## Page 9
- Requirements: payslips, salary administration, loans/advances.
- Screens: `SCR-PT-007`, `SCR-PT-011`, `SCR-ESS-004`, `SCR-ESS-005`.
- Why: employee transparency and recoverable financing.

## Page 10
- Requirements: ESS/MSS detailed capabilities.
- Screens: `SCR-ESS-001..006`, `SCR-MSS-001..003`.
- Why: user adoption and operational throughput rely on self-service.

## Page 11
- Requirements: admin cockpit, no-code workflows, permissions, reports.
- Screens: `SCR-A-007`, `SCR-A-006`, `SCR-A-012`, report configuration under `SCR-R-*`.
- Why: configurable platform avoids repeated custom coding.

## Page 12
- Requirements: RBAC, MFA, encryption, integrations.
- Screens: `SCR-A-010`, `SCR-A-011`.
- Why: payroll requires enterprise-grade security posture.

## Page 13
- Requirements: role-based workflow and request processing.
- Screens: `SCR-MSS-001`, `SCR-A-007`, `SCR-A-006`.
- Why: approvals and role boundaries enforce governance.

## Page 14
- Requirements: UAT, parallel run, go-live stabilization.
- Screens: test and reconciliation views from `SCR-PT-004`, `SCR-PT-005`, `SCR-R-001`.
- Why: payroll transition risk is minimized with controlled parallel validation.

## Page 15
- Requirements: role-based training materials.
- Screens: optional in-app knowledge center (future enhancement).
- Why: reduces support load and improves adoption.

## Page 16
- Requirements: licensing and employee count limits.
- Screens: subscription/usage panel (can be admin extension).
- Why: SaaS commercialization and guardrails.

## Page 17
- Requirements: support period and AMC context.
- Screens: `SCR-S-001`.
- Why: predictable operational support commitments.

## Page 18
- Requirements: optional add-ons.
- Screens: `SCR-A-005` add-on controls.
- Why: monetize features without branching codebase.

## Page 19
- Requirements: payment milestone terms.
- Screens: optional project billing tracker (implementation-side).
- Why: useful for SI delivery operations, not core payroll runtime.

## Page 20
- Requirements: SLA matrix and regulatory updates.
- Screens: `SCR-S-001`, `SCR-S-002`.
- Why: production trust depends on service quality and legal updates.

## Page 21
- Functional requirements: none.
- Build impact: none.

## Page 22
- Requirements: legal ownership and acceptance conditions.
- Screens: `SCR-S-003`.
- Why: legal defensibility and audit evidence.

---

## 6. API Design Standards (Per Screen)

For every screen, define and implement:

1. `GET list` endpoint with filters, pagination, tenant scoping.
2. `GET by id` endpoint with role-aware field masking.
3. `POST create` endpoint with validation and duplicate rules.
4. `PUT update` endpoint with optimistic version check.
5. `POST state action` endpoint where lifecycle exists (submit/approve/reject/lock).
6. `GET audit` endpoint for history and actor traceability.
7. Import/export endpoints where batch operations are required.

All endpoints must include:

- tenant context resolution
- country policy resolution (Oman policy pack initially)
- structured error model (`code`, `message`, `details`, `correlationId`)
- audit publishing for create/update/approve/lock/delete-equivalent actions

---

## 7. End-to-End UI + API Development Flow (Continuous, Not Phase-only)

Use this loop for each screen bundle:

1. Define screen contract
   - fields, validations, states, permissions, workflow actions, audit events.
2. Define API contract
   - request/response schema, error schema, idempotency, versioning.
3. Define domain invariants
   - status transitions, compliance checks, effective-date logic.
4. Implement backend
   - controller -> application service -> domain policies -> repository adapters -> event/audit.
5. Implement frontend
   - route -> data query/mutation hooks -> form/grid -> validation -> action confirmations.
6. Integrate approvals and notifications
   - attach workflow to actions and trigger notifications.
7. Add observability
   - logs, metrics, traces with tenantId and correlationId.
8. Test pyramid
   - unit (calculators/rules), integration (DB/API), contract (WPS/PASI outputs), E2E.
9. Deploy behind feature flags
   - release per tenant/module without branching architecture.

This single loop is repeated until all `SCR-*` are implemented.

---

## 8. Multi-Country and SaaS Readiness Rules

- Keep a country policy interface for statutory calculations and file formats.
- Implement Oman policy pack first (`WPS`, `PASI`, `EOSB`); keep payroll engine generic.
- Never hardcode country formulas inside generic payroll services.
- Keep all legal/statutory masters effective-dated.
- Localize templates and labels (English now, Arabic-ready).
- Ensure all queries enforce tenant boundaries.

---

## 9. Done Criteria (Requirement Completeness)

A requirement item is not complete unless it has:

1. Screen ID and type (`M/T/R/A`)
2. Why statement
3. UI fields and validations
4. API contract set
5. Workflow and approval states
6. Role/permission behavior
7. Audit events
8. Tenant and country policy behavior
9. Acceptance criteria with positive and negative paths

---

## 10. Immediate Execution Priority (Recommended)

1. Payroll masters (`SCR-PM-*`) + employee/bank masters (`SCR-H-*`).
2. Payroll core transactions (`SCR-PT-001..009`).
3. Compliance reports (`SCR-R-001..004`).
4. ESS/MSS essential screens (`SCR-ESS-001..005`, `SCR-MSS-001`).
5. EOSB/Loan/Air-ticket (`SCR-PT-010..012`, `SCR-R-005..007`).
6. Admin hardening (`SCR-A-006..012`) and support/legal (`SCR-S-*`).

This order gives fastest business value while preserving SaaS and multi-country architecture.
