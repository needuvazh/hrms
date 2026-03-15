# Step 1 - SaaS Masters API

Base paths:

- `/api/saas/tenants`
- `/api/saas/subscription-plans`
- `/api/saas/feature-flags`

Common status payload:

```json
{ "active": true }
```

## 1) Tenant

Endpoints:

- `POST /api/saas/tenants`
- `PUT /api/saas/tenants/{tenantCode}`
- `GET /api/saas/tenants/{tenantCode}`
- `GET /api/saas/tenants?q=&active=&page=&size=&sort=&all=`
- `PATCH /api/saas/tenants/{tenantCode}/status`

Request (`TenantUpsertRequest`):

```json
{
  "tenantCode": "acme",
  "tenantName": "Acme LLC",
  "legalName": "Acme Legal Name LLC",
  "contactEmail": "admin@acme.com",
  "contactPhone": "+971500000000",
  "defaultTimezone": "Asia/Dubai",
  "goLiveDate": "2026-01-01",
  "defaultLanguageCode": "en",
  "homeCountryCode": "AE"
}
```

Response item (`TenantViewDto`) fields:

- `tenantCode`, `tenantName`, `legalName`, `contactEmail`, `contactPhone`
- `defaultTimezone`, `goLiveDate`, `defaultLanguageCode`, `homeCountryCode`
- `active`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

List response wrapper (`PagedResult<TenantViewDto>`):

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}
```

## 2) Subscription Plan

Endpoints:

- `POST /api/saas/subscription-plans`
- `PUT /api/saas/subscription-plans/{id}`
- `GET /api/saas/subscription-plans/{id}`
- `GET /api/saas/subscription-plans?q=&active=&page=&size=&sort=&all=`
- `PATCH /api/saas/subscription-plans/{id}/status`
- `GET /api/saas/subscription-plans/options?activeOnly=true`

Request (`SubscriptionPlanUpsertRequest`):

```json
{
  "planCode": "BASIC",
  "planName": "Basic",
  "description": "Starter plan",
  "maxUsers": 100,
  "maxStorageGb": 50,
  "monthlyPrice": 99.0,
  "annualPrice": 990.0,
  "currencyCode": "AED",
  "active": true
}
```

Response (`SubscriptionPlanViewDto`) fields:

- `id`, `planCode`, `planName`, `description`
- `maxUsers`, `maxStorageGb`, `monthlyPrice`, `annualPrice`, `currencyCode`
- `active`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

## 3) Tenant Subscription

Endpoints:

- `PUT /api/saas/tenants/{tenantCode}/subscription`
- `GET /api/saas/tenants/{tenantCode}/subscription`

Request (`TenantSubscriptionUpsertRequest`) example:

```json
{
  "subscriptionPlanId": "11111111-1111-1111-1111-111111111111",
  "subscriptionStartDate": "2026-01-01",
  "subscriptionEndDate": "2026-12-31",
  "autoRenew": true,
  "active": true
}
```

Response (`TenantSubscriptionViewDto`) fields:

- `tenantCode`, `subscriptionPlanId`, `planCode`, `planName`
- `subscriptionStartDate`, `subscriptionEndDate`, `autoRenew`, `active`
- `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

## 4) Tenant Branding

Endpoints:

- `PUT /api/saas/tenants/{tenantCode}/branding`
- `GET /api/saas/tenants/{tenantCode}/branding`

Request (`TenantBrandingUpsertRequest`):

```json
{
  "brandName": "Acme HR",
  "logoUrl": "https://cdn/logo.png",
  "faviconUrl": "https://cdn/favicon.ico",
  "primaryColor": "#003366",
  "secondaryColor": "#FF9900",
  "loginBannerUrl": "https://cdn/banner.png",
  "emailLogoUrl": "https://cdn/email-logo.png",
  "active": true
}
```

Response (`TenantBrandingViewDto`) fields:

- `tenantCode`, `brandName`, `logoUrl`, `faviconUrl`
- `primaryColor`, `secondaryColor`, `loginBannerUrl`, `emailLogoUrl`
- `active`, `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

## 5) Tenant Localization Preferences (Country-level)

Endpoints:

- `PUT /api/saas/tenants/{tenantCode}/localization/{countryCode}`
- `GET /api/saas/tenants/{tenantCode}/localization`

Request (`TenantLocalizationUpsertRequest`):

```json
{
  "defaultLanguageCode": "en",
  "dateFormat": "dd/MM/yyyy",
  "timeFormat": "HH:mm",
  "weekStartDay": "MONDAY",
  "currencyCode": "AED",
  "numberFormat": "#,##0.00",
  "rtlEnabled": false,
  "publicHolidayCalendarCode": "AE-2026",
  "calendarType": "GREGORIAN",
  "active": true
}
```

Response (`TenantLocalizationViewDto`) fields:

- `id`, `tenantCode`, `countryCode`, `defaultLanguageCode`
- `dateFormat`, `timeFormat`, `weekStartDay`, `currencyCode`, `numberFormat`
- `rtlEnabled`, `publicHolidayCalendarCode`, `calendarType`, `active`
- `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

