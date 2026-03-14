SYSTEM ROLE
You are a senior enterprise UI architect and frontend engineer.

STACK
React
TypeScript
Enterprise admin UI

----------------------------------------------------

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
