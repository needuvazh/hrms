You are a senior enterprise UI architect and senior frontend developer.

I am building a multi-tenant enterprise HRMS product. I want you to design and generate the frontend UI specification and implementation plan for:

Step 2 — Global Reference Masters

These are reusable platform-wide masters used across the whole HRMS.

Created endpoint base is dynamic:
- /api/reference/{resource}
- resource values:
  - countries
  - currencies
  - languages
  - nationalities
  - religions
  - genders
  - marital-statuses
  - relationship-types
  - document-types
  - education-levels
  - certification-types
  - skill-categories
  - skills
I also used existing auth API for testing:
- POST /api/v1/auth/login
---
1) Login (used for all secured calls)
- Request
POST /api/v1/auth/login
Content-Type: application/json
{
  "username": "admin",
  "password": "admin"
}
- Response (200)
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "id": "8e2dffb8-26ad-4a89-bc84-f397f9772112",
    "username": "admin",
    "superAdmin": true,
    "canViewAllTenants": true,
    "roles": ["SUPER_ADMIN"]
  }
}
---
2) Create reference
- Request
POST /api/reference/{resource}
Authorization: Bearer <jwt>
Content-Type: application/json
{
  "code": "OMR",
  "name": "Omani Rial",
  "shortName": "OMR",
  "decimalPlaces": 3,
  "active": true
}
- Response (200)
{
  "id": "7898876a-7911-4d32-add8-c868edc217e9",
  "code": "OMR",
  "name": "Omani Rial",
  "shortName": "OMR",
  "decimalPlaces": 3,
  "active": true,
  "createdBy": "system",
  "updatedBy": "system",
  "createdAt": "2026-03-14T18:29:09.577308Z",
  "updatedAt": "2026-03-14T18:29:09.577308Z"
}
---
3) List reference (paged/search/sort/filter)
- Request
GET /api/reference/{resource}?q=java&active=true&page=0&size=10&sort=updated_at,desc
Authorization: Bearer <jwt>
- For skills:
GET /api/reference/skills?q=java&page=0&size=10&sort=skill_name,asc&skillCategoryId=<uuid>
- Response (200)
{
  "items": [
    {
      "id": "794740d9-afd9-433b-a786-20829af3dbd2",
      "code": "JAVA",
      "name": "Java",
      "skillCategoryId": "11ee625f-62ae-41e7-b7d4-422e223caf93",
      "skillCategoryName": "Technical",
      "active": true
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
---
4) Get by id
- Request
GET /api/reference/{resource}/{id}
Authorization: Bearer <jwt>
- Response (200)
{
  "id": "576f6f78-53b7-4198-ad39-acfb5f855c2f",
  "code": "OM",
  "name": "Oman",
  "iso2Code": "OM",
  "iso3Code": "OMN",
  "defaultCurrencyCode": "OMR",
  "active": true
}
- Not found (404)
{
  "errorCode": "REFERENCE_NOT_FOUND",
  "message": "Reference record not found"
}
---
5) Update by id
- Request
PUT /api/reference/{resource}/{id}
Authorization: Bearer <jwt>
Content-Type: application/json
{
  "code": "OM",
  "name": "Sultanate of Oman",
  "iso2Code": "OM",
  "iso3Code": "OMN",
  "defaultCurrencyCode": "OMR",
  "active": true
}
- Response (200)
{
  "id": "576f6f78-53b7-4198-ad39-acfb5f855c2f",
  "code": "OM",
  "name": "Sultanate of Oman",
  "active": true,
  "updatedBy": "system"
}
---
6) Status change
- Request
PATCH /api/reference/{resource}/{id}/status
Authorization: Bearer <jwt>
Content-Type: application/json
{
  "active": false
}
- Response (200)  
Empty body.
---
7) Options/dropdown
- Request
GET /api/reference/{resource}/options
Authorization: Bearer <jwt>
- optional:
GET /api/reference/{resource}/options?activeOnly=false
- Response (200)
[
  {
    "id": "7898876a-7911-4d32-add8-c868edc217e9",
    "code": "OMR",
    "name": "Omani Rial"
  }
]
---
8) Document type business-rule validation (implemented + verified)
- Invalid request
POST /api/reference/document-types
Authorization: Bearer <jwt>
Content-Type: application/json
{
  "code": "NID",
  "name": "National ID",
  "documentFor": "EMPLOYEE",
  "issueDateRequired": true,
  "expiryDateRequired": true,
  "alertRequired": false,
  "alertDaysBefore": 15,
  "active": true
}
- Response (400)
{
  "errorCode": "INVALID_ALERT_CONFIGURATION",
  "message": "Alert days cannot be positive when alert is disabled"
}

