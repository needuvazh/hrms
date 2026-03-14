Phase 1 — Core enterprise SaaS HRMS masters

This phase is only about masters required to support HRMS, not payroll processing.

You should build them in this order.

Step 1 — Platform / SaaS masters

These make the product a real SaaS platform.

Modules

Tenant

Subscription Plan

Feature Flag

Tenant Branding / Localization Preferences

Why

These control who the customer is and what they are allowed to use.

Deliverables

screens

CRUD APIs

active/inactive

audit logs

tenant settings retrieval APIs

Step 2 — Global reference masters

These are reusable across the whole system.

Modules

Country

Currency

Language

Nationality

Religion

Gender

Marital Status

Relationship Type

Document Type

Education Level

Certification Type

Skill / Skill Category

Why

Employee profile, documents, dependents, onboarding, compliance, and alerts all depend on these.

Step 3 — Organization structure masters

These define the customer’s enterprise structure.

Modules

Legal Entity

Branch

Division

Department

Section

Location / Work Location

Cost Center

Business Unit

Reporting Unit

Why

Employees must belong somewhere in the organization.

Step 4 — Job architecture masters

These define enterprise position structure.

Modules

Designation / Job Title

Job Family

Job Function

Grade

Grade Band

Position

Employment Type

Worker Type

Employee Category

Employee Subcategory

Contract Type

Probation Policy

Notice Period Policy

Transfer Type

Promotion Type

Separation Reason

Why

Your Employee Information Management module needs position, categorisation, and lifecycle structure.

Step 5 — Compliance-supporting HR masters

These are HR-side compliance masters, still not payroll processing.

Modules

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

Why

This directly supports:

Omani national vs expatriate

PASI-linked categorisation

visa / permit tracking

beneficiary and dependent management

Step 6 — Document and policy masters

Needed for employee vault and onboarding.

Modules

Document Type

Document Category

Document Applicability Rule

Document Expiry Rule

Policy Document Type

Policy Acknowledgement Type

Attachment Category

Why

Needed for:

passport

visa

residence card

labour card

civil ID

education certificates

professional licences

onboarding document acknowledgement

Step 7 — Workflow and access masters

Even if Camunda handles process orchestration later, your HRMS still needs definitions and permissions.

Modules

Role

Permission

Role-Permission Mapping

Workflow Type

Approval Matrix

Notification Template

Service Request Type

Delegation Type

Why

Employee onboarding, profile approvals, document approvals, policy acknowledgement, and transfers all need access rules.

Step 8 — HR-only time and lifecycle support masters

Only keep HR-side tracking, not payroll processing.

Modules

Holiday Calendar

Leave Type

Shift

Attendance Source

Onboarding Task Type

Offboarding Task Type

Event Type

Employee Status

Employment Lifecycle Stage

Why

These are useful for employee administration even if payroll calculations are externalized.