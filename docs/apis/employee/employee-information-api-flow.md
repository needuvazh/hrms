# Employee Information API Flow (Swagger Test Order)

Base URL: `/api/v1/employee-information`  
Required headers:
- `X-Tenant-Id: default`
- `X-Tenant-Key: <tenant-key-if-enabled>`
- `Authorization: Bearer <token-if-security-enabled>`

## 1. Create Core Employee
1. `POST /employees`
2. Save `employeeId` and `employeeCode` from response
3. `GET /employees/{employeeId}` to verify create
4. Optional search checks:
`GET /employees`
`GET /employees/code/{employeeCode}`

## 2. Update Status
1. `PATCH /employees/{employeeId}/status`
2. Valid values:
`DRAFT`, `ACTIVE`, `ON_PROBATION`, `CONFIRMED`, `NOTICE_PERIOD`, `RESIGNED`, `TERMINATED`, `RETIRED`, `SUSPENDED`, `INACTIVE`

## 3. Add Child Data (in this order)
1. Address:
`POST /employees/{employeeId}/addresses`
`POST /employees/{employeeId}/addresses/bulk` (optional)
2. Emergency Contact:
`POST /employees/{employeeId}/emergency-contacts`
3. Dependants:
`POST /employees/{employeeId}/dependants`
4. Beneficiaries:
`POST /employees/{employeeId}/beneficiaries`
5. Workforce:
`PUT /employees/{employeeId}/workforce-detail`

## 4. Document Vault
1. `POST /employees/{employeeId}/documents` (metadata create)
2. `PUT /employees/{employeeId}/documents/{employeeDocumentId}` (metadata update)
3. `PATCH /employees/{employeeId}/documents/{employeeDocumentId}/verification`
4. `GET /employees/{employeeId}/documents`
5. `GET /employees/{employeeId}/documents/expiry/expiring?bucket=DAYS_30&activeOnly=true`
6. `GET /employees/{employeeId}/documents/expiry/dashboard`
7. `GET /employees/{employeeId}/documents/expiry/expired?activeOnly=true`

## 5. Employment History
1. `POST /employees/{employeeId}/employment-history`
2. `GET /employees/{employeeId}/employment-history`
3. `GET /employees/{employeeId}/employment-history/{employmentHistoryId}`

## 6. Digital Onboarding
1. `POST /employees/{employeeId}/onboarding`
2. `PUT /employees/{employeeId}/onboarding/progress`
3. `POST /employees/{employeeId}/onboarding/policy-ack`
4. `POST /employees/{employeeId}/onboarding/submit`
5. `POST /employees/{employeeId}/onboarding/review`
6. `POST /employees/{employeeId}/onboarding/approve` or `.../reject`
7. `POST /employees/{employeeId}/onboarding/complete`
8. `GET /employees/{employeeId}/onboarding`

## 7. Update/Get/Delete Pattern for Child APIs
For each child entity (address, emergency contact, dependant, beneficiary, document):
1. Create
2. List
3. Get by id
4. Update by id
5. Deactivate (DELETE endpoint)

## 8. Important Validation Notes
1. `officialEmail` must be unique per tenant.
2. `dateOfBirth` cannot be future.
3. `confirmationDate` and `probationEndDate` cannot be before `dateOfJoining`.
4. Beneficiary total active allocation cannot exceed 100.
5. Workforce:
`OMANI_NATIONAL` requires `pasiNumber`.
`EXPATRIATE` requires `permitNumber`.
6. Employment history requires `effectiveDate` and at least one old/new field change.
7. Onboarding cannot move to `COMPLETED` unless e-form, documents, and policy acknowledgement are all true.
