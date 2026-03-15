# Step 3 - Organization Structure Masters API

Implementation module: `hrms-modules/hrms-master-data`

Base path:

- `/api/organization`

Common patterns for all masters:

- List: `GET /api/organization/{resource}?q=&active=&limit=&offset=`
- Create: `POST /api/organization/{resource}`
- Get by id: `GET /api/organization/{resource}/{id}`
- Update: `PUT /api/organization/{resource}/{id}`
- Status: `PATCH /api/organization/{resource}/{id}/status`
- Options: `GET /api/organization/{resource}/options?q=&limit=`

Status request payload:

```json
{ "active": true }
```

## 1) Legal Entity (`legal-entities`)

Request:

```json
{
  "legalEntityCode": "LE-001",
  "legalEntityName": "Acme Legal Entity",
  "shortName": "Acme",
  "registrationNo": "REG-001",
  "taxNo": "TAX-001",
  "countryCode": "AE",
  "baseCurrencyCode": "AED",
  "defaultLanguageCode": "en",
  "contactEmail": "admin@acme.com",
  "contactPhone": "+971500000000",
  "addressLine1": "Line 1",
  "addressLine2": "Line 2",
  "city": "Dubai",
  "state": "Dubai",
  "postalCode": "00000",
  "active": true
}
```

Response fields: `id`, `tenantId`, same business fields, plus `createdAt`, `updatedAt`, `createdBy`, `updatedBy`.

## 2) Branch (`branches`)

Request:

```json
{
  "legalEntityId": "uuid",
  "branchCode": "BR-001",
  "branchName": "Dubai Branch",
  "branchShortName": "DXB",
  "addressLine1": "Line 1",
  "addressLine2": "Line 2",
  "city": "Dubai",
  "state": "Dubai",
  "countryCode": "AE",
  "postalCode": "00000",
  "phone": "+971500000000",
  "fax": "+971400000000",
  "email": "branch@acme.com",
  "active": true
}
```

Response includes: `id`, `tenantId`, `legalEntityId`, branch fields + audit fields.

## 3) Business Unit (`business-units`)

Request:

```json
{
  "legalEntityId": "uuid",
  "businessUnitCode": "BU-001",
  "businessUnitName": "Corporate",
  "description": "Corporate business unit",
  "active": true
}
```

Response includes: `id`, `tenantId`, `legalEntityId`, `businessUnitCode`, `businessUnitName`, `description`, `active` + audit fields.

## 4) Division (`divisions`)

Request:

```json
{
  "legalEntityId": "uuid",
  "businessUnitId": "uuid",
  "branchId": "uuid",
  "divisionCode": "DIV-001",
  "divisionName": "Operations",
  "description": "Ops division",
  "active": true
}
```

Response includes: `id`, `tenantId`, `legalEntityId`, `businessUnitId`, `branchId`, division fields + audit fields.

## 5) Department (`departments`)

Request:

```json
{
  "legalEntityId": "uuid",
  "businessUnitId": "uuid",
  "divisionId": "uuid",
  "branchId": "uuid",
  "departmentCode": "DEP-001",
  "departmentName": "Finance",
  "shortName": "FIN",
  "description": "Finance department",
  "active": true
}
```

Response includes: `id`, `tenantId`, parent ids, department fields + audit fields.

## 6) Section (`sections`)

Request:

```json
{
  "departmentId": "uuid",
  "sectionCode": "SEC-001",
  "sectionName": "Accounts Payable",
  "description": "AP section",
  "active": true
}
```

Response includes: `id`, `tenantId`, `departmentId`, section fields + audit fields.

## 7) Work Location (`work-locations`)

Request:

```json
{
  "legalEntityId": "uuid",
  "branchId": "uuid",
  "locationCode": "LOC-001",
  "locationName": "HQ Building",
  "locationType": "OFFICE",
  "addressLine1": "Line 1",
  "addressLine2": "Line 2",
  "city": "Dubai",
  "state": "Dubai",
  "countryCode": "AE",
  "postalCode": "00000",
  "latitude": 25.2048,
  "longitude": 55.2708,
  "geofenceRadius": 100.0,
  "active": true
}
```

`locationType` enum values:

- `OFFICE`, `SITE`, `PLANT`, `WAREHOUSE`, `REMOTE`, `CLIENT_SITE`

Response includes: `id`, `tenantId`, parent ids, location fields + audit fields.

## 8) Cost Center (`cost-centers`)

Request:

```json
{
  "legalEntityId": "uuid",
  "costCenterCode": "CC-001",
  "costCenterName": "Finance Cost Center",
  "description": "Finance allocation",
  "glAccountCode": "GL-1000",
  "parentCostCenterId": "uuid",
  "active": true
}
```

Response includes: `id`, `tenantId`, `legalEntityId`, cost center fields + audit fields.

## 9) Reporting Unit (`reporting-units`)

Request:

```json
{
  "reportingUnitCode": "RU-001",
  "reportingUnitName": "Corporate Reporting",
  "parentReportingUnitId": "uuid",
  "description": "Top-level reporting unit",
  "active": true
}
```

Response includes: `id`, `tenantId`, reporting fields + audit fields.

## Shared options response

All `/options` endpoints return `OptionViewDto[]`:

```json
[
  { "id": "uuid", "code": "DEP-001", "name": "Finance" }
]
```

## Organization Tree / Chart

Endpoints:

- `GET /api/organization/tree`
- `GET /api/organization/chart`

Response (`OrganizationTreeViewDto`):

```json
{
  "nodes": [
    {
      "type": "LEGAL_ENTITY",
      "id": "uuid",
      "code": "LE-001",
      "name": "Acme Legal Entity",
      "active": true,
      "children": []
    }
  ]
}
```

Tree node fields (`OrganizationNodeViewDto`):

- `type`, `id`, `code`, `name`, `active`, `children[]`
