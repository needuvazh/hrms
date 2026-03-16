# Step 5 - Compliance-supporting HR Masters API

Implementation module: `hrms-modules/hrms-master-data`

Base path:

- `/api/compliance`

Resources:

- `visa-types`
- `residence-statuses`
- `labour-card-types`
- `civil-id-types`
- `passport-types`
- `sponsor-types`
- `work-permit-types`
- `nationalisation-categories`
- `social-insurance-types`
- `beneficiary-types`
- `dependent-types`

Endpoints for each resource:

- `POST /api/compliance/{resource}`
- `PUT /api/compliance/{resource}/{id}`
- `GET /api/compliance/{resource}/{id}`
- `GET /api/compliance/{resource}?q=&active=&limit=&offset=`
- `PATCH /api/compliance/{resource}/{id}/status`
- `GET /api/compliance/{resource}/options?q=&limit=`

Status payload:

```json
{ "active": true }
```

Common request payload (`MasterRequest`):

```json
{
  "code": "VIS-EMP",
  "name": "Employment Visa",
  "visaCategory": "WORK",
  "appliesTo": "EMPLOYEE",
  "renewableFlag": true,
  "expiryTrackingRequired": true,
  "omaniFlag": false,
  "countsForOmanisationFlag": false,
  "pensionEligibleFlag": false,
  "occupationalHazardEligibleFlag": true,
  "govtContributionApplicableFlag": false,
  "priorityOrder": 1,
  "insuranceEligibleFlag": true,
  "familyVisaEligibleFlag": true,
  "description": "Used for employee onboarding",
  "active": true
}
```

Response payload (`MasterViewDto`) fields:

- `id`, `tenantId`, `code`, `name`
- `visaCategory`, `appliesTo`, `renewableFlag`
- `expiryTrackingRequired`, `omaniFlag`, `countsForOmanisationFlag`
- `pensionEligibleFlag`, `occupationalHazardEligibleFlag`, `govtContributionApplicableFlag`
- `priorityOrder`, `insuranceEligibleFlag`, `familyVisaEligibleFlag`
- `description`, `active`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

Enum constraints:

- `appliesTo` for visa/sponsor: `EMPLOYEE`, `DEPENDENT`, `BOTH`
- `appliesTo` for civil-id: `OMANI`, `EXPATRIATE`, `BOTH`

Validation notes:

- `code` and `name` are mandatory.
- `code` must be unique within tenant for each resource.
- `priorityOrder` must be `>= 0` for beneficiary types.

Options response sample:

```json
[
  { "id": "uuid", "code": "VIS-EMP", "name": "Employment Visa" }
]
```
