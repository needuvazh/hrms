# Payroll System Build Guide (Dynamic, Auditable, Multi-Country SaaS)

## 1) Goal of this document

Use this as the implementation source of truth for building payroll once and scaling it to:

- one client now (Al Sawari)
- many tenants later (SaaS)
- many countries later (country policy packs)

This guide is intentionally master-first. Payroll transactions are not reliable without complete setup masters.

---

## 2) Product principles (must not be violated)

1. Tenant-first: every record and query is tenant-scoped.
2. Configuration-driven payroll: formulas and policies in masters, not hardcoded logic.
3. Effective-dated rules: legal and business changes should not require code edits.
4. Immutable payroll evidence: locked runs, file artifacts, and audit logs are tamper-resistant.
5. Country-pack architecture: core payroll is generic; statutory logic lives in country modules.
6. Modular monolith first: clean boundaries now, extraction-ready later.

---

## 3) Target repository shape

```text
hrms-root
  build-logic
  hrms-platform
    hrms-platform-tenant
    hrms-platform-auth
    hrms-platform-workflow
    hrms-platform-audit
    hrms-platform-feature-toggle
    hrms-platform-notification
  hrms-modules
    hrms-module-employee
    hrms-module-attendance
    hrms-module-leave
    hrms-module-payroll
    hrms-module-compliance-oman
    hrms-module-reporting
    hrms-module-support
  hrms-apps
    hrms-app-monolith
```

---

## 4) Module contracts you should define first

Define API interfaces (internal contracts) before implementations.

- `EmployeeModuleApi`
- `AttendanceModuleApi`
- `LeaveModuleApi`
- `PayrollModuleApi`
- `WorkflowModuleApi`
- `ComplianceModuleApi`
- `AuditModuleApi`

Each module should expose `api`, `application`, `domain`, `infrastructure`, `config`, `events` packages.

---

## 5) Core data model (high-level)

## Platform entities
- `tenant`
- `tenant_module_subscription`
- `feature_flag`
- `role`, `permission`, `role_permission`
- `workflow_definition`, `workflow_step`, `approval_action`
- `audit_event`

## HR entities
- `employee`
- `employee_employment`
- `employee_bank_account`
- `employee_document`
- `department`, `cost_center`, `branch`, `grade`, `employment_type`

## Payroll master entities
- `payroll_calendar`
- `payroll_group`
- `pay_component`
- `pay_component_formula`
- `salary_structure`
- `salary_structure_component`
- `employee_payroll_profile`
- `rounding_rule`
- `proration_rule`
- `loan_type`
- `air_ticket_rule`
- `gl_mapping`

## Payroll transaction entities
- `payroll_run`
- `payroll_run_employee`
- `payroll_line_item`
- `payroll_variable_input`
- `payslip`
- `loan_request`
- `loan_schedule`
- `payroll_adjustment`
- `gl_export`

## Oman compliance entities
- `oman_pasi_policy`
- `oman_wps_profile`
- `oman_wps_file`
- `oman_eosb_rule`
- `oman_pasi_run`

All policy and formula entities must include:

- `effective_from`
- `effective_to` (nullable)
- `version`
- `status` (`draft`, `active`, `retired`)

---

## 6) Master setup catalog (complete)

This is the minimum complete setup set before production payroll processing.

### 6.1 Organization and workforce masters

1. Tenant master
2. Legal entity master
3. Branch/location master
4. Department master
5. Cost center master
6. Grade/band master
7. Employment type master
8. Employee category master (local/expat/etc)
9. Holiday calendar master
10. Shift/roster master

### 6.2 Payroll definition masters

11. Payroll frequency master
12. Payroll calendar master
13. Payroll cutoff rule master
14. Payroll group master
15. Pay component master
16. Pay component formula master
17. Salary structure master
18. Salary structure assignment rule master
19. Proration rule master
20. Rounding rule master

### 6.3 Input and benefit masters

21. Attendance mapping rule master
22. Leave impact rule master
23. Loan type and recovery rule master
24. Advance type rule master
25. Adjustment reason master
26. Arrear rule master
27. Bonus/incentive rule master
28. Air ticket entitlement rule master

### 6.4 Statutory and integration masters

29. Country policy pack binding (tenant -> country pack)
30. PASI policy master (Oman)
31. WPS file profile master (Oman)
32. Bank/bank branch master
33. EOSB rule master (Oman)
34. GL mapping master
35. Document template master (payslip/certificates)
36. Notification template master

