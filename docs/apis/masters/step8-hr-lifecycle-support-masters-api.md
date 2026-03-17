# Step 8 - HR Lifecycle Support Masters API

Implementation module: `hrms-modules/hrms-master-data`

Base path:

- `/api/hr-lifecycle`

Resources:

- `holiday-calendars`
- `leave-types`
- `shifts`
- `attendance-sources`
- `onboarding-task-types`
- `offboarding-task-types`
- `event-types`
- `employee-statuses`
- `employment-lifecycle-stages`

Common endpoints per resource:

- `POST /api/hr-lifecycle/{resource}`
- `PUT /api/hr-lifecycle/{resource}/{id}`
- `GET /api/hr-lifecycle/{resource}/{id}`
- `GET /api/hr-lifecycle/{resource}`
- `PATCH /api/hr-lifecycle/{resource}/{id}/status`
- `GET /api/hr-lifecycle/{resource}/options`

Common list query params:

- `q`, `active`, `limit`, `offset`, `sort`

Additional list filters:

- Holiday Calendar: `countryCode`, `calendarYear`, `calendarType`, `hijriEnabledFlag`, `weekendAdjustmentFlag`
- Leave Type: `leaveCategory`, `paidFlag`, `supportingDocumentRequiredFlag`, `genderApplicability`, `religionApplicability`, `nationalisationApplicability`
- Shift: `shiftType`, `overnightFlag`
- Attendance Source: `sourceType`, `trustedSourceFlag`, `manualOverrideFlag`
- Onboarding Task Type: `assigneeType`, `mandatoryFlag`, `taskCategory`
- Offboarding Task Type: `assigneeType`, `mandatoryFlag`, `taskCategory`
- Event Type: `eventGroup`
- Employee Status: `employmentActiveFlag`, `selfServiceAccessFlag`
- Employment Lifecycle Stage: `entryStageFlag`, `exitStageFlag`

`GET /api/hr-lifecycle/{resource}` response is paged and returns:

```json
{
  "items": [
    {
      "id": "uuid",
      "tenantId": "default",
      "code": "JOIN",
      "name": "Join",
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

Options query params:

- `q`, `limit`, `activeOnly` (default `true`)

Status payload:

```json
{ "active": true }
```
