# Employee Information Management APIs

## Create employee
POST /api/v1/employee-information/employees

```json
{
  "firstName": "Ahmed",
  "lastName": "Al Balushi",
  "officialEmail": "ahmed.albalushi@company.com",
  "primaryMobileNumber": "+96899112233",
  "employeeStatus": "ACTIVE",
  "dateOfJoining": "2026-03-01",
  "workforceCategory": "OMANI_NATIONAL",
  "jobTitle": "HR Executive",
  "actor": "hr.admin"
}
```

## Update employee status
PATCH /api/v1/employee-information/employees/{employeeId}/status

```json
{
  "employeeStatus": "INACTIVE",
  "actor": "hr.admin"
}
```

## Upload document
POST /api/v1/employee-information/employees/{employeeId}/documents/upload
Content-Type: multipart/form-data

Parts:
- file
- documentType = PASSPORT
- documentName = Employee Passport
- issueDate = 2024-01-10
- expiryDate = 2034-01-09
- alertEnabled = true
- actor = hr.admin

## Search employees
GET /api/v1/employee-information/employees?employeeName=Ahmed&employeeStatus=ACTIVE&limit=20&offset=0

## Notes
- This iteration delivers full schema, DTO surface, and core employee profile CRUD/search/status.
- Remaining child-domain APIs are scaffolded and return `PENDING_IMPLEMENTATION` until completed in follow-up commits.
