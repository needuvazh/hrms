# Employee Information API Flow (UI Integration Order)

Base URL: `/api/v1/employee-information`  
Required headers: `X-Tenant-Id: default` (and `Authorization` if security is enabled)

## Phase 1: Core Employee (Use This First)
1. `POST /employees`  
Purpose: Create core employee record.
2. `GET /employees/{employeeId}`  
Purpose: Load profile after create.
3. `PATCH /employees/{employeeId}/status`  
Purpose: Activate/inactivate/suspend employee.
4. `GET /employees`  
Purpose: Employee list/search page.
5. `GET /employees/code/{employeeCode}`  
Purpose: Fast lookup from employee code.

## Phase 2: Child Sections (After Employee Created)
Use `employeeId` from Phase 1 response.
1. Address:
`POST /employees/{employeeId}/addresses`
`GET /employees/{employeeId}/addresses`
2. Emergency contact:
`POST /employees/{employeeId}/emergency-contacts`
`GET /employees/{employeeId}/emergency-contacts`
3. Dependants:
`POST /employees/{employeeId}/dependants`
`GET /employees/{employeeId}/dependants`
4. Beneficiaries:
`POST /employees/{employeeId}/beneficiaries`
`GET /employees/{employeeId}/beneficiaries`

### Recommended Child API UI Sequence
1. Open employee profile using `GET /employees/{employeeId}`.
2. Save current/permanent address first.
3. Save emergency contact next (primary contact).
4. Save dependant entries.
5. Save beneficiary entries and show allocation summary in UI.

### Child API Request Rules (UI)
1. Always send `employeeId` from created profile as path variable.
2. Use same tenant header used during employee create.
3. Keep a separate save action per section:
Address / Emergency / Dependants / Beneficiaries.
4. For delete action use corresponding `DELETE` endpoint with optional `actor`.

## Phase 3: Workforce + Documents
1. Workforce:
`PUT /employees/{employeeId}/workforce-detail`
`GET /employees/{employeeId}/workforce-detail`
2. Document metadata:
`POST /employees/{employeeId}/documents`
3. Document upload:
`POST /employees/{employeeId}/documents/upload` (multipart)
4. Document list:
`GET /employees/{employeeId}/documents`

## Phase 4: Employment History + Onboarding
1. Employment history:
`POST /employees/{employeeId}/employment-history`
`GET /employees/{employeeId}/employment-history`
2. Digital onboarding:
`POST /employees/{employeeId}/onboarding`
`PUT /employees/{employeeId}/onboarding/progress`
`POST /employees/{employeeId}/onboarding/submit`
`POST /employees/{employeeId}/onboarding/review`
`POST /employees/{employeeId}/onboarding/approve`
`POST /employees/{employeeId}/onboarding/reject`
`POST /employees/{employeeId}/onboarding/complete`
`GET /employees/{employeeId}/onboarding`

## Current Implementation Note
Working now:
1. Core employee create/get/getByCode/list/status
2. Upload route and controller wiring for document

Scaffolded (API visible, service still pending for full behavior):
1. Most Phase 2/3/4 child APIs

### UI Team Practical Note
1. You can integrate child screens now using these APIs/contracts.
2. Until service implementation is completed, handle `PENDING_IMPLEMENTATION` gracefully in UI.

UI recommendation now:
1. Integrate Phase 1 fully.
2. Keep Phase 2/3/4 screens ready with feature flag / fallback message until service completion.
