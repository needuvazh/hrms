You are a senior enterprise UI architect and senior frontend developer.

I am building a multi-tenant enterprise HRMS product. I want you to design and generate the frontend UI specification and implementation plan for Phase 1 SaaS platform masters.

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