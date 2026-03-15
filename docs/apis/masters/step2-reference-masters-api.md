# Step 2 - Global Reference Masters API

Base path:

- `/api/reference/{resource}`

Controller uses one generic contract for all global references.

## Supported resources

- `countries`
- `currencies`
- `languages`
- `nationalities`
- `religions`
- `genders`
- `marital-statuses`
- `relationship-types`
- `document-types`
- `education-levels`
- `certification-types`
- `skill-categories`
- `skills`

## Common endpoints for each resource

- `POST /api/reference/{resource}`
- `PUT /api/reference/{resource}/{id}`
- `GET /api/reference/{resource}/{id}`
- `GET /api/reference/{resource}?q=&active=&page=&size=&sort=&all=`
- `PATCH /api/reference/{resource}/{id}/status`
- `GET /api/reference/{resource}/options?activeOnly=true`

## Request objects

Upsert request (`ReferenceMasterUpsertRequest`):

```json
{
  "code": "AE",
  "name": "United Arab Emirates",
  "shortName": "UAE",
  "iso2Code": "AE",
  "iso3Code": "ARE",
  "phoneCode": "+971",
  "nationalityName": "Emirati",
  "defaultCurrencyCode": "AED",
  "defaultTimezone": "Asia/Dubai",
  "gccFlag": true,
  "decimalPlaces": 2,
  "nativeName": "الإمارات",
  "rtlEnabled": false,
  "countryCode": "AE",
  "gccNationalFlag": true,
  "omaniFlag": false,
  "displayOrder": 1,
  "dependentAllowed": true,
  "emergencyContactAllowed": true,
  "beneficiaryAllowed": true,
  "shortDescription": "Passport",
  "documentFor": "EMPLOYEE",
  "issueDateRequired": true,
  "expiryDateRequired": true,
  "alertRequired": true,
  "alertDaysBefore": 30,
  "rankingOrder": 1,
  "expiryTrackingRequired": true,
  "issuingBodyRequired": true,
  "description": "Reference description",
  "skillCategoryId": "11111111-1111-1111-1111-111111111111",
  "active": true
}
```

Status request (`ReferenceStatusUpdateRequest`):

```json
{ "active": true }
```

## Response objects

Single/list item (`ReferenceMasterViewDto`) fields:

- `id`, `code`, `name`
- `shortName`, `iso2Code`, `iso3Code`, `phoneCode`, `nationalityName`
- `defaultCurrencyCode`, `defaultTimezone`, `gccFlag`, `decimalPlaces`
- `nativeName`, `rtlEnabled`, `countryCode`, `gccNationalFlag`, `omaniFlag`
- `displayOrder`, `dependentAllowed`, `emergencyContactAllowed`, `beneficiaryAllowed`
- `shortDescription`, `documentFor`, `issueDateRequired`, `expiryDateRequired`
- `alertRequired`, `alertDaysBefore`, `rankingOrder`, `expiryTrackingRequired`, `issuingBodyRequired`
- `description`, `skillCategoryId`, `skillCategoryName`
- `active`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

List wrapper (`PagedResult<ReferenceMasterViewDto>`):

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

Option item (`ReferenceOptionViewDto`):

```json
{ "id": "uuid", "code": "AE", "name": "United Arab Emirates" }
```

## Resource-specific guidance

- `countries`: use `iso2Code`, `iso3Code`, `phoneCode`, `defaultCurrencyCode`, `defaultTimezone`, `gccFlag`.
- `currencies`: use `code`, `name`, `decimalPlaces`.
- `languages`: use `code`, `name`, `nativeName`, `rtlEnabled`.
- `nationalities`: use `code`, `name`, `countryCode`, `gccNationalFlag`, `omaniFlag`.
- `religions`, `genders`, `marital-statuses`, `relationship-types`: mostly `code`, `name`, `displayOrder`.
- `document-types`: use `documentFor`, `shortDescription`, issue/expiry/alert flags.
- `education-levels`: use `rankingOrder`.
- `certification-types`: use `expiryTrackingRequired`, `issuingBodyRequired`.
- `skill-categories`: use `description`.
- `skills`: use `skillCategoryId`, `description`.
