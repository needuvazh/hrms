SYSTEM ROLE
You are a senior enterprise UI architect and frontend engineer.

STACK
React
TypeScript
Enterprise admin UI

----------------------------------------------------

Perfect — here is a README-style API contract you can hand over directly to the UI agent for Step 3 Organization Masters.
# Step 3 Organization Masters API Contract (UI Integration)
## Base
- Base path: `/api/organization`
- Content-Type: `application/json`
- IDs are UUIDs
- All APIs are tenant-scoped in backend context (UI should send tenant context headers/token as configured in app shell)
---
## Common Patterns
### List API (all masters)
`GET /api/organization/{resource}`
Query params:
- `q` (optional, string): search keyword
- `active` (optional, boolean): filter by active state
- `limit` (optional, int, default `50`)
- `offset` (optional, int, default `0`)
Response:
- `200 OK`
- JSON array of master DTO objects
### Get by ID
`GET /api/organization/{resource}/{id}`
Response:
- `200 OK`
- JSON object (master DTO)
### Create
`POST /api/organization/{resource}`
Response:
- `200 OK`
- JSON object (created master DTO)
### Update
`PUT /api/organization/{resource}/{id}`
Response:
- `200 OK`
- JSON object (updated master DTO)
### Status Toggle
`PATCH /api/organization/{resource}/{id}/status`
Request:
```json
{ "active": true }
Response:
- 200 OK
- JSON object (updated master DTO)
Options
GET /api/organization/{resource}/options
Query params:
- q (optional, string)
- limit (optional, int, default 100)
Response:
[
  { "id": "uuid", "code": "CODE", "name": "Display Name" }
]
---
## Endpoint Map
- Legal Entity: `legal-entities`
- Branch: `branches`
- Business Unit: `business-units`
- Division: `divisions`
- Department: `departments`
- Section: `sections`
- Work Location: `work-locations`
- Cost Center: `cost-centers`
- Reporting Unit: `reporting-units`
---
Request Objects
1) Legal Entity
POST/PUT body:
{
  "legalEntityCode": "LE-001",
  "legalEntityName": "Acme Legal Entity",
  "shortName": "Acme LE",
  "registrationNo": "REG-123",
  "taxNo": "TAX-123",
  "countryCode": "AE",
  "baseCurrencyCode": "AED",
  "defaultLanguageCode": "en",
  "contactEmail": "admin@acme.com",
  "contactPhone": "+971500000000",
  "addressLine1": "Address 1",
  "addressLine2": "Address 2",
  "city": "Dubai",
  "state": "Dubai",
  "postalCode": "00000",
  "active": true
}
2) Branch
{
  "legalEntityId": "uuid",
  "branchCode": "BR-001",
  "branchName": "Dubai Branch",
  "branchShortName": "DXB",
  "addressLine1": "Address 1",
  "addressLine2": "Address 2",
  "city": "Dubai",
  "state": "Dubai",
  "countryCode": "AE",
  "postalCode": "00000",
  "phone": "+971500000000",
  "fax": "+971400000000",
  "email": "branch@acme.com",
  "active": true
}
3) Business Unit
{
  "legalEntityId": "uuid",
  "businessUnitCode": "BU-001",
  "businessUnitName": "Corporate BU",
  "description": "Corporate functions",
  "active": true
}
legalEntityId is optional.
4) Division
{
  "legalEntityId": "uuid",
  "businessUnitId": "uuid",
  "branchId": "uuid",
  "divisionCode": "DIV-001",
  "divisionName": "Operations",
  "description": "Ops division",
  "active": true
}
legalEntityId, businessUnitId, branchId are optional.
5) Department
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
legalEntityId, businessUnitId, divisionId, branchId are optional.
6) Section
{
  "departmentId": "uuid",
  "sectionCode": "SEC-001",
  "sectionName": "Accounts Payable",
  "description": "AP section",
  "active": true
}
departmentId is required.
7) Work Location
{
  "legalEntityId": "uuid",
  "branchId": "uuid",
  "locationCode": "LOC-001",
  "locationName": "HQ Building",
  "locationType": "OFFICE",
  "addressLine1": "Address 1",
  "addressLine2": "Address 2",
  "city": "Dubai",
  "state": "Dubai",
  "countryCode": "AE",
  "postalCode": "00000",
  "latitude": 25.2048,
  "longitude": 55.2708,
  "geofenceRadius": 100.0,
  "active": true
}
locationType enum values:
- OFFICE
- SITE
- PLANT
- WAREHOUSE
- REMOTE
- CLIENT_SITE
8) Cost Center
{
  "legalEntityId": "uuid",
  "costCenterCode": "CC-001",
  "costCenterName": "Finance Cost Center",
  "description": "Finance allocation",
  "glAccountCode": "GL-1000",
  "parentCostCenterId": "uuid",
  "active": true
}
legalEntityId and parentCostCenterId are optional.
9) Reporting Unit
{
  "reportingUnitCode": "RU-001",
  "reportingUnitName": "Corporate Reporting",
  "parentReportingUnitId": "uuid",
  "description": "Top reporting unit",
  "active": true
}
parentReportingUnitId is optional.
---
Response Objects
All master responses include audit fields (createdAt, updatedAt, createdBy, updatedBy) and tenantId.
LegalEntityDto
{
  "id": "uuid",
  "tenantId": "default",
  "legalEntityCode": "LE-001",
  "legalEntityName": "Acme Legal Entity",
  "shortName": "Acme LE",
  "registrationNo": "REG-123",
  "taxNo": "TAX-123",
  "countryCode": "AE",
  "baseCurrencyCode": "AED",
  "defaultLanguageCode": "en",
  "contactEmail": "admin@acme.com",
  "contactPhone": "+971500000000",
  "addressLine1": "Address 1",
  "addressLine2": "Address 2",
  "city": "Dubai",
  "state": "Dubai",
  "postalCode": "00000",
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
BranchDto
{
  "id": "uuid",
  "tenantId": "default",
  "legalEntityId": "uuid",
  "branchCode": "BR-001",
  "branchName": "Dubai Branch",
  "branchShortName": "DXB",
  "addressLine1": "Address 1",
  "addressLine2": "Address 2",
  "city": "Dubai",
  "state": "Dubai",
  "countryCode": "AE",
  "postalCode": "00000",
  "phone": "+971500000000",
  "fax": "+971400000000",
  "email": "branch@acme.com",
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
BusinessUnitDto
{
  "id": "uuid",
  "tenantId": "default",
  "legalEntityId": "uuid",
  "businessUnitCode": "BU-001",
  "businessUnitName": "Corporate BU",
  "description": "Corporate functions",
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
DivisionDto
{
  "id": "uuid",
  "tenantId": "default",
  "legalEntityId": "uuid",
  "businessUnitId": "uuid",
  "branchId": "uuid",
  "divisionCode": "DIV-001",
  "divisionName": "Operations",
  "description": "Ops division",
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
DepartmentDto
{
  "id": "uuid",
  "tenantId": "default",
  "legalEntityId": "uuid",
  "businessUnitId": "uuid",
  "divisionId": "uuid",
  "branchId": "uuid",
  "departmentCode": "DEP-001",
  "departmentName": "Finance",
  "shortName": "FIN",
  "description": "Finance department",
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
SectionDto
{
  "id": "uuid",
  "tenantId": "default",
  "departmentId": "uuid",
  "sectionCode": "SEC-001",
  "sectionName": "Accounts Payable",
  "description": "AP section",
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
WorkLocationDto
{
  "id": "uuid",
  "tenantId": "default",
  "legalEntityId": "uuid",
  "branchId": "uuid",
  "locationCode": "LOC-001",
  "locationName": "HQ Building",
  "locationType": "OFFICE",
  "addressLine1": "Address 1",
  "addressLine2": "Address 2",
  "city": "Dubai",
  "state": "Dubai",
  "countryCode": "AE",
  "postalCode": "00000",
  "latitude": 25.2048,
  "longitude": 55.2708,
  "geofenceRadius": 100.0,
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
CostCenterDto
{
  "id": "uuid",
  "tenantId": "default",
  "legalEntityId": "uuid",
  "costCenterCode": "CC-001",
  "costCenterName": "Finance Cost Center",
  "description": "Finance allocation",
  "glAccountCode": "GL-1000",
  "parentCostCenterId": "uuid",
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
ReportingUnitDto
{
  "id": "uuid",
  "tenantId": "default",
  "reportingUnitCode": "RU-001",
  "reportingUnitName": "Corporate Reporting",
  "parentReportingUnitId": "uuid",
  "description": "Top reporting unit",
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
---
Tree / Chart APIs
GET /api/organization/tree
GET /api/organization/chart
Response:
{
  "nodes": [
    {
      "type": "LEGAL_ENTITY",
      "id": "uuid",
      "code": "LE-001",
      "name": "Acme Legal Entity",
      "active": true,
      "children": [
        {
          "type": "BRANCH",
          "id": "uuid",
          "code": "BR-001",
          "name": "Dubai Branch",
          "active": true,
          "children": []
        }
      ]
    }
  ]
}
Node shape:
{
  "type": "string",
  "id": "uuid|null",
  "code": "string",
  "name": "string",
  "active": true,
  "children": [ /* same node shape */ ]
}
---
UI Notes (Important)
- List endpoints return arrays (not paged wrapper), so UI pager should be client-driven unless backend paging metadata is added later.
- status toggle endpoint returns full updated entity object.
- options endpoints return normalized {id, code, name} for dropdowns.
- Use UUID type in frontend models for IDs.
- active exists in all master request/response payloads.
- Validation constraints currently enforced at API layer include required fields, email, lat/long ranges, geofence minimum, and hierarchy cycle prevention (cost center/reporting unit).

MODULE
Step 3 — Organization Structure Masters

Design the UI and validation system for:

Legal Entity
Branch
Business Unit
Division
Department
Section
Work Location
Cost Center
Reporting Unit

----------------------------------------------------

UI GOALS

Enterprise HRMS admin interface

Must support:

organization setup
flexible hierarchy
organization visualization
employee assignment readiness

Design must be reusable and consistent.

----------------------------------------------------

SCREEN STRUCTURE

For each module create:

List screen
Create screen
Edit screen
View screen
Status toggle
Audit log placeholder

----------------------------------------------------

MODULE UI DETAILS

LEGAL ENTITY

Columns

Legal Entity Code
Legal Entity Name
Country
Base Currency
Default Language
Contact Email
Status

Form fields

Legal Entity Code
Legal Entity Name
Short Name
Registration Number
Tax Number
Country
Base Currency
Default Language
Contact Email
Contact Phone
Address
City
State
Postal Code
Active


BRANCH

Columns

Branch Code
Branch Name
Legal Entity
Country
Phone
Email
Status

Fields

Legal Entity
Branch Code
Branch Name
Branch Short Name
Address
City
State
Country
Postal Code
Phone
Fax
Email
Active


BUSINESS UNIT

Columns

Code
Name
Legal Entity
Status

Fields

Code
Name
Legal Entity optional
Description
Active


DIVISION

Columns

Division Code
Division Name
Business Unit
Branch
Status

Fields

Division Code
Division Name
Business Unit optional
Branch optional
Description
Active


DEPARTMENT

Columns

Department Code
Department Name
Division
Branch
Status

Fields

Department Code
Department Name
Division optional
Branch optional
Description
Active


SECTION

Columns

Section Code
Section Name
Department
Status

Fields

Section Code
Section Name
Department
Description
Active


WORK LOCATION

Columns

Location Code
Location Name
Type
Branch
Country
Status

Fields

Location Code
Location Name
Location Type
Legal Entity optional
Branch optional
Address
Country
Latitude
Longitude
Geofence Radius
Active


COST CENTER

Columns

Cost Center Code
Cost Center Name
Parent
GL Account
Status

Fields

Cost Center Code
Cost Center Name
Parent Cost Center
GL Account Code
Description
Active


REPORTING UNIT

Columns

Reporting Unit Code
Reporting Unit Name
Parent
Status

Fields

Reporting Unit Code
Reporting Unit Name
Parent Reporting Unit
Description
Active


----------------------------------------------------

SPECIAL SCREEN

Organization Tree

Features

expand collapse
node type icons
filter by legal entity
show active only
click node to open details


----------------------------------------------------

VALIDATIONS

Common

required code
required name
trim whitespace
code uniqueness
show inline errors
disable submit if invalid

Legal Entity

code unique
email format
phone format

Branch

legal entity required
code unique

Department

code unique
name required

Section

department required

Work Location

latitude between -90 and 90
longitude between -180 and 180
geofence >= 0

Cost Center

prevent parent = self

Reporting Unit

prevent parent = self


----------------------------------------------------

UX REQUIREMENTS

Use consistent table layout
Top right action buttons
Status badges
Confirmation dialogs
Toast notifications
Empty state messages
Loading state

----------------------------------------------------

API INTEGRATION

Use endpoints

GET /api/organization/{master}
POST /api/organization/{master}
PUT /api/organization/{master}/{id}
PATCH /api/organization/{master}/{id}/status
GET /api/organization/{master}/options

Tree API

GET /api/organization/tree


----------------------------------------------------

OUTPUT FORMAT

Return answer in this order

1 screen map
2 reusable UI pattern
3 page specifications
4 form fields
5 validation rules
6 business validation
7 component structure
8 folder structure
9 state management
10 api integration
11 table configs
12 modal vs page design
13 organization tree UI
14 sample JSON payloads
15 edge cases
16 reusable component suggestions