## 6) Tenant Languages (new)

Endpoints:

- `POST /api/saas/tenants/{tenantCode}/languages`
- `PUT /api/saas/tenants/{tenantCode}/languages/{languageCode}`
- `GET /api/saas/tenants/{tenantCode}/languages`
- `PATCH /api/saas/tenants/{tenantCode}/languages/{languageCode}/status`
- `PATCH /api/saas/tenants/{tenantCode}/languages/{languageCode}/default`

Upsert request (`TenantLanguageUpsertRequest`):

```json
{
  "languageCode": "en",
  "defaultLanguage": true,
  "active": true,
  "displayOrder": 1
}
```

Status request (`TenantLanguageStatusUpdateRequest`):

```json
{ "active": true }
```

Default request (`TenantLanguageDefaultUpdateRequest`):

```json
{ "defaultLanguage": true }
```

Response (`TenantLanguageViewDto`) fields:

- `id`, `tenantCode`, `languageCode`, `languageName`
- `defaultLanguage`, `active`, `displayOrder`
- `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

## 7) Tenant Countries (new)

Endpoints:

- `POST /api/saas/tenants/{tenantCode}/countries`
- `PUT /api/saas/tenants/{tenantCode}/countries/{countryCode}`
- `GET /api/saas/tenants/{tenantCode}/countries`
- `PATCH /api/saas/tenants/{tenantCode}/countries/{countryCode}/status`
- `PATCH /api/saas/tenants/{tenantCode}/countries/{countryCode}/home`

Upsert request (`TenantCountryUpsertRequest`):

```json
{
  "countryCode": "AE",
  "defaultCurrencyCode": "AED",
  "defaultTimezone": "Asia/Dubai",
  "homeCountry": true,
  "active": true
}
```

Status request (`TenantCountryStatusUpdateRequest`):

```json
{ "active": true }
```

Home request (`TenantCountryHomeUpdateRequest`):

```json
{ "homeCountry": true }
```

Response (`TenantCountryViewDto`) fields:

- `id`, `tenantCode`, `countryCode`, `countryName`
- `defaultCurrencyCode`, `defaultTimezone`, `homeCountry`, `active`
- `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

## 8) Feature Flag Catalog (new)

Endpoints:

- `POST /api/saas/feature-flags`
- `PUT /api/saas/feature-flags/{id}`
- `GET /api/saas/feature-flags/{id}`
- `GET /api/saas/feature-flags?q=&active=&page=&size=&sort=&all=`
- `PATCH /api/saas/feature-flags/{id}/status`
- `GET /api/saas/feature-flags/options?activeOnly=true`

Request (`FeatureFlagUpsertRequest`):

```json
{
  "featureKey": "attendance.geo_fencing",
  "featureName": "Attendance Geo Fencing",
  "description": "Enable geofence attendance checks",
  "active": true
}
```

Response (`FeatureFlagViewDto`) fields:

- `id`, `featureKey`, `featureName`, `description`, `active`
- `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

Options response (`FeatureFlagOptionViewDto`):

```json
{ "id": "uuid", "featureKey": "attendance.geo_fencing", "featureName": "Attendance Geo Fencing" }
```

## 9) Tenant Module/Feature toggles

Module toggle endpoints:

- `PUT /api/saas/tenants/{tenantCode}/modules/{moduleKey}` with `{ "enabled": true }`
- `GET /api/saas/tenants/{tenantCode}/modules`

Feature toggle endpoints:

- `PUT /api/saas/tenants/{tenantCode}/features/{featureKey}` with `{ "enabled": true }`
- `GET /api/saas/tenants/{tenantCode}/features`

Response item (`ToggleViewDto`):

```json
{ "key": "payroll", "enabled": true }
```

## 10) Tenant Settings and Audit Logs

Settings:

- `GET /api/saas/tenants/{tenantCode}/settings`

Response (`TenantSettingsViewDto`) fields:

- `tenant`, `subscription`, `branding`
- `localizationPreferences`
- `languages`, `countries`
- `defaultLanguageCode`, `homeCountryCode`
- `enabledModules`, `enabledFeatures`

Audit logs:

- `GET /api/saas/tenants/{tenantCode}/audit-logs?page=0&size=20`

Response: `PagedResult<AuditLogViewDto>`
