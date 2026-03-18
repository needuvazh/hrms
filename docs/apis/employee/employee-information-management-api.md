# Employee Information Management APIs (Working)

Base path: `/api/v1/employee-information`

## Core Employee
1. `POST /employees` create employee
2. `PUT /employees/{employeeId}` update employee
3. `GET /employees/{employeeId}` get by id
4. `GET /employees/code/{employeeCode}` get by code
5. `GET /employees` search/list
6. `PATCH /employees/{employeeId}/status` update status

## Address
1. `POST /employees/{employeeId}/addresses`
2. `POST /employees/{employeeId}/addresses/bulk`
3. `PUT /employees/{employeeId}/addresses/{addressId}`
4. `GET /employees/{employeeId}/addresses/{addressId}`
5. `GET /employees/{employeeId}/addresses`
6. `DELETE /employees/{employeeId}/addresses/{addressId}`

## Emergency Contact
1. `POST /employees/{employeeId}/emergency-contacts`
2. `PUT /employees/{employeeId}/emergency-contacts/{emergencyContactId}`
3. `GET /employees/{employeeId}/emergency-contacts/{emergencyContactId}`
4. `GET /employees/{employeeId}/emergency-contacts`
5. `DELETE /employees/{employeeId}/emergency-contacts/{emergencyContactId}`

## Dependants
1. `POST /employees/{employeeId}/dependants`
2. `PUT /employees/{employeeId}/dependants/{dependantId}`
3. `GET /employees/{employeeId}/dependants/{dependantId}`
4. `GET /employees/{employeeId}/dependants`
5. `DELETE /employees/{employeeId}/dependants/{dependantId}`

## Beneficiaries
1. `POST /employees/{employeeId}/beneficiaries`
2. `PUT /employees/{employeeId}/beneficiaries/{beneficiaryId}`
3. `GET /employees/{employeeId}/beneficiaries/{beneficiaryId}`
4. `GET /employees/{employeeId}/beneficiaries`
5. `DELETE /employees/{employeeId}/beneficiaries/{beneficiaryId}`

## Workforce Detail
1. `PUT /employees/{employeeId}/workforce-detail`
2. `GET /employees/{employeeId}/workforce-detail`

## Document Vault
1. `POST /employees/{employeeId}/documents`
2. `POST /employees/{employeeId}/documents/upload` (multipart)
3. `POST /employees/{employeeId}/documents/{employeeDocumentId}/replace-file` (multipart)
4. `PUT /employees/{employeeId}/documents/{employeeDocumentId}`
5. `PATCH /employees/{employeeId}/documents/{employeeDocumentId}/verification`
6. `GET /employees/{employeeId}/documents/{employeeDocumentId}`
7. `GET /employees/{employeeId}/documents`
8. `DELETE /employees/{employeeId}/documents/{employeeDocumentId}`
9. `GET /employees/{employeeId}/documents/expiry/expiring?bucket=DAYS_30&activeOnly=true`
10. `GET /employees/{employeeId}/documents/expiry/dashboard`
11. `GET /employees/{employeeId}/documents/expiry/expired?activeOnly=true`

## Employment History
1. `POST /employees/{employeeId}/employment-history`
2. `GET /employees/{employeeId}/employment-history`
3. `GET /employees/{employeeId}/employment-history/{employmentHistoryId}`

## Digital Onboarding
1. `POST /employees/{employeeId}/onboarding`
2. `PUT /employees/{employeeId}/onboarding/progress`
3. `POST /employees/{employeeId}/onboarding/policy-ack`
4. `POST /employees/{employeeId}/onboarding/submit`
5. `POST /employees/{employeeId}/onboarding/review`
6. `POST /employees/{employeeId}/onboarding/approve`
7. `POST /employees/{employeeId}/onboarding/reject`
8. `POST /employees/{employeeId}/onboarding/complete`
9. `GET /employees/{employeeId}/onboarding`

## Employee Status Values
- `DRAFT`
- `ACTIVE`
- `ON_PROBATION`
- `CONFIRMED`
- `NOTICE_PERIOD`
- `RESIGNED`
- `TERMINATED`
- `RETIRED`
- `SUSPENDED`
- `INACTIVE`

## Swagger Testing Notes
1. Pass tenant headers from `Authorize` popup before execution.
2. Create employee first, then use returned `employeeId` for all child APIs.
3. Use valid master UUIDs for relation fields like:
`countryId`, `nationalityId`, `genderId`, `relationshipTypeId`, `departmentId`, `designationId`.
4. Full request/response samples are in:
`docs/apis/employee/employee-information-sample-payloads.json`
