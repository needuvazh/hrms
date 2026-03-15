SYSTEM ROLE
You are a senior enterprise UI architect and frontend engineer.

STACK

Flutter
Enterprise admin UI


----------------------------------------------------

MODULE

Step 5 — Compliance-supporting HR Masters

Modules:

Visa Type  
Residence Status  
Labour Card Type  
Civil ID Type  
Passport Type  
Sponsor Type  
Work Permit Type  
Nationalisation Category  
Social Insurance Eligibility Type  
Beneficiary Type  
Dependent Type  

----------------------------------------------------

UI GOALS

Enterprise HRMS compliance configuration interface.

Supports:

employee immigration tracking  
passport and visa classification  
labour card classification  
civil ID tracking  
nationalisation segmentation  
PASI eligibility categorisation  
dependent management  
beneficiary designation  

These screens configure compliance data used by employee records.

----------------------------------------------------

SCREEN STRUCTURE

Each module must include:

List screen  
Create screen  
Edit screen  
View screen  
Status toggle  
Audit log placeholder  

----------------------------------------------------

MODULE UI DETAILS

Visa Type

Columns

Visa Type Code  
Visa Type Name  
Category  
Applies To  
Renewable  
Status  

Fields

Visa Type Code  
Visa Type Name  
Visa Category  
Applies To  
Renewable Flag  
Description  
Active  


Residence Status

Columns

Residence Status Code  
Residence Status Name  
Status  

Fields

Residence Status Code  
Residence Status Name  
Description  
Active  


Labour Card Type

Columns

Labour Card Type Code  
Labour Card Type Name  
Expiry Tracking  
Status  

Fields

Labour Card Type Code  
Labour Card Type Name  
Expiry Tracking Required  
Description  
Active  


Civil ID Type

Columns

Civil ID Type Code  
Civil ID Type Name  
Applies To  
Expiry Tracking  
Status  

Fields

Civil ID Type Code  
Civil ID Type Name  
Applies To  
Expiry Tracking Required  
Description  
Active  


Passport Type

Columns

Passport Type Code  
Passport Type Name  
Status  

Fields

Passport Type Code  
Passport Type Name  
Description  
Active  


Sponsor Type

Columns

Sponsor Type Code  
Sponsor Type Name  
Applies To  
Status  

Fields

Sponsor Type Code  
Sponsor Type Name  
Applies To  
Description  
Active  


Work Permit Type

Columns

Work Permit Type Code  
Work Permit Type Name  
Renewable  
Status  

Fields

Work Permit Type Code  
Work Permit Type Name  
Renewable Flag  
Description  
Active  


Nationalisation Category

Columns

Category Code  
Category Name  
Omani Flag  
Counts for Omanisation  
Status  

Fields

Category Code  
Category Name  
Omani Flag  
Counts for Omanisation Flag  
Description  
Active  


Social Insurance Eligibility Type

Columns

Eligibility Code  
Eligibility Name  
Pension Eligible  
Occupational Hazard  
Govt Contribution  
Status  

Fields

Eligibility Code  
Eligibility Name  
Pension Eligible Flag  
Occupational Hazard Eligible Flag  
Government Contribution Applicable Flag  
Description  
Active  


Beneficiary Type

Columns

Beneficiary Type Code  
Beneficiary Type Name  
Priority Order  
Status  

Fields

Beneficiary Type Code  
Beneficiary Type Name  
Priority Order  
Description  
Active  


Dependent Type

Columns

Dependent Type Code  
Dependent Type Name  
Insurance Eligible  
Family Visa Eligible  
Status  

Fields

Dependent Type Code  
Dependent Type Name  
Insurance Eligible Flag  
Family Visa Eligible Flag  
Description  
Active  


----------------------------------------------------

VALIDATIONS

Common

code required  
name required  
code uniqueness  
trim whitespace  
inline error display  
prevent duplicate submission  

Numeric validations

priority order >= 0  

Dropdown validations

applies_to must be valid enum  
category selection required when applicable  

----------------------------------------------------

API INTEGRATION

Endpoints

GET /api/compliance/{master}  
POST /api/compliance/{master}  
PUT /api/compliance/{master}/{id}  
PATCH /api/compliance/{master}/{id}/status  
GET /api/compliance/{master}/options  

----------------------------------------------------

OUTPUT FORMAT

Return answer in this order:

1 screen map  
2 reusable UI pattern  
3 page specifications  
4 form fields  
5 validation rules  
6 component structure  
7 folder structure  
8 state management  
9 api integration  
10 table configs  
11 modal vs page design  
12 sample JSON payloads  
13 edge cases  
14 reusable component suggestions  

Provide implementation-ready frontend specification.