### 6.5 Governance masters

37. Approval workflow master
38. Delegation rule master
39. Payroll lock/unlock policy master
40. Security policy master (MFA/session/IP)

---

## 7) Master screen specifications (detailed format)

Use this format for every screen: `Purpose`, `Fields`, `Validations`, `Role access`, `APIs`, `Audit events`.

## 7.1 Screen: Pay Component Master

Purpose:
- Define reusable earnings/deductions/contributions/provisions dynamically.

Fields:
- `componentCode` (unique)
- `componentName`
- `componentType` (`EARNING`, `DEDUCTION`, `CONTRIBUTION`, `PROVISION`)
- `taxable` (bool)
- `affectsGross` (bool)
- `affectsNet` (bool)
- `calculationMode` (`FIXED`, `FORMULA`, `INPUT`, `REFERENCE`)
- `defaultValue`
- `currency`
- `displayOrder`
- `effectiveFrom`, `effectiveTo`
- `status`

Validations:
- `componentCode` unique per tenant.
- `effectiveFrom <= effectiveTo` when `effectiveTo` present.
- `FIXED` mode requires `defaultValue`.
- cannot retire component if active salary structures still reference it.

Role access:
- HR Admin: create/edit draft
- Payroll Admin: activate/retire
- Auditor: read-only

APIs:
- `POST /api/v1/pay-components`
- `GET /api/v1/pay-components`
- `GET /api/v1/pay-components/{id}`
- `PUT /api/v1/pay-components/{id}`
- `POST /api/v1/pay-components/{id}/activate`
- `POST /api/v1/pay-components/{id}/retire`
- `GET /api/v1/pay-components/{id}/audit`

Audit events:
- `PAY_COMPONENT_CREATED`
- `PAY_COMPONENT_UPDATED`
- `PAY_COMPONENT_ACTIVATED`
- `PAY_COMPONENT_RETIRED`

## 7.2 Screen: Pay Component Formula Master

Purpose:
- Define calculation expression for formula-based components.

Fields:
- `formulaCode`
- `componentId`
- `expression` (DSL)
- `dependsOnComponents[]`
- `roundingRuleId`
- `priorityOrder`
- `effectiveFrom`, `effectiveTo`, `status`

Validations:
- cycle detection in dependency graph.
- expression parser validation before save.
- only active components can be referenced.

APIs:
- `POST /api/v1/pay-component-formulas`
- `PUT /api/v1/pay-component-formulas/{id}`
- `POST /api/v1/pay-component-formulas/{id}/validate`
- `POST /api/v1/pay-component-formulas/{id}/activate`

## 7.3 Screen: Salary Structure Master

Purpose:
- Build reusable salary templates by grade/entity/location.

Fields:
- `structureCode`, `structureName`
- `legalEntityId`
- `gradeId` (nullable)
- `currency`
- `componentLines[]` (component + amount/rule + sequence)
- `effectiveFrom`, `effectiveTo`, `status`

Validations:
- at least one earning component required.
- total deduction cannot exceed configurable threshold at template stage.

APIs:
- `POST /api/v1/salary-structures`
- `PUT /api/v1/salary-structures/{id}`
- `GET /api/v1/salary-structures/{id}`
- `POST /api/v1/salary-structures/{id}/activate`

## 7.4 Screen: Employee Payroll Profile

Purpose:
- Bind employee to payroll structure and statutory profile.

Fields:
- `employeeId`
- `payrollGroupId`
- `salaryStructureId`
- `paymentMode`
- `bankAccountId`
- `pasiEligible`
- `eosbEligible`
- `airTicketEligible`
- `effectiveFrom`, `status`

Validations:
- employee must be active employment.
- bank account mandatory for bank transfer mode.

APIs:
- `PUT /api/v1/employees/{employeeId}/payroll-profile`
- `GET /api/v1/employees/{employeeId}/payroll-profile`

## 7.5 Screen: Payroll Calendar + Cutoff Master

Purpose:
- Define processing periods and input lock deadlines.

Fields:
- `calendarName`
- `frequency`
- `periodStartRule`, `periodEndRule`
- `attendanceCutoffDay`
- `leaveCutoffDay`
- `variableInputCutoffDay`
- `payDateRule`

APIs:
- `POST /api/v1/payroll-calendars`
- `PUT /api/v1/payroll-calendars/{id}`
- `GET /api/v1/payroll-calendars`