--------------------------------------------------
SCOPE
--------------------------------------------------

Create detailed UI and validation design for these modules:

1. Country
2. Currency
3. Language
4. Nationality
5. Religion
6. Gender
7. Marital Status
8. Relationship Type
9. Document Type
10. Education Level
11. Certification Type
12. Skill Category
13. Skill

Important business context:
- These are global reference masters, not tenant-owned masters
- They will be reused across employee profile, dependents, education, certifications, documents, training, recruitment, and reporting
- Some are simple lookup masters
- Some are richer and need more detailed forms, especially:
  - Document Type
  - Skill Category
  - Skill
  - Certification Type
- UI must be enterprise-admin friendly
- Need strong field validation and business validation
- Need reusable patterns because many screens are similar

Assume frontend stack is modern web application architecture.
If needed, use:
- React + TypeScript
- reusable component-based architecture
- data table/grid
- form validation library
- API integration layer
- modal/drawer patterns
- role-based action placeholders
- clean admin UX

--------------------------------------------------
SCREEN GROUPING STRATEGY
--------------------------------------------------

Design these screens using reusable patterns.

Group A — Simple Reference Masters
Use a common screen pattern for:
- Religion
- Gender
- Marital Status
- Education Level

Group B — Medium Reference Masters
Use a slightly richer pattern for:
- Country
- Currency
- Language
- Nationality
- Relationship Type
- Certification Type

Group C — Rich Reference Masters
Use full-featured forms for:
- Document Type
- Skill Category
- Skill

--------------------------------------------------
SCREENS TO DESIGN
--------------------------------------------------

For each module, design:

1. LIST SCREEN
Features:
- page title
- search
- status filter
- pagination
- sorting
- create button
- row actions
- view, edit, activate, deactivate

2. CREATE SCREEN OR MODAL
3. EDIT SCREEN OR MODAL
4. VIEW/DETAIL SCREEN where useful
5. STATUS CHANGE CONFIRMATION DIALOG
6. AUDIT LOG TAB or SECTION placeholder
7. OPTIONS/DROPDOWN USABILITY NOTES for how these records are used by downstream modules

--------------------------------------------------
MODULE-SPECIFIC UI REQUIREMENTS
--------------------------------------------------

1. COUNTRY
List columns:
- Country Code
- Country Name
- ISO2
- ISO3
- Phone Code
- Default Currency
- Default Timezone
- GCC
- Status

Form fields:
- Country Code
- Country Name
- Short Name
- ISO2 Code
- ISO3 Code
- Phone Code
- Nationality Name
- Default Currency Code
- Default Timezone
- GCC Flag
- Active

2. CURRENCY
List columns:
- Currency Code
- Currency Name
- Symbol
- Decimal Places
- Status

Form fields:
- Currency Code
- Currency Name
- Currency Symbol
- Decimal Places
- Active

3. LANGUAGE
List columns:
- Language Code
- Language Name
- Native Name
- RTL
- Status

Form fields:
- Language Code
- Language Name
- Native Name
- RTL Enabled
- Active

4. NATIONALITY
List columns:
- Nationality Code
- Nationality Name
- Country
- GCC National
- Omani
- Status

Form fields:
- Nationality Code
- Nationality Name
- Country Code
- GCC National Flag
- Omani Flag
- Active

5. RELIGION
List columns:
- Religion Code
- Religion Name
- Status

Form fields:
- Religion Code
- Religion Name
- Active

6. GENDER
List columns:
- Gender Code
- Gender Name
- Display Order
- Status

Form fields:
- Gender Code
- Gender Name
- Display Order
- Active

7. MARITAL STATUS
List columns:
- Marital Status Code
- Marital Status Name
- Status

Form fields:
- Marital Status Code
- Marital Status Name
- Active

8. RELATIONSHIP TYPE
List columns:
- Relationship Code
- Relationship Name
- Dependent Allowed
- Emergency Contact Allowed
- Beneficiary Allowed
- Status

Form fields:
- Relationship Type Code
- Relationship Type Name
- Dependent Allowed
- Emergency Contact Allowed
- Beneficiary Allowed
- Active

9. DOCUMENT TYPE
List columns:
- Document Type Code
- Document Type Name
- Document For
- Issue Date Required
- Expiry Date Required
- Alert Required
- Alert Days Before
- Status

Form fields:
- Document Type Code
- Document Type Name
- Short Description
- Document For
- Issue Date Required
- Expiry Date Required
- Alert Required
- Alert Days Before
- Active

Behavior:
- when Alert Required is false, disable Alert Days Before
- when Expiry Date Required is false, show helper text explaining expiry alerts may not apply

10. EDUCATION LEVEL
List columns:
- Education Level Code
- Education Level Name
- Ranking Order
- Status

