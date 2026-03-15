You are a senior enterprise UI architect and senior frontend developer.

I am building a multi-tenant enterprise HRMS product. I want you to design and generate the frontend UI specification and implementation plan for Phase 1 SaaS platform masters.

Great — here is a README-style API contract for step1-ui.md scope (Tenant, Branding, Language, Country, Country Preferences), excluding subscription/feature flag.
# Phase 1 SaaS Masters API Contract (For UI Agent)
## Scope
Included:
1. Tenant
2. Tenant Branding
3. Tenant Language (via reference + localization usage)
4. Tenant Country
5. Tenant Country Preferences
Excluded:
- Subscription Plan
- Feature Flag
---
## Base Endpoints
- SaaS tenant master APIs: `/api/saas/tenants`
- Tenant country APIs (existing tenant module): `/api/v1/tenants`
- Global reference masters: `/api/reference/{resource}`
---
## 1) Tenant APIs
## Create Tenant
`POST /api/saas/tenants`
Request:
```json
{
  "tenantCode": "acme_hr",
  "tenantName": "Acme HR",
  "legalName": "Acme HR Technologies LLC",
  "contactEmail": "admin@acme.com",
  "contactPhone": "+971501234567",
  "defaultTimezone": "Asia/Dubai",
  "goLiveDate": "2026-04-01",
  "defaultLanguageCode": "en",
  "homeCountryCode": "AE"
}
Response (TenantViewDto):
{
  "tenantCode": "acme_hr",
  "tenantName": "Acme HR",
  "legalName": "Acme HR Technologies LLC",
  "contactEmail": "admin@acme.com",
  "contactPhone": "+971501234567",
  "defaultTimezone": "Asia/Dubai",
  "goLiveDate": "2026-04-01",
  "defaultLanguageCode": "en",
  "homeCountryCode": "AE",
  "active": true,
  "createdAt": "2026-03-15T10:00:00Z",
  "updatedAt": "2026-03-15T10:00:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
Update Tenant
PUT /api/saas/tenants/{tenantCode}  
Request/response same as create.
Get Tenant
GET /api/saas/tenants/{tenantCode}  
Response: TenantViewDto (same shape above)
List Tenants
GET /api/saas/tenants?q=&active=&page=0&size=20&sort=&all=false
Response (PagedResult<TenantViewDto>):
{
  "items": [ /* TenantViewDto[] */ ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
Activate/Deactivate Tenant
PATCH /api/saas/tenants/{tenantCode}/status
Request:
{ "active": true }
Response: 204 No Content
---
2) Tenant Branding APIs
Upsert Branding
PUT /api/saas/tenants/{tenantCode}/branding
Request (TenantBrandingUpsertRequest):
{
  "brandName": "Acme HR",
  "logoUrl": "https://cdn.example.com/logo.png",
  "faviconUrl": "https://cdn.example.com/favicon.ico",
  "primaryColor": "#0A4D8C",
  "secondaryColor": "#F59E0B",
  "loginBannerUrl": "https://cdn.example.com/banner.png",
  "emailLogoUrl": "https://cdn.example.com/email-logo.png",
  "active": true
}
Response (TenantBrandingViewDto):
{
  "tenantCode": "acme_hr",
  "brandName": "Acme HR",
  "logoUrl": "https://cdn.example.com/logo.png",
  "faviconUrl": "https://cdn.example.com/favicon.ico",
  "primaryColor": "#0A4D8C",
  "secondaryColor": "#F59E0B",
  "loginBannerUrl": "https://cdn.example.com/banner.png",
  "emailLogoUrl": "https://cdn.example.com/email-logo.png",
  "active": true,
  "createdAt": "2026-03-15T10:10:00Z",
  "updatedAt": "2026-03-15T10:10:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
Get Branding
GET /api/saas/tenants/{tenantCode}/branding  
Response: TenantBrandingViewDto
---
3) Tenant Country Preference APIs (Localization)
Upsert Country Preference
PUT /api/saas/tenants/{tenantCode}/localization/{countryCode}
Request (TenantLocalizationUpsertRequest):
{
  "defaultLanguageCode": "en",
  "dateFormat": "dd/MM/yyyy",
  "timeFormat": "HH:mm",
  "weekStartDay": "MONDAY",
  "currencyCode": "AED",
  "numberFormat": "#,##0.00",
  "rtlEnabled": false,
  "publicHolidayCalendarCode": "AE-DXB",
  "calendarType": "GREGORIAN",
  "active": true
}
Response (TenantLocalizationViewDto):
{
  "id": "5b05a9f3-5f8c-4c7e-a832-18d9d0359c89",
  "tenantCode": "acme_hr",
  "countryCode": "AE",
  "defaultLanguageCode": "en",
  "dateFormat": "dd/MM/yyyy",
  "timeFormat": "HH:mm",
  "weekStartDay": "MONDAY",
  "currencyCode": "AED",
  "numberFormat": "#,##0.00",
  "rtlEnabled": false,
  "publicHolidayCalendarCode": "AE-DXB",
  "calendarType": "GREGORIAN",
  "active": true,
  "createdAt": "2026-03-15T10:20:00Z",
  "updatedAt": "2026-03-15T10:20:00Z",
  "createdBy": "system",
  "updatedBy": "system"
}
List Country Preferences
GET /api/saas/tenants/{tenantCode}/localization  
Response: TenantLocalizationViewDto[]
---
4) Tenant Country APIs
List Supported Countries (global)
GET /api/v1/tenants/countries
Response (CountryViewDto[]):
[
  {
    "countryCode": "AE",
    "countryName": "United Arab Emirates",
    "currencyCode": "AED",
    "timezone": "Asia/Dubai",
    "locale": "en-AE",
    "active": true
  }
]
List Tenant Countries
GET /api/v1/tenants/{tenantCode}/countries
Response (TenantCountryConfigViewDto[]):
[
  {
    "tenantCode": "acme_hr",
    "countryCode": "AE",
    "primaryCountry": true,
    "complianceProfile": "UAE_STANDARD",
    "effectiveFrom": "2026-01-01",
    "effectiveTo": null,
    "active": true
  }
]
---
5) Tenant Language Data APIs
Note: there is no dedicated /api/saas/tenants/{tenantCode}/languages CRUD endpoint currently.
Use:
1. defaultLanguageCode in tenant and localization payloads
2. global language reference options from reference master
Language options
GET /api/reference/languages/options?activeOnly=true
Response (ReferenceOptionViewDto[]):
[
  { "id": "uuid", "code": "en", "name": "English" },
  { "id": "uuid", "code": "ar", "name": "Arabic" }
]
Also useful:
- Countries: GET /api/reference/countries/options?activeOnly=true
- Currencies: GET /api/reference/currencies/options?activeOnly=true
---
6) Optional Consolidated Settings API
Tenant Settings Summary
GET /api/saas/tenants/{tenantCode}/settings
Response (TenantSettingsViewDto):
{
  "tenant": { /* TenantViewDto */ },
  "subscription": null,
  "branding": { /* TenantBrandingViewDto */ },
  "localizationPreferences": [ /* TenantLocalizationViewDto[] */ ],
  "enabledModules": [],
  "enabledFeatures": []
}
For Step 1 UI scope, ignore subscription and enabledFeatures.
---
7) Validation Notes for UI
- tenantCode: regex ^[a-z0-9_-]{2,64}$
- tenantName, legalName, defaultTimezone: required
- contactEmail: valid email
- Branding brandName: required
- Localization required: defaultLanguageCode, dateFormat, timeFormat, weekStartDay, currencyCode, numberFormat
- Status request uses { "active": boolean }
---
8) Suggested Screen-to-API Mapping
- Tenant List: GET /api/saas/tenants
- Tenant Create/Edit: POST/PUT /api/saas/tenants...
- Tenant Status Toggle: PATCH /api/saas/tenants/{tenantCode}/status
- Branding Tab: GET/PUT /api/saas/tenants/{tenantCode}/branding
- Countries Tab: GET /api/v1/tenants/countries + GET /api/v1/tenants/{tenantCode}/countries
- Country Preferences Tab: GET /api/saas/tenants/{tenantCode}/localization + PUT /api/saas/tenants/{tenantCode}/localization/{countryCode}
- Language dropdowns: GET /api/reference/languages/options?activeOnly=true

Important scope:
- Ignore Subscription Plan
- Ignore Feature Flag
- Focus only on:
  1. Tenant
  2. Tenant Branding
  3. Tenant Language
  4. Tenant Country
  5. Tenant Country Preferences

Business requirements:
- One tenant can support multiple languages
- One tenant can support multiple countries
- Branding is tenant-level
- Localization preferences are country-level under a tenant
- Need strong form validation
- Need admin-friendly enterprise screens
- Need future-ready responsive design
- Need clean UX for create/edit/view/list flows

Assume frontend stack is modern web application architecture.
If needed, use:
- React + TypeScript
- component-based architecture
- form validation library
- reusable data table
- reusable modal / drawer patterns
- API integration layer
- role-based action visibility placeholders

--------------------------------------------------
SCREENS TO DESIGN
--------------------------------------------------

1. TENANT LIST SCREEN
Columns:
- Tenant Code
- Tenant Name
- Legal Name
- Default Language
- Home Country
- Status
- Go Live Date
- Actions

Features:
- search by tenant code / tenant name
- filter by status
- pagination
- sorting
- row action menu
- view, edit, activate, deactivate

2. CREATE TENANT SCREEN
Fields:
- Tenant Code
- Tenant Name
- Legal Name
- Contact Email
- Contact Phone
- Default Timezone
- Go Live Date

Behavior:
- basic tenant info only
- language and country can be added in detail page or wizard step
- save and save-and-continue actions

3. TENANT DETAIL / EDIT SCREEN
Use tab-based layout:
- General Info
- Branding
- Languages
- Countries
- Country Preferences
- Audit Logs

4. BRANDING SCREEN / TAB
Fields:
- Brand Name
- Logo Upload
- Favicon Upload
- Primary Color
- Secondary Color
- Login Banner Upload
- Email Logo Upload
- Active toggle
- Live preview section

5. LANGUAGES SCREEN / TAB
Table columns:
- Language Code
- Language Name
- RTL
- Default
- Enabled
- Display Order
- Actions

Actions:
- add language
- edit language
- enable/disable
- set default

6. COUNTRIES SCREEN / TAB
Table columns:
- Country Code
- Country Name
- Currency
- Timezone
- Home Country
- Enabled
- Actions

Actions:
- add country
- edit country
- enable/disable
- set as home country

7. COUNTRY PREFERENCES SCREEN / TAB
Should allow selecting one tenant country and editing:
- Default Language
- Date Format
- Time Format
- Week Start Day
- Currency Code
- Number Format
- RTL Enabled
- Public Holiday Calendar Code
- Calendar Type

8. SETTINGS VIEW SCREEN
Optional read-only admin summary screen showing:
- tenant summary
- branding summary
- enabled languages
- enabled countries
- home country
- default language
- country-level preferences

--------------------------------------------------
VALIDATION REQUIREMENTS
--------------------------------------------------

Create detailed field-level and business-level validation.

Tenant validations:
- tenant code required
- tenant code format only lowercase letters, numbers, hyphen or underscore
- tenant code length limit
- tenant name required
- legal name required
- valid email format
- valid phone format
- timezone required
- go live date cannot be invalid

Branding validations:
- brand name required
- color code must be valid hex
- upload file type restrictions for logo/favicon/banner
- upload size limits
- preview should fail gracefully if image missing

Language validations:
- language code required
- language code must be unique within tenant
- language name required
- only one default language allowed
- cannot disable default language unless another default exists
- rtl auto-suggest when Arabic selected

Country validations:
- country code required
- country code unique within tenant
- country name required
- default currency required
- default timezone required
- only one home country allowed
- cannot disable home country unless another home country exists

Country preference validations:
- tenant country required
- default language must exist in tenant language list
- date format required
- time format required
- week start day required
- currency code required
- number format required
- if rtl enabled, UI should support preview or note
- one preference profile per tenant country

UX validations:
- show inline field error
- show top summary error for form submit
- disable submit when critical invalid state exists
- use confirmation modal for activate/deactivate/default/home actions
- show success toast and failure toast
- warn when navigating away with unsaved changes

--------------------------------------------------
UX / UI EXPECTATIONS
--------------------------------------------------

Design should be enterprise HRMS admin style:
- clean, professional, low-clutter
- tab-based tenant profile page
- reusable page header with breadcrumbs
- action buttons in top-right
- data tables for language/country lists
- drawer or modal for add/edit child records
- empty states for no language / no country / no branding
- clear status badges
- default badges
- confirmation dialogs for risky actions

Accessibility:
- keyboard accessible
- proper labels
- required field indicators
- accessible color contrast
- validation message readability

Responsive behavior:
- desktop first
- tablet supported
- mobile reasonable but admin-focused

--------------------------------------------------
OUTPUT EXPECTATION
--------------------------------------------------

I want the answer in this order:
1. screen map
2. page-by-page UI specification
3. form fields for each screen
4. validation rules for each field
5. business rule validations
6. component structure
7. frontend folder/module structure
8. state management approach
9. API integration mapping per screen
10. table configuration
11. modal/drawer configuration
12. sample UX flows
13. sample JSON payloads sent to backend
14. important UI edge cases
15. recommendations for reusable components

Do not give generic advice.
Give detailed, implementation-ready frontend specifications and validation behavior.
If code examples are included, use React + TypeScript style.