## 7.6 Screen: Payroll Group Master

Purpose:
- Segment employees into independent payroll run populations.

Fields:
- `groupCode`, `groupName`
- `filters` (legal entity/branch/department/grade/custom)
- `calendarId`
- `approvalWorkflowId`

APIs:
- `POST /api/v1/payroll-groups`
- `PUT /api/v1/payroll-groups/{id}`
- `GET /api/v1/payroll-groups`

## 7.7 Screen: PASI Policy Master (Oman)

Purpose:
- Configure statutory rates and caps with effective dates.

Fields:
- `employeeRate`
- `employerRate`
- `wageCeiling`
- `eligibleCategories[]`
- `effectiveFrom`, `effectiveTo`

APIs:
- `POST /api/v1/compliance/oman/pasi-policies`
- `PUT /api/v1/compliance/oman/pasi-policies/{id}`
- `GET /api/v1/compliance/oman/pasi-policies`

## 7.8 Screen: WPS Profile Master (Oman)

Purpose:
- Define SIF output profile and bank mapping constraints.

Fields:
- `profileCode`
- `bankCodeMapping[]`
- `fileFormatVersion`
- `defaultPaymentNarration`
- `effectiveFrom`, `status`

APIs:
- `POST /api/v1/compliance/oman/wps-profiles`
- `PUT /api/v1/compliance/oman/wps-profiles/{id}`
- `GET /api/v1/compliance/oman/wps-profiles`

## 7.9 Screen: EOSB Rule Master (Oman)

Purpose:
- Configure end-of-service formula matrix by service duration and exit type.

Fields:
- `ruleCode`
- `serviceYearBandFrom`, `serviceYearBandTo`
- `exitType`
- `benefitMultiplier`
- `reductionFactor`
- `effectiveFrom`, `effectiveTo`

APIs:
- `POST /api/v1/compliance/oman/eosb-rules`
- `PUT /api/v1/compliance/oman/eosb-rules/{id}`
- `GET /api/v1/compliance/oman/eosb-rules`

## 7.10 Screen: GL Mapping Master

Purpose:
- Map payroll components to accounting ledgers and dimensions.

Fields:
- `componentId`
- `drAccount`, `crAccount`
- `costCenterMode` (`EMPLOYEE`, `FIXED`, `DERIVED`)
- `roundingPolicy`
- `effectiveFrom`, `status`

APIs:
- `POST /api/v1/gl-mappings`
- `PUT /api/v1/gl-mappings/{id}`
- `GET /api/v1/gl-mappings`

---

## 8) Transaction screens required after masters

1. Attendance import and reconciliation
2. Variable input upload
3. Pre-payroll checklist
4. Payroll simulation
5. Payroll run workbench
6. Exception resolution
7. Approval and lock
8. Payslip generation/publication
9. WPS generation/submission tracking
10. PASI run/reporting
11. EOSB settlement
12. Loan/advance processing
13. GL export

Do not release transaction screens without readiness checks for dependencies.

---

## 9) Payroll run state machine

```text
DRAFT -> INPUT_READY -> CALCULATED -> VALIDATED -> APPROVED -> LOCKED -> PUBLISHED
             |             |             |
             +-> FAILED ---+             +-> REJECTED (to DRAFT)
```

Rules:
- only `APPROVED` can move to `LOCKED`
- `LOCKED` is immutable for payroll lines
- any unlock requires privileged role + reason + audit event

---

## 10) Formula engine design (dynamic processing)

Use expression DSL with safe functions:

- arithmetic: `+`, `-`, `*`, `/`
- comparisons: `>`, `<`, `>=`, `<=`, `==`
- functions: `min`, `max`, `round`, `if`, `sum`, `coalesce`
- references: `COMP.BASIC`, `VAR.OT_HOURS`, `ATT.WORK_DAYS`

Example formulas:

```text
COMP.HRA = round(COMP.BASIC * 0.40, 3)
COMP.OT_PAY = round(VAR.OT_HOURS * (COMP.BASIC / 30 / 8) * 1.25, 3)
COMP.GROSS = sum(COMP.BASIC, COMP.HRA, COMP.TRANSPORT, COMP.OT_PAY)
COMP.PASI_EMP = if(PROFILE.PASI_ELIGIBLE, min(COMP.BASIC, POLICY.PASI_WAGE_CEILING) * POLICY.PASI_EMP_RATE, 0)
COMP.NET = COMP.GROSS - sum(COMP.PASI_EMP, COMP.LOAN_EMI, COMP.OTHER_DED)
```