Form fields:
- Education Level Code
- Education Level Name
- Ranking Order
- Active

11. CERTIFICATION TYPE
List columns:
- Certification Type Code
- Certification Type Name
- Expiry Tracking Required
- Issuing Body Required
- Status

Form fields:
- Certification Type Code
- Certification Type Name
- Expiry Tracking Required
- Issuing Body Required
- Active

12. SKILL CATEGORY
List columns:
- Skill Category Code
- Skill Category Name
- Description
- Status

Form fields:
- Skill Category Code
- Skill Category Name
- Description
- Active

13. SKILL
List columns:
- Skill Code
- Skill Name
- Skill Category
- Status

Form fields:
- Skill Code
- Skill Name
- Skill Category
- Description
- Active

Behavior:
- skill category must be selected
- skill category dropdown should use option API
- search and filtering by skill category required on list page

--------------------------------------------------
VALIDATION REQUIREMENTS
--------------------------------------------------

Create detailed field-level and business-level validation.

Common validation rules:
- code required
- name required
- code length limit
- name length limit
- trim whitespace
- prevent duplicate submissions
- show inline errors
- show form-level error summary on submit
- disable submit on invalid critical state
- success toast and failure toast
- confirm status change actions

Country validations:
- country code required and unique
- ISO2 required and unique
- ISO3 required and unique
- phone code format validation
- default currency must exist
- timezone must be valid
- country name required

Currency validations:
- currency code required and unique
- decimal places must be non-negative integer

Language validations:
- language code required and unique
- language name required
- native name optional or required based on design choice
- RTL checkbox behavior should be clear

Nationality validations:
- nationality code required and unique
- nationality name required
- country must exist when selected

Religion validations:
- religion code required and unique
- religion name required

Gender validations:
- gender code required and unique
- display order non-negative integer

Marital Status validations:
- marital status code required and unique
- marital status name required

Relationship Type validations:
- relationship type code required and unique
- at least one usage flag can be allowed or all optional based on design choice, but explain the UX behavior

Document Type validations:
- document type code required and unique
- document type name required
- document for required
- alert days before must be zero or positive
- if alert required is false, alert days before should be disabled or reset
- behavioral validation should be visible in UI

Education Level validations:
- education level code required and unique
- education level name required
- ranking order non-negative integer

Certification Type validations:
- certification type code required and unique
- certification type name required

Skill Category validations:
- skill category code required and unique
- skill category name required

Skill validations:
- skill code required and unique
- skill name required
- skill category required
- cannot save without valid category

--------------------------------------------------
UX / UI EXPECTATIONS
--------------------------------------------------

Design should be enterprise HRMS admin style:
- clean, professional, low clutter
- reusable table layout across masters
- consistent page header with breadcrumbs
- action buttons top-right
- status badge styles
- active/inactive filter
- confirmation dialogs
- empty states
- loading states
- no-results states
- clear validation messages
- drawer or modal for simple masters
- full page form for richer masters if appropriate

Accessibility:
- keyboard support
- labeled inputs
- accessible validation messaging
- readable contrast
- toggle and checkbox clarity

Responsive behavior:
- desktop first
- tablet supported
- mobile reasonable but admin-focused

--------------------------------------------------
REUSABILITY EXPECTATION
--------------------------------------------------

Define reusable frontend patterns for:
- reference master list page
- reference master form modal
- status change dialog
- search/filter bar
- option dropdown loading
- audit log section placeholder
- active/inactive badge
- boolean icon/toggle renderer
- row action menu

--------------------------------------------------
API INTEGRATION EXPECTATION
--------------------------------------------------

Map each screen to backend APIs such as:
- POST /api/reference/{master}
- GET /api/reference/{master}
- GET /api/reference/{master}/{id}
- PUT /api/reference/{master}/{id}
- PATCH /api/reference/{master}/{id}/status
- GET /api/reference/{master}/options

Also include:
- search parameter mapping
- pagination parameter mapping
- sorting parameter mapping
- skill category option loading
- default dropdown behavior for active options only

--------------------------------------------------
OUTPUT EXPECTATION
--------------------------------------------------

I want the answer in this order:
1. screen map
2. reusable UI design pattern for all reference masters
3. page-by-page UI specification
4. form fields for each module
5. field-level validation rules
6. business validation rules
7. component structure
8. frontend folder/module structure
9. state management approach
10. API integration mapping per screen
11. table configuration per module
12. modal/drawer/full-page decision per module
13. sample UX flows
14. sample JSON payloads sent to backend
15. important UI edge cases
16. recommendations for reusable components

Do not give generic advice.
Give detailed, implementation-ready frontend specifications and validation behavior.
If code examples are included, use React + TypeScript style.