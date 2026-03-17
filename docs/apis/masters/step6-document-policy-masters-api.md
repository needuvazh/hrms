# Step 6 - Document and Policy Masters API

Implementation module: `hrms-modules/hrms-master-data`

Base path:

- `/api/document-policy`

Resources:

- `document-categories`
- `document-types`
- `document-applicability-rules`
- `document-expiry-rules`
- `policy-document-types`
- `policy-acknowledgement-types`
- `attachment-categories`

Endpoints for each resource:

- `POST /api/document-policy/{resource}`
- `PUT /api/document-policy/{resource}/{id}`
- `GET /api/document-policy/{resource}/{id}`
- `GET /api/document-policy/{resource}`
- `PATCH /api/document-policy/{resource}/{id}/status`
- `GET /api/document-policy/{resource}/options`

List query params:

- Common: `q`, `active`, `limit`, `offset`, `sort`
- Document Type: `documentCategoryId`, `documentFor`
- Document Applicability Rule: `documentTypeId`, `workerTypeId`, `employeeCategoryId`, `nationalisationCategoryId`, `legalEntityId`, `jobFamilyId`, `designationId`, `dependentTypeId`, `mandatoryFlag`, `onboardingRequiredFlag`
- Document Expiry Rule: `documentTypeId`, `expiryTrackingRequired`, `renewalRequired`, `blockTransactionOnExpiryFlag`
- Attachment Category: `mimeGroup`

Options query params:

- `q`, `limit`, `activeOnly` (defaults to `true`)

Status payload:

```json
{ "active": true }
```

Common request payload (`MasterRequest`) uses a unified contract; provide only resource-relevant fields.

Key enum fields:

- `documentFor`: `EMPLOYEE`, `EMPLOYER`, `DEPENDENT`, `BOTH`
- `mimeGroup`: `PDF`, `IMAGE`, `OFFICE_DOC`, `ARCHIVE`, `OTHER`

Example document type create:

```json
{
  "code": "PASSPORT",
  "name": "Passport",
  "shortDescription": "Employee passport copy",
  "documentFor": "EMPLOYEE",
  "documentCategoryId": "uuid",
  "attachmentRequired": true,
  "issueDateRequired": true,
  "expiryDateRequired": true,
  "referenceNoRequired": true,
  "multipleAllowed": false,
  "active": true
}
```

Example expiry rule create:

```json
{
  "code": "PASSPORT_EXPIRY",
  "name": "Passport expiry rule",
  "documentTypeId": "uuid",
  "expiryTrackingRequired": true,
  "renewalRequired": true,
  "alertDaysBefore": [90, 60, 30, 7, 1],
  "gracePeriodDays": 0,
  "blockTransactionOnExpiryFlag": true,
  "active": true
}
```

`GET /api/document-policy/{resource}` response is paged and returns:

```json
{
  "items": [
    {
      "id": "uuid",
      "tenantId": "default",
      "code": "PASSPORT",
      "name": "Passport",
      "active": true,
      "createdAt": "2026-01-01T00:00:00Z",
      "updatedAt": "2026-01-01T00:00:00Z",
      "createdBy": "system",
      "updatedBy": "system"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

Each item in `items` is `MasterViewDto` and includes resource metadata + active/audit fields:

- `id`, `tenantId`, `code`, `name`
- Resource-specific properties (document flags, rule links, alert days, policy flags, mime limits)
- `active`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

Options response sample:

```json
[
  { "id": "uuid", "code": "PASSPORT", "name": "Passport" }
]
```