Engine safeguards:
- parser validation
- dependency cycle check
- deterministic execution order
- decimal precision controls
- formula snapshot per payroll run

---

## 11) Country pack pattern (multi-country support)

Define interfaces in payroll core:

- `StatutoryContributionPolicy`
- `BankTransferFilePolicy`
- `EndOfServicePolicy`
- `LeaveEncashmentPolicy`

Oman pack implements:
- PASI calculations and reporting
- WPS SIF file generation and validation
- EOSB rules

Future country support = add new pack module; no change to run orchestration.

---

## 12) API standards

All APIs must follow:

- versioned path: `/api/v1/...`
- tenant derived from auth + reactor context
- request id and correlation id propagation
- standardized error envelope:

```json
{
  "code": "PAYROLL_VALIDATION_FAILED",
  "message": "Payroll checklist has blocking issues",
  "details": [{"field": "attendance", "issue": "pending_reconciliation"}],
  "correlationId": "9d9f4d3c-..."
}
```

- pagination: `page`, `size`, `sort`
- filtering: explicit query params
- idempotency header for create/import APIs

---

## 13) Audit model (must-have events)

At minimum, publish events for:

- all master create/update/activate/retire actions
- payroll simulation started/completed
- payroll run validated/approved/locked/unlocked
- payslip generated/published
- WPS file generated/submitted
- PASI run generated
- EOSB settlement approved
- role permission changes

Audit payload minimum:
- `tenantId`, `actorId`, `action`, `resourceType`, `resourceId`, `timestamp`, `before`, `after`, `correlationId`

---

## 14) Testing strategy

## Unit tests
- formula parser/evaluator
- PASI/EOSB calculators
- proration and rounding policies

## Integration tests
- repository isolation by tenant
- API validations
- payroll state transitions

## Contract tests
- WPS file format
- PASI report format
- GL export format

## E2E tests
- master setup -> payroll run -> lock -> payslip -> WPS/PASI outputs
- approval reject path
- unlock with reason path

Use Testcontainers for PostgreSQL and Redis.

---

## 15) UI implementation standards

For every master screen:

- list + filter + export
- create/edit form with server-side validation mapping
- effective-dated version view
- activate/retire lifecycle actions
- audit tab

For every transaction screen:

- checklist banner
- blocker vs warning distinction
- timeline of state transitions
- action confirmation for irreversible steps

---

## 16) Build sequence you should follow (single flow)

1. Platform foundation (tenant/auth/workflow/audit/feature flags)
2. Organization + HR masters
3. Payroll masters (components, formulas, structures, calendar, groups)
4. Statutory masters (PASI/WPS/EOSB Oman)
5. Employee payroll profile and dependencies
6. Attendance and variable input pipelines
7. Payroll engine + simulation + run lifecycle
8. Payslip/document generation
9. WPS/PASI/EOSB outputs
10. GL export and reports
11. ESS/MSS approval flows
12. Support/SLA and regulatory update center

This is one continuous path with dependency order, not a disconnected phase plan.

---

## 17) Definition of done for each screen/API

A screen or API is done only when all are true:

1. functional behavior complete
2. role and permission checks complete
3. tenant isolation enforced
4. effective-date behavior verified
5. audit events emitted and visible
6. error responses standardized
7. integration tests added
8. documentation updated

---

## 18) Immediate next implementation set

Implement first in this exact order:

1. Pay Component Master
2. Pay Component Formula Master
3. Salary Structure Master
4. Payroll Calendar Master
5. Payroll Group Master
6. Employee Payroll Profile
7. Pre-payroll checklist + simulation

Once these are stable, payroll is truly dynamic and ready for the remaining transaction set.

Detailed per-screen specs for this set are available in:

- `docs/analysis/screen-specs/README.md`

---

## 19) Industry patterns to follow (inspired by leading HRMS products)

Use these proven patterns seen across enterprise HRMS ecosystems:

1. Effective dating everywhere for payroll policies and statutory rules.
2. Payroll elements/components abstraction instead of fixed columns.
3. Formula or rule engine for dynamic calculations.
4. Payroll process list with strict state controls and maker-checker.
5. Retro and arrears handling as first-class transactions.
6. Separation of core engine from country statutory adapters.
7. Immutable payroll result snapshots for legal and audit support.

Apply these patterns through the master setup and engine design in this guide.
