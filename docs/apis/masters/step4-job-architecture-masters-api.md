# Step 4 - Job Architecture Masters API

Implementation module: `hrms-modules/hrms-master-data`

Base path:

- `/api/job-architecture`

Required request context:

- Header: `X-Tenant-Id`
- Tenant must have module key `job-architecture` enabled.

Supported resources:

- `designations`
- `job-families`
- `job-functions`
- `grade-bands`
- `grades`
- `positions`
- `employment-types`
- `worker-types`
- `employee-categories`
- `employee-subcategories`
- `contract-types`
- `probation-policies`
- `notice-period-policies`
- `transfer-types`
- `promotion-types`
- `separation-reasons`

## Endpoints

Common endpoints for every resource:

- `POST /api/job-architecture/{resource}`
- `PUT /api/job-architecture/{resource}/{id}`
- `GET /api/job-architecture/{resource}/{id}`
- `GET /api/job-architecture/{resource}`
- `PATCH /api/job-architecture/{resource}/{id}/status`
- `GET /api/job-architecture/{resource}/options`

List query params:

- Common: `q`, `active`, `limit` (default `50`, min `1`, max `500`), `offset` (default `0`, min `0`)
- Additional: `jobFamilyId`, `jobFunctionId`, `gradeBandId`, `designationId`, `gradeId`, `legalEntityId`, `branchId`, `departmentId`, `costCenterId`, `vacancyStatus`, `criticalPositionFlag`, `employeeCategoryId`

Options query params:

- `q`
- `limit` (default `100`, min `1`, max `500`)

## Request contract

Single request model is used for create/update across all resources.

Always required:

- `code` (string, non-blank)
- `name` (string, non-blank)

All other fields are optional and only relevant for specific resources:

- `shortName`, `description`, `active`
- `jobFamilyId`, `jobFunctionId`, `bandOrder`, `gradeBandId`, `rankingOrder`, `salaryScaleMin`, `salaryScaleMax`
- `designationId`, `gradeId`, `legalEntityId`, `branchId`, `businessUnitId`, `divisionId`, `departmentId`, `sectionId`, `workLocationId`, `costCenterId`, `reportingUnitId`, `reportsToPositionId`
- `approvedHeadcount`, `filledHeadcount`, `vacancyStatus`, `criticalPositionFlag`
- `contractRequired`, `employeeCategoryId`, `fixedTermFlag`, `defaultDurationDays`, `renewalAllowed`
- `durationDays`, `extensionAllowed`, `maxExtensionDays`, `confirmationRequired`
- `employeeNoticeDays`, `employerNoticeDays`, `paymentInLieuAllowed`, `gardenLeaveAllowed`
- `separationCategory`, `voluntaryFlag`, `finalSettlementRequired`

Example upsert payload:

```json
{
  "code": "POS-001",
  "name": "Finance Manager Muscat",
  "designationId": "9ce1880a-9726-49f8-a5fd-f9654eaebeca",
  "gradeId": "cbf96c2e-050f-47a6-9faa-a5f336be8a8d",
  "reportsToPositionId": "8ce1880a-9726-49f8-a5fd-f9654eaebec0",
  "approvedHeadcount": 2,
  "filledHeadcount": 1,
  "vacancyStatus": "PARTIALLY_FILLED",
  "active": true
}
```

Status payload:

```json
{
  "active": true
}
```

## Response contract

`MasterViewDto` fields returned by create/update/get/list/status endpoints:

- `id`
- `tenantId`
- `code`
- `name`
- `description`
- `reportsToPositionId`
- `approvedHeadcount`
- `filledHeadcount`
- `vacancyStatus`
- `active`
- `createdAt`
- `updatedAt`
- `createdBy`
- `updatedBy`

Note: the response is intentionally compact and does not echo every request field.

Options response (`OptionViewDto`):

```json
[
  {
    "id": "9ce1880a-9726-49f8-a5fd-f9654eaebeca",
    "code": "DES-001",
    "name": "Software Engineer"
  }
]
```

## Resource validation rules

- `designations`: validates `jobFamilyId` and `jobFunctionId` when provided.
- `job-functions`: validates `jobFamilyId` when provided.
- `grades`: validates `gradeBandId` when provided; `salaryScaleMin <= salaryScaleMax`.
- `positions`:
  - Requires `designationId` and `gradeId`.
  - `approvedHeadcount >= 0`, `filledHeadcount >= 0`, and `filledHeadcount <= approvedHeadcount`.
  - `vacancyStatus` must be one of: `VACANT`, `PARTIALLY_FILLED`, `FILLED`, `FROZEN`.
  - Validates linked ids across `job_architecture` and `organization` schemas when provided.
  - Prevents self-reporting and circular reporting chains.
- `employee-subcategories`: requires `employeeCategoryId` and checks existence.
- `contract-types`: `defaultDurationDays` must be positive if provided.
- `probation-policies`:
  - `durationDays` is required and must be `> 0`.
  - If `extensionAllowed=true`, `maxExtensionDays` must be `>= 0` when provided.
  - If `extensionAllowed=false`, `maxExtensionDays` must be `null` or `0`.
- `notice-period-policies`: `employeeNoticeDays` and `employerNoticeDays` must be `>= 0` when provided.
- `separation-reasons`:
  - Requires `separationCategory`.
  - Allowed values: `RESIGNATION`, `TERMINATION`, `RETIREMENT`, `CONTRACT_EXPIRY`, `DEATH`, `ABSCONDING`, `OTHER`.

## Common error codes

- `TENANT_REQUIRED`
- `NOT_FOUND`
- `CODE_EXISTS`
- `CONSTRAINT_VIOLATION`
- `CODE_NAME_REQUIRED`
- `INVALID_SALARY_RANGE`
- `DESIGNATION_GRADE_REQUIRED`
- `INVALID_HEADCOUNT`
- `HEADCOUNT_EXCEEDED`
- `INVALID_VACANCY_STATUS`
- `POSITION_CYCLE`
- `POSITION_DEACTIVATION_BLOCKED`
- `SEPARATION_CATEGORY_REQUIRED`
- `INVALID_SEPARATION_CATEGORY`
