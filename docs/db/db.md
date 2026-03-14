




Invite team members
You are a senior staff-level data architect and backend systems designer.

Design a **production-grade read database** for my **HRMS (Human Resource Management System)**. The database is intended primarily for **read-heavy workloads** and must support **employee dashboards, admin dashboards, attendance views, leave tracking, shift reports, payroll-related read views, audit/history views, compliance reporting, and downstream service/API consumption**.

The design must be suitable for a **real production environment** and should cover not only schema design, but also **security, auditing, traceability, performance, governance, resiliency, and operations**.

## HRMS project context

This HRMS includes features such as:
- multi-tenant / multi-client registration
- employee management
- admin and regular user roles
- attendance tracking
- biometric login such as face/thumb authentication
- shift management
- attendance punch types (in, out, break, overtime, etc.)
- leave management
- alerts and notifications
- admin dashboards
- attendance reports
- employee history and activity tracking

The read database should support use cases like:
- employee self-service profile view
- employee attendance calendar and monthly summary
- admin attendance dashboard
- shift-wise attendance reports
- late coming / early leaving reports
- absentee reports
- leave balance and leave history view
- employee movement/activity timeline
- audit trail views
- compliance and governance reports
- downstream APIs for UI and reporting systems

## Expected design goals

The design must be:
- production-ready
- secure
- highly auditable
- scalable
- resilient
- maintainable
- cost-aware
- optimized for read-heavy access
- suitable for future growth

## Please structure the response in the following sections

### 1. Requirement understanding
Identify and explain the functional and non-functional requirements for the HRMS read DB, including:
- key read use cases
- query patterns
- filtering, sorting, search, pagination
- aggregation/reporting needs
- dashboard needs
- latency expectations
- read throughput expectations
- scale assumptions
- data freshness requirements
- consistency requirements
- retention and archival expectations
- compliance requirements
- tenant isolation expectations

State clear assumptions where needed.

### 2. High-level architecture
Propose the best architecture for the HRMS read DB. Compare options such as:
- dedicated read replica
- CQRS read model
- denormalized reporting database
- search index for employee lookup/reporting
- analytical store for attendance/payroll reporting
- hybrid architecture

Explain:
- what should be the source of truth
- how data flows from write system to read DB
- whether to use CDC, events, batch sync, ETL, stream processing, or materialized views
- whether near-real-time or batch updates are better for each HRMS module
- recommended technologies and why

### 3. Domain-driven read model design
Design the read-side model for HRMS. Cover entities such as:
- tenant
- employee
- employee profile
- department
- designation
- reporting manager hierarchy
- role/access profile
- biometric identity reference
- shift definition
- employee shift assignment
- attendance event
- attendance daily summary
- attendance monthly summary
- leave request
- leave balance
- holiday calendar
- payroll read summary if relevant
- notification history
- audit event/history
- employee lifecycle history (joined, promoted, transferred, deactivated, etc.)

For each important entity, explain:
- why it exists in the read model
- whether it should be normalized or denormalized
- how it supports specific queries
- key fields
- relationships
- indexing needs

Provide sample schema examples.

### 4. Query-driven optimization
Explain how the read DB should be optimized for typical HRMS queries such as:
- find an employee by ID, email, code, phone, or biometric reference
- list employees by tenant, department, shift, status, or location
- get today’s attendance dashboard
- get monthly attendance summary for an employee
- list late arrivals, missing punches, absentees
- get leave balances and leave history
- retrieve shift allocation reports
- search employee activity/audit trail
- export compliance/audit reports

Cover:
- indexing strategy
- composite indexes
- covering indexes
- partitioning strategy
- precomputed summary tables
- materialized views
- caching strategy
- search optimization
- pagination strategy
- handling expensive reports
- anti-patterns to avoid

### 5. Data freshness and consistency
Describe:
- which data must be near-real-time
- which data can be delayed
- eventual consistency vs strong consistency tradeoffs
- handling replication lag
- stale-read tolerance
- idempotent ingestion
- reconciliation when source and read DB differ
- reprocessing/backfill strategy
- replay strategy for attendance or leave events
- how to handle corrections to attendance or shift data

### 6. Auditing and traceability
This section is mandatory and must be very detailed.

Design a complete auditing and traceability strategy for the HRMS read DB.

Cover:
- who changed what, when, why
- which user/admin/system performed the change
- how to track employee profile changes
- how to track attendance corrections
- how to track leave approval/rejection history
- how to track shift assignment changes
- how to track status changes like active/inactive/terminated
- how to track biometric enrollment/update/removal events
- how to correlate read records with source transactions/events
- source event ID, request ID, transaction ID, workflow ID correlation
- created_at, updated_at, deleted_at, version, changed_by, approved_by, change_reason fields
- lineage from source tables/services to read model tables
- immutable audit log design
- append-only audit table design
- support for forensic investigations
- debugging support for event processing failures
- reconstructing historical state at a given point in time
- compliance reporting support
- governance and internal audit readiness
- auditing for derived tables like daily attendance summary and monthly summary
- when to keep audit metadata in base tables and when to keep it in separate append-only history tables

Also explain how to design:
- employee_audit_history
- attendance_audit_history
- leave_audit_history
- shift_assignment_audit_history
- read_model_sync_audit
- pipeline_error_audit or dead-letter tracking

### 7. Security and access control
Design security for production HRMS reads:
- authentication
- authorization
- RBAC
- tenant isolation
- row-level security if needed
- column-level protection for sensitive fields
- encryption at rest and in transit
- secrets management
- masking of PII
- protection of biometric references
- least privilege access
- secure audit access
- admin-only visibility rules
- compliance-safe reporting access

### 8. Reliability and resiliency
Design for operational reliability:
- backups
- point-in-time recovery
- failover
- multi-AZ deployment
- replication
- disaster recovery
- RPO/RTO
- degraded mode behavior
- ingestion retry strategy
- dead-letter queue or failure capture
- how to recover missing attendance sync data
- how to safely rebuild read models

### 9. Observability and operations
Cover how to operate this HRMS read DB in production:
- application logs
- audit logs
- metrics
- tracing
- query latency monitoring
- slow query detection
- replication lag monitoring
- ingestion pipeline monitoring
- dead-letter monitoring
- data drift monitoring
- storage growth monitoring
- partition/index health monitoring
- alerting strategy
- operational dashboards
- runbooks for common failures

### 10. Data lifecycle and governance
Explain:
- retention of attendance records
- retention of leave history
- retention of audit logs
- archival of old employee records
- purge rules for deleted/deactivated users
- compliance considerations
- schema evolution strategy
- backward compatibility
- data ownership
- stewardship
- data validation
- data quality checks
- reconciliation jobs
- governance processes for HR and admin access

### 11. Scalability planning
Describe how the design will scale for:
- more tenants
- more employees
- more attendance events
- longer retention periods
- more dashboards and reports
- higher concurrent read traffic
- organization-wide exports
- future analytics use cases

Cover:
- horizontal scaling
- read replicas
- partitioning
- rebalancing
- archiving strategy
- caching
- search engine integration if needed
- analytical store integration if needed

### 12. Example schema and sample tables
Provide concrete sample designs for:
- employee_read_model
- attendance_event_read
- attendance_daily_summary
- attendance_monthly_summary
- leave_balance_read
- shift_assignment_read
- employee_audit_history
- attendance_audit_history
- read_model_sync_audit

Include:
- important columns
- primary keys
- foreign keys if relevant
- indexes
- partition keys
- audit fields

### 13. Example read flows
Provide example end-to-end flows for:
- new employee created
- employee profile updated
- attendance punch captured
- attendance corrected by admin
- leave request approved
- shift reassigned
- employee deactivated

For each flow, explain:
- source write action
- event or CDC propagation
- read model update
- audit generation
- reconciliation considerations

### 14. Tradeoffs and decision log
List the major tradeoffs, such as:
- normalized vs denormalized read model
- single read DB vs hybrid read architecture
- strong consistency vs eventual consistency
- real-time summaries vs batch summaries
- embedded audit fields vs separate history tables
- relational DB vs search engine vs analytical store
- cost vs performance
- simplicity vs scalability

Explain why each decision is recommended for an HRMS system.

### 15. Final recommendation
End with:
- the best recommended architecture for this HRMS read DB
- the recommended schema strategy
- the recommended audit strategy
- the recommended security controls
- the recommended operational controls
- top risks
- top mitigations
- phased implementation plan for MVP, scale-up, and enterprise-grade maturity

## Important expectations

Do not give generic theory only.
Give concrete and practical recommendations.
Use production-grade thinking.
Focus strongly on read-heavy HRMS use cases.
Make auditing and traceability first-class concerns.
Where useful, provide sample table structures, field lists, and index examples.
Explain how to support compliance, investigations, and historical reconstruction.  I would like to design the db for my requirement you can refer HRMS_Payroll_ESS_Blueprint_Oman and HRMS_Payroll_ESS_Proposal_AlSawari_Nexura as the source and refer other document for references


Thought for 30s
Below is a production-grade design for your HRMS read database, grounded in the Oman-focused blueprint and proposal you shared. I’m using the blueprint as the primary architecture source because it is newer and explicitly defines the target platform, compliance posture, tech stack, modules, and operating model; the proposal and Personnex references help validate functional scope, dashboards, reporting, security, and audit expectations. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…

 
Personnex Vs Mena HRMS

 
Personnex HR Suite 2024


1. Requirement understanding
Functional requirements
Your read side must serve these major workloads:

Employee self-service views: profile, documents, leave balances/history, payslip summaries, attendance calendar, monthly attendance summary, requests, alerts. The blueprint explicitly calls out employee dashboards with pay summary, leave balance, upcoming events, announcements, and live leave-aware transactions. 
HRMS_Payroll_ESS_Blueprint_Oman


Manager/admin operational dashboards: team attendance, leave plan, overtime, open requests, headcount, compliance cockpit, payroll/WPS status, Omanisation dashboard, absenteeism, late arrivals. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Attendance reporting: daily attendance, monthly summary, missing punches, late/early, OT, no-shift, who-is-in, raw punch review, mismatch correction. This is strongly supported by Personnex product coverage and Time Attendance features. 
Personnex HR Suite 2024

 
Personnex Vs Mena HRMS


Leave views: balances, accruals, request history, approvals, resumption/overstay, encashment, team leave calendar, compliance with Oman Labour Law. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Payroll-related read views: salary summaries, variance views, WPS readiness, PASI contribution views, EOSB summaries, payroll audit views, bank/WPS tracking. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Audit/history views: employee history, approvals, corrections, login/activity, workflow state changes, document expiry/audit, full transaction trail. Both the blueprint and proposal require complete audit logs and exportable system audit views. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Compliance/governance views: WPS, PASI, Omanisation, document expiry, statutory leave correctness, payroll reconciliation, legal/audit evidence. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Query patterns
Typical query patterns will be:

point lookups by employee_id / employee_code / email / mobile / civil_id / passport / biometric_ref

filtered lists by tenant, branch, department, designation, grade, nationality, status, manager, shift, date range

time-series and calendar views by employee/day/month

exception and alert queries: late arrivals, absentees, missing punch, expiring documents, compliance breaches

aggregate dashboards by tenant / branch / department / category / day / month

full-history timelines and audit searches by employee, module, workflow, request_id, source_event_id

Non-functional requirements
Assumptions for a production design:

Tenants: 10 to 500+ organizations

Employees: 100 to 50,000 per tenant; total platform scale can exceed 1M employees

Attendance events: 2 to 12 punches per employee/day; this becomes the largest fact stream

Latency:

profile and dashboard widgets: p95 under 150 ms

employee lists/search: p95 under 300 ms

attendance calendar/month views: p95 under 500 ms

large exports/reports: async generation

Freshness:

profile / leave / shift / attendance exceptions: near real time, 5–60 seconds

payroll summaries / WPS readiness / PASI views: near real time during payroll windows, otherwise 1–5 minutes

monthly analytics and compliance packs: batch acceptable, 15 min to overnight depending on size

Consistency:

ESS and dashboards can tolerate eventual consistency

payroll approval, WPS, PASI, EOSB, final settlement, and audit evidence need deterministic versioned reads

Retention:

attendance and payroll history: 7–10 years minimum

audit logs and WPS archives: 10 years or more, matching the blueprint’s tamper-proof WPS archive and long retention expectations. 
HRMS_Payroll_ESS_Blueprint_Oman


Tenant isolation: hard isolation in the data plane, not just in the app layer

2. High-level architecture
Best-fit architecture
For your HRMS, the best design is a hybrid read architecture:

Primary operational read store
PostgreSQL read model database for most API/UI reads.

Search index
Elasticsearch/OpenSearch for employee lookup, document search, and audit search. The blueprint explicitly recommends Elasticsearch for employee search, document search, and audit log queries. 
HRMS_Payroll_ESS_Blueprint_Oman


Analytical/reporting store
A reporting warehouse or columnar store for heavy compliance exports, payroll analytics, monthly attendance analytics, and organization-wide historical reports.

This hybrid approach aligns with the blueprint’s stack: PostgreSQL + Redis + Elasticsearch + reporting/BI service + APIs. 
HRMS_Payroll_ESS_Blueprint_Oman


What should be the source of truth
The write-side domain services remain the source of truth:

employee-service

leave-service

attendance-service

payroll-service

compliance-service

integration-service

The blueprint explicitly decomposes the platform into these domain services. 
HRMS_Payroll_ESS_Blueprint_Oman


Recommended data flow
Use:

CDC from write DBs for authoritative row changes

domain events from services for workflow and business semantics

stream processing / projection builders to populate read models

batch recomputation for summaries and reconciliation

Recommended pattern by module
Employee master / org / role / shift assignment / leave requests
CDC + domain events, near real time

Attendance punches
event streaming from biometric/mobile integrations + CDC on corrected transactions

Attendance daily summary
incremental projector + nightly reconciliation recompute

Attendance monthly summary
batch/incremental hybrid

Payroll read summaries / WPS / PASI
event + batch finalize; version every payroll run

Audit and compliance evidence
append-only event log + immutable history tables

Why not only a read replica
A plain read replica is not enough because:

read access patterns are query-shaped, not write-schema-shaped

dashboards need denormalized summaries

attendance and audit need different partitioning/indexing than OLTP

historical reconstruction and forensic traceability need append-only models

full-text audit search needs a search index

Recommended technologies
Operational read DB: PostgreSQL 16

Cache: Redis

Search: Elasticsearch/OpenSearch

Streaming: Kafka

CDC: Debezium or native logical replication + outbox

Analytical store: ClickHouse, BigQuery, Snowflake, or Redshift depending hosting strategy

Object store: S3-compatible encrypted archive for exported reports, WPS archives, audit evidence

PostgreSQL, Redis, Elasticsearch, Kafka/RabbitMQ, and cloud-native deployment are all consistent with the blueprint/proposal. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


3. Domain-driven read model design
Design principle
The read side should be domain-oriented but query-shaped:

keep reference/master data normalized

keep high-traffic read models denormalized

keep history immutable

keep summaries precomputed

keep tenant_id everywhere

Core entities
tenant
Why: mandatory for multi-tenant isolation, quotas, compliance partitioning
Model: normalized
Fields:

tenant_id

tenant_code

legal_name

region

timezone

currency

status

compliance_profile_version

employee_read_model
Why: primary ESS/admin lookup model
Model: denormalized snapshot for fast reads
Supports:

profile page

employee list/search

manager dashboard

compliance profile views

Key fields:

tenant_id, employee_id, employee_code

full_name_en, full_name_ar

email, mobile

status

employment_type

nationality_type (omani/expat)

branch_id, department_id, designation_id, grade_id

manager_employee_id, org_path

hire_date, probation_end_date

civil_id_masked, passport_no_masked

biometric_ref_hash

latest_shift_id

latest_leave_balance_snapshot_json

latest_attendance_status

row_version

source_last_event_id

created_at, updated_at

Indexing:

unique (tenant_id, employee_id)

unique (tenant_id, employee_code)

unique nullable (tenant_id, email)

btree on (tenant_id, department_id, status)

btree on (tenant_id, manager_employee_id)

gin/trgm for multilingual name search if not using search engine

department / designation / grade / branch / location
Why: shared filters and dashboards
Model: normalized dimensions
Index: (tenant_id, code), (tenant_id, active_flag)

reporting_manager_hierarchy
Why: team dashboards and row-level authorization
Model: denormalized hierarchy closure table
Columns:

tenant_id

ancestor_employee_id

descendant_employee_id

depth

active_from, active_to

role_access_profile_read
Why: read authorization evaluation, admin visibility, secure dashboards
Model: normalized with materialized permission expansion

biometric_identity_reference
Why: attendance lookup without exposing raw biometric data
Model: normalized, protected
Fields:

tenant_id

biometric_identity_id

employee_id

provider_type

external_biometric_ref_hash

enrollment_status

enrollment_version

enrolled_at

revoked_at

Never store raw templates in the read DB.

shift_definition
Why: reporting, attendance enrichment
Fields:

shift_id, shift_code, shift_type, start_time, end_time

grace_in_min, grace_out_min

overtime_rule_id

night_shift_flag

roster_flag

shift_assignment_read
Why: shift-wise dashboards, late/early calculations, history
Model: slowly-changing assignment with current pointer
Fields:

assignment_id

tenant_id

employee_id

shift_id

effective_from, effective_to

assignment_source

roster_id

approved_by

row_version

Indexes:

(tenant_id, employee_id, effective_from desc)

(tenant_id, shift_id, effective_from)

attendance_event_read
Why: raw punch investigation and audit
Model: append-only fact table
Fields:

tenant_id

attendance_event_id

employee_id

event_ts

event_local_date

event_type (in/out/break_in/break_out/ot_in/ot_out/manual_correction)

source_type (biometric/mobile/manual/import/api)

source_device_id

biometric_ref_hash

geo_lat, geo_long, geo_accuracy

photo_ref

source_event_id

request_id

correlation_id

ingestion_run_id

is_corrected

correction_of_event_id

created_at

Partition by event_local_date monthly or daily depending volume.

attendance_daily_summary
Why: most common operational attendance query
Model: fully denormalized per employee/day
Fields:

tenant_id

attendance_date

employee_id

shift_id

scheduled_in, scheduled_out

actual_first_in, actual_last_out

worked_minutes

late_minutes

early_out_minutes

overtime_minutes

break_minutes

attendance_status

missing_punch_flag

absent_flag

on_leave_flag

holiday_flag

offday_flag

anomaly_flags jsonb

derived_from_version

source_window_start_ts

source_window_end_ts

last_recomputed_at

Indexes:

pk (tenant_id, attendance_date, employee_id)

(tenant_id, attendance_date, department_id, attendance_status)

partial index where late_minutes > 0

partial index where missing_punch_flag = true

attendance_monthly_summary
Why: employee monthly calendar, payroll feed view, admin summary
Model: precomputed summary per employee/month
Fields:

tenant_id

month_key

employee_id

present_days

absent_days

leave_days

late_count

early_out_count

total_worked_minutes

total_ot_minutes

payable_ot_minutes

unpaid_leave_days

derived_version

last_recomputed_at

leave_request_read
Why: request tracking, manager queues, history
Fields:

tenant_id

leave_request_id

employee_id

leave_type_id

start_date, end_date

requested_days

approved_days

resumption_date

status

approval_level_current

submitted_at, approved_at, rejected_at, cancelled_at

approved_by, rejected_by

workflow_id

change_reason

source_event_id

row_version

leave_balance_read
Why: ESS and manager dashboards
Fields:

tenant_id

employee_id

leave_type_id

entitlement_days

accrued_days

consumed_days

reserved_days

balance_days

carry_forward_days

encashable_days

as_of_date

calc_version

Use snapshot + balance ledger.

holiday_calendar
Why: attendance/leave derivation
Fields:

tenant_id

calendar_date

holiday_code

holiday_name

holiday_type

is_national

is_islamic

applicable_branch_id nullable

payroll_read_summary
Why: read-only payroll views, cost dashboards, employee pay summary
Fields:

tenant_id

payroll_run_id

employee_id

payroll_month

payroll_group

gross_pay

net_pay

total_deductions

PASI_employee

PASI_employer

EOSB_provision

WPS_status

bank_transfer_status

finalized_flag

finalized_at

payroll_version

notification_history_read
Why: alert traceability and support
Fields:

notification_id

tenant_id

target_employee_id

channel

notification_type

subject

template_version

sent_at

delivery_status

related_entity_type / id

correlation_id

audit_event_history
Why: universal immutable audit evidence
Model: append-only
Fields:

audit_event_id

tenant_id

entity_type

entity_id

entity_version

action_type

changed_fields jsonb

old_values jsonb

new_values jsonb

changed_by_actor_type

changed_by_actor_id

approved_by_actor_id

change_reason

source_service

source_event_id

request_id

transaction_id

workflow_id

correlation_id

occurred_at

ingested_at

legal_hold_flag

employee_lifecycle_history
Why: joined/promoted/transferred/deactivated timelines
Fields:

lifecycle_event_id

tenant_id

employee_id

lifecycle_type

effective_at

previous_org_ref jsonb

new_org_ref jsonb

reason_code

initiated_by

approved_by

source_event_id

4. Query-driven optimization
Indexing strategy
Employee lookup
For:

employee_id

employee_code

email

phone

biometric reference

Use:

unique btree on exact identifiers

hash or btree on normalized phone/email

separate secure lookup table for biometric_ref_hash

List employees by tenant/department/shift/status/location
Use:

(tenant_id, status, department_id)

(tenant_id, latest_shift_id, status)

(tenant_id, branch_id, department_id, designation_id)

INCLUDE columns for frequent list fields to support covering indexes

Today attendance dashboard
Best pattern:

attendance_daily_summary current-day rows

index on (tenant_id, attendance_date, department_id)

partial indexes for absentees, latecomers, missing punches

Monthly attendance summary
Never compute from raw punches on request at scale.
Use attendance_monthly_summary + employee dimension.

Audit trail search
Use:

PostgreSQL partitioned append-only table

plus Elasticsearch/OpenSearch index on audit payload for keyword/time/entity/actor searches

Partitioning strategy
attendance_event_read: partition by month on event_local_date

attendance_daily_summary: partition by month on attendance_date

audit_event_history: partition by month or quarter on occurred_at

payroll_read_summary: partition by payroll_month

notification_history: partition by month

Precomputed summary tables
Mandatory:

attendance_daily_summary

attendance_monthly_summary

leave_balance_read

payroll_read_summary

compliance_daily_snapshot

organization_headcount_snapshot

Materialized views
Use for:

manager team aggregates

monthly department absenteeism

payroll variance by month

Omanisation percentages by org unit

Refresh incrementally where possible.

Caching
Use Redis for:

employee mini-profile

dashboard widget payloads

leave balance snapshot

org hierarchy

manager team list

Never cache immutable audit evidence as the only source.

Pagination
use keyset pagination for large employee/audit lists

avoid offset pagination for high page numbers

expose sort keys like (employee_code, employee_id) or (occurred_at desc, audit_event_id desc)

Handling expensive reports
run asynchronously

produce downloadable CSV/PDF into object storage

store report execution metadata and checksum

allow rerun by date/version

Anti-patterns to avoid
joining raw attendance events for every dashboard request

storing only current state without history

relying only on read replicas

mixing mutable summary rows and immutable audit rows in the same table

storing biometric raw payloads in reporting stores

offset-based pagination on audit tables

tenant filtering only in application code

5. Data freshness and consistency
Near-real-time data
Needs 5–60 second freshness:

employee profile changes

shift assignment changes

attendance punches and attendance exceptions

leave request status

alerts/notifications

compliance cockpit indicators

ESS profile/leave/attendance views

Can be slightly delayed
monthly summaries

payroll cost analytics

organization-wide exports

attrition/forecast analytics

historical compliance packs

Eventual vs strong consistency
Use eventual consistency for most read views.
Use versioned deterministic consistency for payroll/compliance outputs:

payroll_run_id + payroll_version

WPS_file_id + generated_version

PASI_submission_id + contribution_version

The user sees finalized versions, not “latest maybe changing” views.

Replication lag handling
expose data_as_of timestamp in UI/API

if lag breaches threshold, show freshness banner

payroll and compliance APIs should reject non-finalized snapshots for statutory operations

Idempotent ingestion
Every projector should key idempotency on:

source_event_id

source_service

entity_id + version

Reconciliation
Run:

daily employee/profile parity checks

hourly attendance parity for same-day punches

leave balance recomputation checks

payroll finalization reconciliation against write-side totals

WPS/PASI confirmation matching

Reprocessing/backfill
Keep:

immutable source event store or CDC archive

replayable projector code

versioned projector definitions

For corrections:

replay employee/attendance/leave/payroll streams by tenant, module, date range

Attendance or shift correction handling
Do not update history in place without trace.

Pattern:

original punch remains

correction event appended

affected daily summary invalidated

summary recomputed with new derivation version

audit records linked to both original and corrected records

6. Auditing and traceability
This is the most important part of your design.

Core principle
Use two layers:

Audit metadata in base read tables

Separate immutable append-only history tables

Keep in base tables
Use for current-state debugging and optimistic reads:

created_at

updated_at

deleted_at

row_version

source_last_event_id

source_last_event_at

last_modified_by

approval_state

approved_by

change_reason_latest

Keep in append-only history
Use for evidence, forensic reconstruction, compliance:

every field delta

old/new values

actor

approval chain

workflow step changes

source request and transaction correlation

replay lineage

derivation lineage for summaries

Track “who changed what, when, why”
Every audit event should capture:

who: user/admin/system/integration/batch job

what: entity type + entity id + changed fields

when: business effective time and technical ingestion time

why: reason code + free-text reason if required

where from: service/module/device/api/import job

under which process: request_id / transaction_id / workflow_id / correlation_id

Mandatory audit columns
Across current-state rows:

created_at

updated_at

deleted_at

version

changed_by

approved_by

change_reason

source_event_id

request_id

transaction_id

workflow_id

correlation_id

Lineage design
Every read row should be traceable to:

source service

source table or domain aggregate

source primary key

source event id

source aggregate version

projector version

sync batch or stream partition/offset

recomputation job id if derived

Immutable audit log design
employee_audit_history
Purpose:

profile changes

org changes

salary-visible attribute changes if relevant

status changes

document-related identity changes

manager changes

Columns:

audit_id

tenant_id

employee_id

audit_type

effective_at

old_snapshot jsonb

new_snapshot jsonb

changed_fields text[]

changed_by_actor_id

approved_by_actor_id

change_reason

workflow_id

source_event_id

request_id

transaction_id

correlation_id

source_service

entity_version

ingested_at

attendance_audit_history
Purpose:

punch corrections

mismatch resolution

manual attendance edits

derived-status changes

Columns:

audit_id

tenant_id

employee_id

attendance_date

attendance_event_id nullable

summary_row_key nullable

action_type

old_values jsonb

new_values jsonb

correction_reason

corrected_by

approved_by

source_event_id

correlation_id

derivation_version_before

derivation_version_after

occurred_at

ingested_at

leave_audit_history
Purpose:

request submit / approve / reject / cancel / resume / encash / adjust
Columns:

audit_id

tenant_id

leave_request_id

employee_id

leave_type_id

action_type

status_from

status_to

days_before

days_after

balance_before

balance_after

acted_by

approved_by

workflow_step

change_reason

source_event_id

request_id

workflow_id

occurred_at

shift_assignment_audit_history
Purpose:

assignments, temporary shifts, roster changes, backdated changes
Columns:

audit_id

tenant_id

employee_id

shift_assignment_id

old_shift_id

new_shift_id

effective_from

effective_to

reason_code

changed_by

approved_by

roster_id

source_event_id

workflow_id

occurred_at

read_model_sync_audit
Purpose:

pipeline observability and lineage
Columns:

sync_audit_id

tenant_id

projector_name

source_service

source_stream

source_event_id

source_offset

entity_type

entity_id

target_table

target_pk

projection_action

projector_version

sync_status

sync_started_at

sync_completed_at

error_code

error_payload

pipeline_error_audit / dead_letter_tracking
Purpose:

failures during projection, enrichment, lookup, schema mismatch
Columns:

dlq_id

tenant_id

source_service

source_event_id

entity_type

entity_id

projector_name

failure_stage

error_class

error_message

retry_count

next_retry_at

payload_ref

first_failed_at

last_failed_at

resolved_at

resolved_by

Support for forensic investigations
You need to answer questions like:

who changed employee X’s department on March 4

why was attendance on March 7 corrected

which admin approved a leave reversal

which version of the attendance rules produced this monthly summary

what did the employee record look like before termination

did the WPS-ready amount match finalized payroll at that time

That means:

append-only history

versioned summaries

request/workflow correlation

legal-hold support

immutable exportable audit archives

Historical state reconstruction
There are two valid methods:

Snapshot + delta replay
current snapshot plus append-only history

Period-valid temporal tables
valid_from / valid_to rows for key dimensions

Recommended:

use temporal validity for employee org/role/shift status

use append-only facts for audit and attendance

Auditing derived tables
Derived tables like attendance_daily_summary and attendance_monthly_summary must also carry auditability:

derived_from_event_count

derivation_version

rule_set_version

computed_at

recomputed_reason

recomputed_by_job_id

source_range_hash

If a summary changes, record it in summary audit history.

7. Security and access control
The blueprint and proposal already call for OAuth/SAML, MFA, RBAC, row-level security, encryption, and complete audit trails. Build the read DB around that. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Authentication
SSO via OAuth 2.0 / SAML / LDAP / AD

MFA for admin, HR, payroll, manager

short-lived access tokens, service identities for backend readers

Authorization
Use layered authorization:

RBAC for broad function access

ABAC for context-aware access

RLS in PostgreSQL for tenant and employee/team scoping

Example:

employee sees own rows

manager sees team subtree

HR sees tenant HR domains

payroll sees payroll schemas/views only

compliance auditor gets read-only evidence views

support staff gets masked views only

Tenant isolation
Minimum:

tenant_id on every table

RLS policies per tenant

per-tenant encryption context where possible

For larger enterprise tenants, consider:

separate schema per tenant for operational reads

shared analytical store with tenant partitioning

Column-level protection
Protect:

salary fields

bank account/IBAN

civil ID

passport

personal contact

biometric references

Methods:

field-level encryption

masked secure views

dynamic data masking

separate lookup tables for sensitive identifiers

Biometric protection
never store raw biometric templates in read DB

store only hashed/tokenized reference

segregate biometric identity map from attendance analytics

restricted admin-only access with dedicated audit trail

Encryption
AES-256 at rest

TLS 1.3 in transit

KMS/HSM-managed keys

envelope encryption for sensitive fields

Least privilege
separate DB roles for API, reporting, audit, support, ETL, admin

no direct table access for UI users

audited privileged access workflow

8. Reliability and resiliency
The blueprint/proposal already target 99.9% uptime, active-passive failover, RPO 4h, RTO 2h, and long-retention backups. Use those as baseline operational targets. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Core controls
multi-AZ PostgreSQL

one or more read replicas

PITR enabled

daily incremental + weekly full backups

encrypted off-site backups

DR region copy for critical payroll/WPS data

RPO/RTO
Recommended:

operational read DB: RPO 15–60 min, RTO 30–60 min

critical payroll/compliance data: align to RPO 4h / RTO 2h at minimum, preferably better for SaaS

Ingestion retry strategy
exponential backoff

idempotent retries

poison message threshold

DLQ with operator tooling

Recover missing attendance sync data
detect holes by device/employee/time window

request replay from source integration

reproject affected days only

mark corrected windows in sync audit

Rebuild read models safely
rebuild per tenant/module/date range

verify via checksums and row counts

shadow build new projection version

cut over atomically

9. Observability and operations
Logs
application logs

ingestion/projection logs

query access logs

audit access logs

privileged access logs

Metrics
query p50/p95/p99 by endpoint/table

cache hit ratio

projector lag

CDC lag

DLQ size

reconciliation failure counts

table growth / partition growth

index bloat

replica lag

Tracing
Use OpenTelemetry:

trace from UI/API request

through read API

to cache/DB/search

correlate with source request_id and workflow_id

Monitoring
Mandatory dashboards:

attendance ingestion health

read model freshness

payroll projection lag

audit ingest success rate

WPS/PASI export readiness

slow query dashboard

partition health dashboard

Alerting
Trigger on:

stale data beyond SLA

replication lag

failed payroll summary projection

audit projector failures

DLQ growth

storage nearing threshold

backup failures

unusual query spikes/exfil patterns

Runbooks
Need written runbooks for:

stale attendance today

leave balances out of sync

payroll summary mismatch

read replica lag

corrupted projector deployment

audit export failure

tenant-specific performance degradation

10. Data lifecycle and governance
Retention
Recommended:

raw attendance events: 7–10 years hot/warm, then archive

daily/monthly summaries: retain full period

leave history: employment term + 7 years minimum

payroll/WPS/PASI/EOSB: 10 years

audit logs: 10 years minimum, longer with legal hold

Archival
cold storage in object store for old partitions

keep summarized tables online longer than raw events

archive terminated employee detail after grace period but preserve audit/legal records

Purge rules
deactivated/deleted users should be soft-deleted in operational reads

hard purge only after legal/compliance approval

preserve pseudonymized audit references where required

Schema evolution
backward-compatible events

additive columns first

versioned projectors

blue/green projection rollout

contract tests for downstream APIs

Data ownership
HR owns employee/leave master correctness

Payroll owns payroll/WPS/PASI correctness

IT/platform owns pipeline and DB health

Compliance/audit owns legal-hold and evidence policy

Data quality checks
null/format checks

duplicate employee code/email/biometric ref checks

balance integrity

payroll amount parity

shift assignment overlap checks

orphan audit or orphan summary detection

11. Scalability planning
How it scales
More tenants
tenant-aware partitioning

shard analytically if very large

separate heavy tenants if needed

More employees / attendance events
partition raw facts by month

compress/archive old partitions

precompute daily/monthly summaries

route searches to search index

More dashboards and reports
materialized aggregates

Redis caching

async report generation

analytical store offload

Higher concurrent read traffic
read replicas

connection pooling

API caching

denormalized query models

search offload

Future analytics
stream facts to warehouse

keep dimensional conformance with operational read model

use CDC/event streams as shared data contract

12. Example schema and sample tables
employee_read_model
create table employee_read_model (
  tenant_id uuid not null,
  employee_id uuid not null,
  employee_code varchar(50) not null,
  full_name_en varchar(200) not null,
  full_name_ar varchar(200),
  email varchar(200),
  mobile varchar(30),
  status varchar(30) not null,
  employment_type varchar(30),
  nationality_type varchar(20) not null,
  branch_id uuid,
  department_id uuid,
  designation_id uuid,
  grade_id uuid,
  manager_employee_id uuid,
  org_path ltree,
  hire_date date,
  probation_end_date date,
  latest_shift_id uuid,
  latest_attendance_status varchar(30),
  biometric_ref_hash varchar(128),
  row_version bigint not null,
  source_last_event_id varchar(100) not null,
  created_at timestamptz not null,
  updated_at timestamptz not null,
  deleted_at timestamptz,
  changed_by varchar(100),
  approved_by varchar(100),
  change_reason text,
  primary key (tenant_id, employee_id),
  unique (tenant_id, employee_code)
);
create index idx_emp_tenant_dept_status on employee_read_model (tenant_id, department_id, status);
create index idx_emp_tenant_manager on employee_read_model (tenant_id, manager_employee_id);
attendance_event_read
create table attendance_event_read (
  tenant_id uuid not null,
  attendance_event_id uuid not null,
  employee_id uuid not null,
  event_ts timestamptz not null,
  event_local_date date not null,
  event_type varchar(30) not null,
  source_type varchar(30) not null,
  source_device_id varchar(100),
  biometric_ref_hash varchar(128),
  source_event_id varchar(100) not null,
  request_id varchar(100),
  correlation_id varchar(100),
  is_corrected boolean not null default false,
  correction_of_event_id uuid,
  created_at timestamptz not null,
  primary key (tenant_id, attendance_event_id)
) partition by range (event_local_date);
attendance_daily_summary
create table attendance_daily_summary (
  tenant_id uuid not null,
  attendance_date date not null,
  employee_id uuid not null,
  department_id uuid,
  shift_id uuid,
  scheduled_in timestamptz,
  scheduled_out timestamptz,
  actual_first_in timestamptz,
  actual_last_out timestamptz,
  worked_minutes int not null default 0,
  late_minutes int not null default 0,
  early_out_minutes int not null default 0,
  overtime_minutes int not null default 0,
  attendance_status varchar(30) not null,
  missing_punch_flag boolean not null default false,
  absent_flag boolean not null default false,
  on_leave_flag boolean not null default false,
  holiday_flag boolean not null default false,
  anomaly_flags jsonb,
  derivation_version int not null,
  source_window_start_ts timestamptz,
  source_window_end_ts timestamptz,
  last_recomputed_at timestamptz not null,
  created_at timestamptz not null,
  updated_at timestamptz not null,
  primary key (tenant_id, attendance_date, employee_id)
) partition by range (attendance_date);
create index idx_ads_tenant_date_dept on attendance_daily_summary (tenant_id, attendance_date, department_id);
create index idx_ads_late on attendance_daily_summary (tenant_id, attendance_date) where late_minutes > 0;
create index idx_ads_missing on attendance_daily_summary (tenant_id, attendance_date) where missing_punch_flag = true;
attendance_monthly_summary
create table attendance_monthly_summary (
  tenant_id uuid not null,
  month_key date not null,
  employee_id uuid not null,
  present_days numeric(5,2) not null default 0,
  absent_days numeric(5,2) not null default 0,
  leave_days numeric(5,2) not null default 0,
  late_count int not null default 0,
  total_worked_minutes int not null default 0,
  total_ot_minutes int not null default 0,
  unpaid_leave_days numeric(5,2) not null default 0,
  derivation_version int not null,
  last_recomputed_at timestamptz not null,
  primary key (tenant_id, month_key, employee_id)
);
leave_balance_read
create table leave_balance_read (
  tenant_id uuid not null,
  employee_id uuid not null,
  leave_type_id uuid not null,
  entitlement_days numeric(8,2) not null,
  accrued_days numeric(8,2) not null,
  consumed_days numeric(8,2) not null,
  reserved_days numeric(8,2) not null,
  carry_forward_days numeric(8,2) not null,
  balance_days numeric(8,2) not null,
  encashable_days numeric(8,2) not null,
  as_of_date date not null,
  calc_version int not null,
  updated_at timestamptz not null,
  primary key (tenant_id, employee_id, leave_type_id)
);
shift_assignment_read
create table shift_assignment_read (
  tenant_id uuid not null,
  shift_assignment_id uuid not null,
  employee_id uuid not null,
  shift_id uuid not null,
  effective_from date not null,
  effective_to date,
  roster_id uuid,
  assignment_source varchar(30) not null,
  row_version bigint not null,
  approved_by varchar(100),
  created_at timestamptz not null,
  updated_at timestamptz not null,
  primary key (tenant_id, shift_assignment_id)
);
create index idx_sar_emp_effective on shift_assignment_read (tenant_id, employee_id, effective_from desc);
employee_audit_history
create table employee_audit_history (
  audit_id uuid primary key,
  tenant_id uuid not null,
  employee_id uuid not null,
  audit_type varchar(50) not null,
  entity_version bigint not null,
  old_snapshot jsonb,
  new_snapshot jsonb,
  changed_fields text[],
  changed_by_actor_id varchar(100) not null,
  approved_by_actor_id varchar(100),
  change_reason text,
  source_service varchar(50) not null,
  source_event_id varchar(100) not null,
  request_id varchar(100),
  transaction_id varchar(100),
  workflow_id varchar(100),
  correlation_id varchar(100),
  effective_at timestamptz not null,
  ingested_at timestamptz not null
) partition by range (effective_at);
attendance_audit_history
create table attendance_audit_history (
  audit_id uuid primary key,
  tenant_id uuid not null,
  employee_id uuid not null,
  attendance_date date not null,
  attendance_event_id uuid,
  summary_row_key varchar(200),
  action_type varchar(50) not null,
  old_values jsonb,
  new_values jsonb,
  correction_reason text,
  corrected_by varchar(100),
  approved_by varchar(100),
  source_event_id varchar(100) not null,
  correlation_id varchar(100),
  derivation_version_before int,
  derivation_version_after int,
  occurred_at timestamptz not null,
  ingested_at timestamptz not null
) partition by range (occurred_at);
read_model_sync_audit
create table read_model_sync_audit (
  sync_audit_id uuid primary key,
  tenant_id uuid,
  projector_name varchar(100) not null,
  source_service varchar(50) not null,
  source_stream varchar(100) not null,
  source_event_id varchar(100) not null,
  source_offset varchar(100),
  entity_type varchar(50) not null,
  entity_id varchar(100) not null,
  target_table varchar(100) not null,
  target_pk varchar(200) not null,
  projection_action varchar(30) not null,
  projector_version varchar(30) not null,
  sync_status varchar(20) not null,
  sync_started_at timestamptz not null,
  sync_completed_at timestamptz,
  error_code varchar(50),
  error_payload jsonb
);
create index idx_rmsa_source_event on read_model_sync_audit (source_service, source_event_id);
13. Example read flows
New employee created
write-side employee-service saves master data

emits EmployeeCreated

read projector creates employee_read_model

org hierarchy projector updates manager closure

audit projector writes employee_audit_history

reconciliation checks employee count parity

Employee profile updated
write-side update occurs with request_id/workflow_id

CDC/event emitted with changed fields

read snapshot updated in employee_read_model

append delta to employee_audit_history

if department/manager changed, update hierarchy and lifecycle history

Attendance punch captured
biometric/mobile integration emits punch event

stored in attendance_event_read

daily projector recalculates that employee/date row in attendance_daily_summary

anomalies indexed for dashboard/search

sync audit records projector success

Attendance corrected by admin
correction request approved

original event retained

correction event appended

affected daily and monthly summaries recomputed

attendance_audit_history records before/after

reconciliation verifies payroll-impact deltas if payroll window affected

Leave request approved
leave-service state changes

leave_request_read updated

leave_balance_read reserved/consumed balances updated

leave audit row appended

affected attendance daily summaries for date range updated to on_leave_flag

Shift reassigned
new assignment event emitted

shift_assignment_read closes prior row and inserts new row

future attendance derivation uses new shift

shift assignment audit appended

if backdated, reprocess impacted attendance dates

Employee deactivated
employee status updated

snapshot reflects inactive/terminated

lifecycle history appended

access caches invalidated

audit records preserved; records remain searchable per authorization policy

14. Tradeoffs and decision log
Normalized vs denormalized read model
Recommendation: hybrid
Why: master data normalized; high-volume query models denormalized.

Single read DB vs hybrid architecture
Recommendation: hybrid
Why: PostgreSQL alone is not ideal for full-text audit search and heavy analytics.

Strong consistency vs eventual consistency
Recommendation: eventual by default, deterministic versioned reads for payroll/compliance
Why: balances performance with statutory correctness.

Real-time vs batch summaries
Recommendation: daily summary incremental + nightly reconcile; monthly summary batch/incremental
Why: gives dashboards speed without losing correctness.

Embedded audit metadata vs separate history tables
Recommendation: both
Why: current-state debugging + immutable evidence.

Relational DB vs search engine vs analytical store
Recommendation: all three where justified
Why: operational queries, search, and analytics are materially different workloads.

Cost vs performance
Recommendation: start with PostgreSQL + Redis + search; add warehouse once reporting load justifies it.

Simplicity vs scalability
Recommendation: begin with modular projections and clear event contracts so complexity grows safely.

15. Final recommendation
Best recommended architecture
Build a CQRS-style hybrid read platform:

PostgreSQL operational read model

Redis cache

Elasticsearch/OpenSearch for employee/audit/document search

analytics warehouse/columnar store for heavy reports

CDC + event-driven projections

batch recomputation/reconciliation for summaries

This matches your HRMS direction and the source documents’ emphasis on PostgreSQL, Redis, Elasticsearch/search, microservices, reporting, compliance, and auditability. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Recommended schema strategy
normalized tenant/org/reference dimensions

denormalized employee and daily/monthly read models

append-only attendance event facts

immutable audit/history tables

versioned payroll/compliance snapshots

partitioned time-based fact tables

Recommended audit strategy
audit as a first-class subsystem

base-row metadata + immutable history tables

correlation fields everywhere: source_event_id, request_id, transaction_id, workflow_id

summary derivation lineage

replayable projections

legal-hold capable archives

Recommended security controls
SSO + MFA

RBAC + ABAC + PostgreSQL RLS

field-level encryption for salary/identity

masked secure reporting views

segregated biometric references

privileged access auditing

Recommended operational controls
PITR, multi-AZ, replicas, DR drills

projector lag monitoring

reconciliation jobs

DLQ and replay tooling

slow query and partition health monitoring

freshness SLAs surfaced in APIs/UI

Top risks
attendance volume explosion

weak audit lineage

payroll/compliance mismatch from stale reads

tenant leakage

expensive reporting over OLTP-shaped tables

backdated corrections breaking summaries

Top mitigations
partition raw facts

precompute summaries

immutable audit/event lineage

deterministic payroll versioning

RLS and strict tenant keys

replay/recompute tooling

Phased implementation plan
MVP
PostgreSQL read DB

employee_read_model

leave_balance_read

attendance_event_read

attendance_daily_summary

basic audit history

Redis caching

core APIs and dashboards

Scale-up
attendance_monthly_summary

payroll_read_summary

OpenSearch

async report service

sync audit / DLQ / replay tooling

manager hierarchy closure

materialized aggregates

Enterprise-grade maturity
analytical warehouse

full forensic audit package

legal hold / archive tooling

advanced reconciliation dashboards

tenant-level data isolation enhancements

projector versioning and shadow rebuilds

compliance evidence packs for WPS/PASI/Omanisation

If you want, I can turn this next into a full PostgreSQL DDL package, or into a Spring Boot/JPA + Flyway module design for your project.


Sources

first focus on insert entry explain each and every table and its relationships one by one


Thought for 24s
Good direction.
Before we design the full read side, the first thing to stabilize is the insert-side master structure that feeds the read model.

For your HRMS, I recommend we explain the tables in this order:

tenant and organization master

employee core master

employee profile extension

reporting hierarchy

biometric identity

shift master and assignment

attendance raw events

attendance summaries

leave tables

payroll read-support tables

notification/history

audit/history tables

That order matches the source documents, where the platform is built around employee master, org structure, attendance, leave, payroll, ESS, reporting, and complete auditability. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…

 
Personnex HR Suite 2024


I will start with the insert-entry foundation tables first, because every other table depends on them.

1. Insert-side design philosophy
When you said “focus on insert entry,” the right interpretation for a production HRMS is:

first define the authoritative master tables

make them easy to insert correctly

enforce relationships with foreign keys and controlled reference tables

keep transactional inserts normalized

later project them into denormalized read tables

So the insert-side database should be more normalized and integrity-focused, while the read DB can be more denormalized and query-focused.

For your HRMS, the source documents clearly require:

multi-company / multi-branch / multi-department structure

employee master with personal, contact, document, nationality, visa, and employment details

attendance and biometric integration

shift and leave engines

payroll, WPS, PASI, Omanisation, and full audit trail

ESS/MSS and reporting support. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


2. Table group 1: tenant and organization foundation
These are the very first tables that must exist before inserting employees.

2.1 tenant
Why this table exists
This table represents one client or one legal customer using the HRMS.
Because your system is multi-tenant, every business record must belong to a tenant.

What it stores
client/company ownership boundary

regional settings

timezone

base currency

compliance profile

active status

Sample columns
tenant (
    tenant_id uuid primary key,
    tenant_code varchar(50) unique not null,
    tenant_name varchar(200) not null,
    legal_name varchar(250),
    country_code varchar(10) not null,
    timezone varchar(50) not null,
    base_currency varchar(10) not null,
    status varchar(20) not null,
    created_at timestamptz not null,
    created_by varchar(100),
    updated_at timestamptz not null
)
Relationship
Parent of almost everything:

one tenant -> many companies

one tenant -> many employees

one tenant -> many departments

one tenant -> many shifts

one tenant -> many leave types

one tenant -> many audit events

Insert rule
No employee can be inserted unless tenant_id already exists.

2.2 company
Why this table exists
A tenant may have one or many legal entities.
The blueprint and proposal both mention multi-entity and multi-company capability for HRMS and payroll. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


What it stores
legal employer

payroll legal entity

WPS / PASI / statutory reporting entity

Sample columns
company (
    company_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    company_code varchar(50) not null,
    company_name varchar(200) not null,
    legal_entity_name varchar(250),
    registration_no varchar(100),
    tax_no varchar(100),
    wps_entity_code varchar(100),
    pasi_entity_code varchar(100),
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (tenant_id, company_code)
)
Relationship
one tenant -> many companies

one company -> many branches

one company -> many employees

one company -> many payroll groups

Insert rule
Insert tenant first, then company.

2.3 branch
Why this table exists
Your sources explicitly mention multi-branch connectivity and organization structure. 
Personnex HR Suite 2024

 
HRMS_Payroll_ESS_Proposal_AlSaw…


This table supports:

branch-level attendance

payroll grouping

compliance views

employee assignment by location

Sample columns
branch (
    branch_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    company_id uuid not null references company(company_id),
    branch_code varchar(50) not null,
    branch_name varchar(200) not null,
    location_name varchar(200),
    country_code varchar(10),
    city varchar(100),
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (tenant_id, company_id, branch_code)
)
Relationship
one company -> many branches

one branch -> many departments

one branch -> many employees

one branch -> many shift rosters

2.4 department
Why this table exists
Department is needed for:

org structure

headcount reporting

payroll cost analysis

Omanisation dashboard

manager/team views. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Sample columns
department (
    department_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    company_id uuid not null references company(company_id),
    branch_id uuid references branch(branch_id),
    parent_department_id uuid references department(department_id),
    department_code varchar(50) not null,
    department_name varchar(200) not null,
    cost_center_code varchar(100),
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (tenant_id, company_id, department_code)
)
Relationship
one branch -> many departments

one department may have child departments

one department -> many employees

Important note
parent_department_id creates a hierarchy, so this is a self-referencing table.

2.5 designation
Why this table exists
Designation stores job title / functional title.
Needed for employee master, reports, and payroll grouping.

Sample columns
designation (
    designation_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    designation_code varchar(50) not null,
    designation_name varchar(200) not null,
    grade_code varchar(50),
    band_code varchar(50),
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (tenant_id, designation_code)
)
Relationship
one designation -> many employees

2.6 position
Why this table exists
The blueprint separates job catalogue, approved headcount, vacancy, salary scale, KPI, and competency profile per position. 
HRMS_Payroll_ESS_Blueprint_Oman


A designation is the title.
A position is the actual approved slot in the org.

Example:

designation = “Software Engineer”

position = “Software Engineer - Chennai Branch - Slot 04”

Sample columns
position (
    position_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    company_id uuid not null references company(company_id),
    branch_id uuid references branch(branch_id),
    department_id uuid references department(department_id),
    designation_id uuid not null references designation(designation_id),
    position_code varchar(50) not null,
    position_title varchar(200) not null,
    approved_headcount int not null default 1,
    cost_center_code varchar(100),
    reports_to_position_id uuid references position(position_id),
    status varchar(20) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    unique (tenant_id, position_code)
)
Relationship
one department -> many positions

one designation -> many positions

one position -> many employee assignments over time

one position may report to another position

3. Table group 2: employee core insert tables
Now we move to employee insertion.

3.1 employee
This is the central master table.

Why this table exists
The source documents describe the employee information module as the central repository for personal, contact, emergency, passport, visa, residency, history, dependants, beneficiaries, and lifecycle management. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


But in a production design, you should not put everything into one huge table.
The employee table should contain only the stable core identity and employment keys.

Sample columns
employee (
    employee_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    company_id uuid not null references company(company_id),
    branch_id uuid references branch(branch_id),
    department_id uuid references department(department_id),
    designation_id uuid references designation(designation_id),
    position_id uuid references position(position_id),
    employee_code varchar(50) not null,
    employee_type varchar(30) not null,
    employment_status varchar(30) not null,
    hire_date date not null,
    confirmation_date date,
    joining_source varchar(30),
    nationality_type varchar(20) not null,
    is_omani boolean not null default false,
    manager_employee_id uuid references employee(employee_id),
    active_flag boolean not null default true,
    created_at timestamptz not null,
    created_by varchar(100),
    updated_at timestamptz not null,
    updated_by varchar(100),
    unique (tenant_id, employee_code)
)
Relationship
many employees belong to one tenant

many employees belong to one company

many employees belong to one branch

many employees belong to one department

many employees can have one designation

many employees can have one manager

one employee can manage many employees

Important design point
manager_employee_id is a self-reference.

So employee has a recursive relationship:

employee A -> manager employee B

employee B -> manager employee C

This supports org hierarchy and team views.

Insert order
Before inserting an employee, these should exist:

tenant

company

maybe branch

maybe department

maybe designation

maybe position

If manager is required, manager employee must already exist or can be updated later.

3.2 employee_profile
Why this table exists
Do not overload employee with personal details.
Split personally identifiable and profile details into a separate table.

This keeps:

cleaner schema

better security

easier masking

simpler updates

What it stores
name

DOB

gender

marital status

email

mobile

religion if legally allowed

preferred language

photo reference

emergency contact summary

Sample columns
employee_profile (
    employee_id uuid primary key references employee(employee_id),
    tenant_id uuid not null references tenant(tenant_id),
    first_name varchar(100) not null,
    middle_name varchar(100),
    last_name varchar(100),
    full_name varchar(250) not null,
    full_name_ar varchar(250),
    gender varchar(20),
    date_of_birth date,
    marital_status varchar(20),
    personal_email varchar(200),
    work_email varchar(200),
    mobile_no varchar(30),
    alternate_mobile_no varchar(30),
    preferred_language varchar(20),
    profile_photo_url text,
    created_at timestamptz not null,
    updated_at timestamptz not null
)
Relationship
one employee -> one employee_profile

This is a 1:1 relationship.

3.3 employee_address
Why this table exists
An employee can have more than one address:

permanent address

current address

emergency/contact address

home-country address for expatriates

So address should not be kept inside employee_profile as repeated columns.

Sample columns
employee_address (
    employee_address_id uuid primary key,
    employee_id uuid not null references employee(employee_id),
    tenant_id uuid not null references tenant(tenant_id),
    address_type varchar(30) not null,
    line1 varchar(250),
    line2 varchar(250),
    city varchar(100),
    state_name varchar(100),
    postal_code varchar(30),
    country_code varchar(10),
    is_primary boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null
)
Relationship
one employee -> many addresses

3.4 employee_identity_document
Why this table exists
The blueprint and proposal heavily emphasize passport, visa, labour card, civil ID, certifications, document vault, and expiry alerts. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


This should be a separate table because each employee may have multiple identity and compliance documents.

Sample columns
employee_identity_document (
    document_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    employee_id uuid not null references employee(employee_id),
    document_type varchar(50) not null,
    document_number varchar(150),
    issuing_country varchar(10),
    issue_date date,
    expiry_date date,
    document_file_url text,
    verification_status varchar(30),
    extracted_by_ocr boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null
)
Relationship
one employee -> many documents

Examples:

passport

visa

civil ID

labour card

professional certificate

3.5 employee_dependant
Why this table exists
The source documents mention dependant and beneficiary management for insurance and EOSB. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Sample columns
employee_dependant (
    dependant_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    employee_id uuid not null references employee(employee_id),
    dependant_name varchar(200) not null,
    relationship_type varchar(50) not null,
    date_of_birth date,
    nationality_code varchar(10),
    passport_no varchar(100),
    eligible_for_insurance boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null
)
Relationship
one employee -> many dependants

3.6 employee_beneficiary
Why this table exists
For EOSB, insurance, and settlement, beneficiary data should be stored separately.

Relationship
one employee -> many beneficiaries

This is similar to dependants, but used for financial/legal entitlement.

4. Table group 3: hierarchy and role relationship tables
4.1 employee_reporting_history
Why this table exists
If you only keep manager_employee_id in employee, you know only the current manager.
But HRMS needs history:

who reported to whom earlier

from what date to what date

transfer trail

team structure at a past point in time

Sample columns
employee_reporting_history (
    reporting_history_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    employee_id uuid not null references employee(employee_id),
    manager_employee_id uuid not null references employee(employee_id),
    effective_from date not null,
    effective_to date,
    reason_code varchar(50),
    created_at timestamptz not null
)
Relationship
one employee -> many reporting history rows

one manager -> many subordinates over time

4.2 user_account
Why this table exists
Not every employee must immediately be a system user.
So system login identity should be separate from employee master.

Sample columns
user_account (
    user_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    employee_id uuid references employee(employee_id),
    username varchar(150) not null,
    login_type varchar(30) not null,
    status varchar(20) not null,
    last_login_at timestamptz,
    created_at timestamptz not null,
    unique (tenant_id, username)
)
Relationship
one employee -> zero or one user account

one tenant -> many users

4.3 role_master
Why this table exists
The source documents require fine-grained role and permission management. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Sample columns
role_master (
    role_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    role_code varchar(50) not null,
    role_name varchar(100) not null,
    role_scope varchar(30) not null,
    status varchar(20) not null,
    unique (tenant_id, role_code)
)
4.4 user_role_map
Why this table exists
One user can have multiple roles.

Sample columns
user_role_map (
    user_role_map_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    user_id uuid not null references user_account(user_id),
    role_id uuid not null references role_master(role_id),
    effective_from timestamptz not null,
    effective_to timestamptz
)
Relationship
one user -> many roles

one role -> many users

This is a many-to-many relationship resolved through a mapping table.

5. Table group 4: biometric and attendance insert foundation
5.1 biometric_device
Why this table exists
Your system needs to support biometric integrations like ZKTeco, Suprema, Hikvision, Anviz, and mobile/GPS attendance. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…

 
Personnex HR Suite 2024


Sample columns
biometric_device (
    device_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    branch_id uuid references branch(branch_id),
    device_code varchar(100) not null,
    device_name varchar(200),
    vendor_name varchar(100),
    device_type varchar(50),
    ip_address varchar(100),
    status varchar(20) not null,
    created_at timestamptz not null
)
Relationship
one branch -> many devices

one tenant -> many devices

5.2 employee_biometric_identity
Why this table exists
An employee may have one or more biometric identifiers depending on the device ecosystem.

Do not store raw biometric template in the main business tables.

Sample columns
employee_biometric_identity (
    biometric_identity_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    employee_id uuid not null references employee(employee_id),
    device_id uuid references biometric_device(device_id),
    biometric_ref_hash varchar(200) not null,
    biometric_type varchar(30) not null,
    enrollment_status varchar(30) not null,
    enrolled_at timestamptz,
    revoked_at timestamptz,
    unique (tenant_id, biometric_ref_hash)
)
Relationship
one employee -> many biometric identities

one device -> many biometric identities

6. Table group 5: shift master tables
6.1 shift_definition
Why this table exists
The sources explicitly require fixed, rotating, flexible, split, night, temporary, roster-based shifts. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…

 
Personnex HR Suite 2024


Sample columns
shift_definition (
    shift_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    shift_code varchar(50) not null,
    shift_name varchar(150) not null,
    shift_type varchar(30) not null,
    start_time time not null,
    end_time time not null,
    break_minutes int default 0,
    grace_in_minutes int default 0,
    grace_out_minutes int default 0,
    night_shift_flag boolean not null default false,
    status varchar(20) not null,
    created_at timestamptz not null,
    unique (tenant_id, shift_code)
)
Relationship
one tenant -> many shifts

one shift -> many employee assignments

6.2 employee_shift_assignment
Why this table exists
Shift is not static. An employee can move between shifts over time.

So never store only shift_id directly in employee and stop there.
You need history.

Sample columns
employee_shift_assignment (
    shift_assignment_id uuid primary key,
    tenant_id uuid not null references tenant(tenant_id),
    employee_id uuid not null references employee(employee_id),
    shift_id uuid not null references shift_definition(shift_id),
    effective_from date not null,
    effective_to date,
    assignment_type varchar(30) not null,
    roster_batch_id uuid,
    assigned_by varchar(100),
    created_at timestamptz not null
)
Relationship
one employee -> many shift assignments over time

one shift -> many employees

7. Relationship summary so far
At this stage, the insert dependency chain is:

tenant
-> company
-> branch
-> department
-> designation
-> position
-> employee
-> employee_profile
-> employee_address
-> employee_identity_document
-> employee_dependant
-> employee_biometric_identity
-> employee_shift_assignment

And access/security side:

employee
-> user_account
-> user_role_map
-> role_master

Hierarchy side:

employee
-> manager_employee_id
and
employee_reporting_history

8. Recommended insert sequence for a new employee
When a new employee joins, the transactional insert order should be:

Step 1
Validate master data:

tenant exists

company exists

branch exists

department exists

designation exists

position exists if required

Step 2
Insert into employee

Step 3
Insert into employee_profile

Step 4
Insert optional supporting records:

employee_address

employee_identity_document

employee_dependant

employee_beneficiary

Step 5
Insert access record if login needed:

user_account

user_role_map

Step 6
Insert biometric mapping if enrolled:

employee_biometric_identity

Step 7
Insert initial shift:

employee_shift_assignment

Step 8
Insert audit/history:

employee created event

profile created event

shift assigned event

role assigned event

9. Normalization rule you should follow
For insert-side design:

keep tenant, company, branch, department, designation, position normalized

keep employee small and stable

move repeating or change-heavy data into child tables

use history tables for manager, shift, and status changes

do not store arrays or giant JSON blobs for core HR transactional data

reserve denormalization for read models only

10. My recommendation for the next table set
The next logical set to explain is:

attendance_event

attendance_correction_request

attendance_daily_summary

leave_type

leave_request

leave_balance

holiday_calendar

payroll_run

payroll_employee_summary

audit tables

That is where your insert-side becomes truly business-critical.

I can continue next with attendance tables one by one, including exact relationships and why raw punch, correction, and daily summary must be separate.


Sources

I see your design is not like senior database designer for hrms field, I need hrms saas that means I need to support multiple country, multiple currency and rule engines etc., (use example as zoho hrms or other hrms in the industry ) so create a master tables like senior database engineer and make sure you cover everything in an detailed way


Thought for 39s
You are right.
For a true HRMS SaaS, the master data model cannot look like a small single-country payroll database. It must be designed like a global HCM platform core:

multi-tenant SaaS

multi-country

multi-legal-entity

multi-currency

multi-calendar

multi-language

policy/rule-driven

auditable

extensible without schema rewrites

That direction is also consistent with your Oman blueprint and proposal, which call for a configurable compliance engine, multi-entity / multi-branch hierarchy, multi-currency payroll, country-specific compliance, ESS/MSS, fine-grained permissions, and a complete immutable audit trail. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


Industry platforms also follow this pattern: Zoho People/Zoho Payroll expose employee self-service, org structure, attendance, leave, payslips, and multi-organization payroll integration, while global HCM platforms like Workday and Darwinbox emphasize unified HR + payroll + self-service across distributed workforces. 

So below I will redesign this the way a senior HRMS database engineer would do it.

1. First principle: separate the HRMS database into master domains
Do not start from employee and keep adding columns forever.

A production HRMS SaaS should divide master data into these domains:

platform / tenancy masters

global localization masters

organization and workforce structure masters

employee core and lifecycle masters

identity, document, and compliance masters

job / position / grade / compensation masters

policy and rule engine masters

time, attendance, leave, and holiday masters

payroll configuration masters

workflow, approval, and delegation masters

security and access masters

audit and version masters

integration masters

That is the correct foundation for a SaaS HRMS.

2. Core design principles for the master schema
2.1 What must be configurable
The blueprint explicitly says compliance must be encoded as version-controlled rules, and future GCC expansion should happen through a configurable compliance engine, not through rewriting core logic. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Blueprint_Oman


So your DB must not hardcode things like:

only Oman leave rules

only one currency

only one weekend definition

only one payroll frequency

only one national ID type

only one public holiday calendar

only one approval chain

2.2 What must be master-driven
These should come from master tables, not application constants:

country

legal entity

business unit

cost center

location

currency

exchange rate source

calendar

holiday calendar

leave type

attendance policy

overtime rule

shift pattern

payroll component

statutory scheme

deduction rule

approval policy

document type

identifier type

employee category

employment type

contract type

separation type

2.3 What must be temporal
A senior HRMS model must support effective dating.
Current value is not enough.

These entities need effective_from, effective_to, version_no, is_current patterns:

employee organization assignment

employee position assignment

employee compensation structure

employee bank account

employee nationality / visa / permit

employee shift assignment

employee policy assignment

statutory registration

manager assignment

Without temporal modeling, HRMS history becomes unreliable.

3. Master domain 1: platform / tenancy masters
These tables define SaaS ownership and isolation.

3.1 tenant
Represents one SaaS customer.

Why it exists

top isolation boundary

billing boundary

configuration boundary

data residency boundary

Important columns

tenant_id

tenant_code

tenant_name

tenant_type

status

data_region

default_country_code

default_timezone

default_currency_code

default_locale

subscription_plan

go_live_date

Relationships

one tenant -> many legal entities

one tenant -> many org units

one tenant -> many employees

one tenant -> many rule sets

one tenant -> many integrations

3.2 tenant_feature_flag
Controls which modules are enabled.

Why
Different customers may use:

only core HR

HR + attendance

HR + payroll

full HCM

Columns

tenant_feature_flag_id

tenant_id

feature_code

enabled_flag

effective_from

effective_to

3.3 tenant_configuration
Stores tenant-specific settings.

Examples

employee code generation pattern

default weekend pattern

allowed login methods

ESS branding

document retention days

payroll lock policy

Better kept as structured config, but not for transactional records.

4. Master domain 2: global localization masters
This is where your earlier design was too small. A global HRMS must begin with localization.

4.1 country_master
Each supported country.

Why
Every country brings:

labor law

payroll tax

holiday calendars

ID document patterns

date and number formats

notice period defaults

statutory schemes

Columns

country_code PK

country_name

iso2

iso3

default_currency_code

default_locale

week_start_day

primary_timezone

supports_hijri_flag

4.2 currency_master
All supported currencies.

Columns

currency_code

currency_name

symbol

decimal_precision

minor_unit_name

rounding_strategy

4.3 exchange_rate_master
Used for multi-currency payroll, allowances, air ticket liabilities, cross-border reporting. Your blueprint explicitly calls out multi-currency processing. 
HRMS_Payroll_ESS_Blueprint_Oman


Columns

rate_id

base_currency_code

quote_currency_code

rate_type

rate_value

effective_date

rate_source

tenant_id nullable

4.4 locale_master
Needed for bilingual and multi-language UI/document rendering. The blueprint requires Arabic/English and dual formatting support. 
HRMS_Payroll_ESS_Blueprint_Oman


4.5 calendar_system_master
Examples:

Gregorian

Hijri

This is important because leave and holiday logic in GCC may require Hijri-aware calendars. 
HRMS_Payroll_ESS_Blueprint_Oman


4.6 timezone_master
Needed for:

attendance timestamps

payroll cutoff

mobile check-in normalization

geo-distributed entities

5. Master domain 3: organization and enterprise structure masters
This is the backbone of a serious HRMS.

5.1 legal_entity
This is the employer of record.

Why
In SaaS HRMS, tenant and legal employer are not always the same.
A customer may have:

parent group

Oman LLC

UAE branch

India captive entity

Columns

legal_entity_id

tenant_id

entity_code

entity_name

country_code

base_currency_code

registration_no

tax_registration_no

statutory_employer_flag

default_holiday_calendar_id

default_payroll_calendar_id

status

Relationships

one tenant -> many legal entities

one legal entity -> many locations

one legal entity -> many employees

one legal entity -> many payroll groups

one legal entity -> many statutory registrations

5.2 business_unit
Operational grouping, usually not legal.

5.3 division
5.4 department
5.5 sub_department
5.6 cost_center
5.7 profit_center
5.8 location
5.9 work_site
A senior design should not force everything into a single department table.

Better approach
Use:

org_unit_type_master

org_unit

org_unit_hierarchy

This lets you support unlimited depth, which your blueprint explicitly requires. 
HRMS_Payroll_ESS_Blueprint_Oman


Recommended model
org_unit_type_master
Values:

company

business_unit

division

department

section

team

cost_center

project_org

branch

org_unit
Columns

org_unit_id

tenant_id

legal_entity_id nullable

org_unit_type_code

org_unit_code

org_unit_name

country_code

currency_code nullable

manager_position_id nullable

effective_from

effective_to

status

org_unit_hierarchy
Columns

org_unit_hierarchy_id

tenant_id

parent_org_unit_id

child_org_unit_id

hierarchy_type

effective_from

effective_to

This is much better than hardcoding:

company -> branch -> department only

Because large HRMS customers always break that assumption.

6. Master domain 4: workforce and employment masters
Now we model the worker properly.

6.1 person
This is the human identity.

Why separate person from employee
A global HRMS may track:

candidate

employee

contingent worker

intern

ex-employee

rehire

board member

contractor

One person can have multiple work relationships over time.

Columns

person_id

tenant_id

global_person_no

full_name

preferred_name

first_name

middle_name

last_name

full_name_local

gender_code

date_of_birth

marital_status_code

blood_group_code nullable

religion_code nullable if legally allowed

primary_nationality_code

secondary_nationality_code nullable

photo_file_id nullable

deceased_flag

created_at

6.2 person_contact
Multiple contacts:

work email

personal email

mobile

emergency contact

6.3 person_address
Multiple address types:

permanent

current

home-country

correspondence

emergency family address

6.4 work_relationship
This is the actual employment or engagement.

Why
One person may have:

one terminated employment record

one rehired record

one concurrent assignment

one consultant contract

Columns

work_relationship_id

tenant_id

person_id

relationship_type_code

legal_entity_id

employment_type_code

worker_category_code

contract_type_code

hire_date

service_start_date

termination_date nullable

status_code

primary_flag

This is a better enterprise pattern than a single employee row.

6.5 employee_assignment
This is the assignment of a work relationship into the org.

Columns

employee_assignment_id

tenant_id

work_relationship_id

employee_code

legal_entity_id

org_unit_id

position_id

job_id

grade_id

location_id

business_title

manager_assignment_id nullable

employment_status_code

assignment_status_code

full_time_equivalent

effective_from

effective_to

This table is critical.

In a senior HRMS schema:

person = who the person is

work_relationship = what type of engagement they have

employee_assignment = where they sit now in the organization

That separation is standard-grade HCM modeling.

7. Master domain 5: job, position, grade, and career masters
Your blueprint explicitly calls for job catalogue, grade, band, salary scale, KPIs, competency profile, approved headcount, and vacancy management. 
HRMS_Payroll_ESS_Blueprint_Oman


7.1 job_family_master
Examples:

Engineering

Finance

HR

Operations

7.2 job_master
Generic job definition.

Columns

job_id

tenant_id

job_code

job_title

job_family_id

job_description

job_level_code

country_scope

is_managerial

is_billable

status

7.3 grade_master
Examples:

G1 to G12

M1 to M5

7.4 band_master
Optional above-grade grouping.

7.5 salary_scale_master
Why
Salary ranges should be master-driven by:

grade

country

legal entity

currency

effective date

7.6 position_master
A concrete approved headcount slot.

Columns

position_id

tenant_id

legal_entity_id

org_unit_id

job_id

grade_id

position_code

position_title

reports_to_position_id

approved_headcount

vacancy_status

cost_center_id

budgeted_flag

effective_from

effective_to

7.7 position_budget_master
Used for approved vs actual headcount and manpower planning.

8. Master domain 6: employee identity, document, nationality, and compliance masters
Your blueprint and proposal both heavily emphasize passport, visa, labor card, civil ID, OCR, expiry alerts, Omani vs expatriate logic, and full audit history. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


8.1 identifier_type_master
Examples:

employee_code

civil_id

national_id

passport

visa_no

work_permit_no

labor_card_no

tax_id

pension_id

biometric_ref

8.2 person_identifier
Stores real identifiers.

Columns

person_identifier_id

tenant_id

person_id

identifier_type_code

country_code

identifier_value_encrypted

identifier_hash

issue_date

expiry_date

issuing_authority

primary_flag

verification_status

effective_from

effective_to

8.3 document_type_master
Examples:

passport

visa

residence permit

civil ID

labor card

contract

education certificate

professional license

medical certificate

8.4 person_document
Columns

person_document_id

tenant_id

person_id

document_type_code

document_number

country_code

issue_date

expiry_date

document_file_id

ocr_status

verification_status

renewal_required_flag

8.5 nationality_master
8.6 visa_type_master
8.7 permit_type_master
8.8 residency_status_master
8.9 person_nationality_history
Because nationality, citizenship status, or nationalization classification may matter historically.

8.10 employee_compliance_profile
A compact current-state table for:

omani / expatriate flag

pension category

occupational hazard category

visa required flag

air ticket eligible flag

wps eligible flag

eosb eligible flag

union/tax/social insurance category

This is one of the most useful operational masters in a global HRMS.

9. Master domain 7: policy and rule engine masters
This is where a senior design becomes different from a small payroll app.

Your blueprint is very explicit: compliance is implemented by a version-controlled rules engine, and future GCC expansion should be handled by changing the rules, not rewriting code. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Blueprint_Oman


So you need a generalized rule framework.

9.1 rule_domain_master
Examples:

attendance

leave

payroll

overtime

EOSB

tax

pension

loan_eligibility

document_expiry

workflow_routing

compliance_alert

shift_assignment

air_ticket

9.2 rule_set_master
A rule set groups rules for a country/tenant/module.

Columns

rule_set_id

tenant_id nullable

country_code nullable

legal_entity_id nullable

rule_domain_code

rule_set_name

version_no

status

effective_from

effective_to

approval_status

9.3 rule_definition
A single rule.

Columns

rule_definition_id

rule_set_id

rule_code

rule_name

rule_type

expression_language

condition_expression

action_expression

priority_no

stop_processing_flag

version_no

9.4 rule_parameter
Stores configurable parameters.

Examples:

annual_leave_days = 30

sick_leave_full_days = 10

overtime_weekday_multiplier = 1.25

EOSB_first_3_years_days = 15

9.5 policy_master
Human-readable business policy object.

Examples:

UAE annual leave policy 2026

Oman attendance policy for field staff

India grade loan policy

GCC overtime policy for operations staff

9.6 policy_assignment
Which employees/groups use which policy.

Assignment scope can be:

tenant-wide

legal entity

org unit

employee category

grade

worker type

individual employee

This is essential.

10. Master domain 8: classification masters
A strong HRMS always centralizes controlled vocabularies.

Must-have classification masters
employment_type_master

worker_category_master

contract_type_master

employee_status_master

assignment_status_master

separation_type_master

gender_master

marital_status_master

religion_master where legally permitted

education_level_master

language_master

skill_master

competency_master

performance_rating_master

disciplinary_action_type_master

grievance_type_master

These look simple, but they are what keep a SaaS HRMS consistent across countries and tenants.

11. Master domain 9: compensation and payroll masters
Your blueprint and proposal require configurable earnings/deductions, multi-payroll-group, multi-currency, salary scales, WPS, PASI, EOSB, loans, advances, and inter-company payroll. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


11.1 payroll_group_master
Grouping for payroll processing.

11.2 payroll_calendar_master
Defines:

monthly

biweekly

weekly

semi-monthly

11.3 pay_component_master
This is critical.

Values:

earning

deduction

employer_contribution

reimbursement

accrual

informational

Examples:

basic salary

housing allowance

transport allowance

overtime

leave without pay deduction

pension employee contribution

pension employer contribution

loan recovery

gratuity provision

Columns

pay_component_id

tenant_id

component_code

component_name

component_category

taxable_flag

pensionable_flag

currency_handling_type

calculation_basis

posting_gl_code

sequence_no

11.4 salary_structure_master
Template of components for a category/grade/country.

11.5 salary_structure_line
Each component inside the structure.

11.6 employee_compensation_assignment
Assigns salary structure to employee assignment.

11.7 statutory_scheme_master
Examples:

Oman PASI

UAE gratuity

KSA GOSI

India PF/ESI/PT/TDS

11.8 statutory_scheme_rule_link
Links schemes to rule sets.

11.9 employee_statutory_enrollment
Stores employee’s live scheme enrollment.

11.10 bank_master
11.11 bank_branch_master
11.12 employee_bank_account
Needed for:

WPS

IBAN verification

multi-account disbursement

primary salary account

reimbursement account

11.13 loan_type_master
11.14 advance_type_master
12. Master domain 10: attendance, shift, leave, and holiday masters
These are not only transactions. They also require deep masters.

12.1 attendance_policy_master
Examples:

office policy

factory shift policy

field force GPS policy

project-site attendance policy

12.2 attendance_policy_rule
Rules like:

grace in

grace out

missing punch tolerance

shortfall penalty

unauthorized absence penalty

geofence required

selfie required

buddy-punch threshold

12.3 shift_type_master
12.4 shift_pattern_master
Examples:

fixed

rotating

split

flexible

project-based

night shift

The blueprint explicitly lists these patterns. 
HRMS_Payroll_ESS_Blueprint_Oman


12.5 shift_pattern_day
Day-wise details of pattern.

12.6 overtime_rule_master
Rules:

weekday OT multiplier

rest-day multiplier

holiday multiplier

pre-approval requirement

rounding rule

minimum OT unit

12.7 work_schedule_calendar
12.8 weekend_definition_master
Important for multi-country support.

12.9 holiday_calendar_master
12.10 holiday_calendar_event
Must support:

national holiday

Islamic holiday

company holiday

restricted holiday

local holiday

floating holiday

12.11 leave_type_master
Must be highly configurable.

Columns

leave_type_id

tenant_id

country_code nullable

leave_code

leave_name

paid_flag

gender_restriction

religion_restriction

nationality_restriction

carry_forward_allowed

encashment_allowed

medical_certificate_required_flag

attachment_required_flag

negative_balance_allowed

holiday_counting_rule

weekend_counting_rule

12.12 leave_policy_master
12.13 leave_policy_rule
12.14 leave_accrual_rule_master
12.15 leave_encashment_rule_master
12.16 ticket_entitlement_rule_master
The proposal and blueprint both call out air ticket entitlement tracking and expatriate contract-linked leave/ticket rules. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


13. Master domain 11: workflow, approval, and delegation masters
Your source documents explicitly require no-code workflow configuration, approval chains, delegation of authority, escalation, and manager leave delegation. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


13.1 workflow_definition
Examples:

leave request approval

overtime approval

bank change approval

employee profile sensitive-field change

payroll lock approval

disciplinary approval

separation clearance approval

13.2 workflow_step_definition
13.3 workflow_condition_definition
13.4 workflow_action_definition
13.5 approval_matrix_master
Defines who can approve what.

Dimensions:

module

transaction type

country

legal entity

org unit

amount threshold

employee category

grade

13.6 delegation_of_authority_master
Very important for real HRMS.

Columns

delegation_id

tenant_id

delegator_user_id

delegate_user_id

approval_scope

start_date

end_date

limit_amount

country_scope

module_scope

13.7 escalation_rule_master
For SLA-based routing.

14. Master domain 12: security and access masters
Your blueprint explicitly requires RBAC + ABAC, row-level security, field-level control, SSO, MFA, and complete immutable audit logging. 
HRMS_Payroll_ESS_Blueprint_Oman


14.1 user_account
14.2 auth_provider_master
Examples:

local

LDAP

SAML

OAuth

Azure AD

Keycloak

14.3 role_master
14.4 permission_master
14.5 role_permission_map
14.6 user_role_assignment
14.7 data_scope_policy
Defines what data a user can see:

self

direct reports

indirect reports

legal entity

org unit

payroll group

field-level salary access

14.8 field_access_policy
This is where senior HRMS design differs from ordinary apps.

Examples:

HR admin can edit passport expiry

manager cannot see bank account

payroll officer can see net pay but not grievance details

employee can edit mobile but not nationality

only CFO can unlock payroll

15. Master domain 13: integration masters
Your platform needs many connectors: biometric devices, WPS banks, PASI, ERP/GL, job boards, email/SMS, AD/SSO. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


15.1 integration_system_master
15.2 integration_endpoint_master
15.3 integration_mapping_master
15.4 external_code_map
Critical for mapping:

bank code

department code

job code

earning component code

statutory form code

ERP GL code

15.5 biometric_device_master
15.6 device_location_map
15.7 employee_biometric_enrollment
16. Master domain 14: audit and version masters
Do not treat audit as optional.

16.1 entity_version_master
Tracks versioned masters.

16.2 change_reason_master
16.3 audit_event_master
16.4 audit_entity_change
16.5 data_correction_reason_master
16.6 record_source_master
For serious HRMS, every master row should also have:

created_at

created_by

updated_at

updated_by

version_no

source_system

change_reason_code

approved_by

approved_at

is_deleted

deleted_at

17. Recommended top-level master tables for your HRMS SaaS
If I were defining the actual enterprise master catalog, I would start with this list.

Platform / Tenant
tenant

tenant_feature_flag

tenant_configuration

Localization
country_master

currency_master

exchange_rate_master

locale_master

timezone_master

calendar_system_master

Enterprise Structure
legal_entity

org_unit_type_master

org_unit

org_unit_hierarchy

location_master

cost_center_master

profit_center_master

Workforce Core
person

person_contact

person_address

work_relationship

employee_assignment

employee_assignment_history

Job / Position
job_family_master

job_master

grade_master

band_master

salary_scale_master

position_master

position_budget_master

Identity / Compliance
identifier_type_master

person_identifier

document_type_master

person_document

nationality_master

visa_type_master

permit_type_master

employee_compliance_profile

Policy / Rules
rule_domain_master

rule_set_master

rule_definition

rule_parameter

policy_master

policy_assignment

Attendance / Leave
attendance_policy_master

attendance_policy_rule

shift_type_master

shift_pattern_master

shift_pattern_day

overtime_rule_master

holiday_calendar_master

holiday_calendar_event

leave_type_master

leave_policy_master

leave_policy_rule

leave_accrual_rule_master

leave_encashment_rule_master

ticket_entitlement_rule_master

Payroll
payroll_group_master

payroll_calendar_master

pay_component_master

salary_structure_master

salary_structure_line

employee_compensation_assignment

statutory_scheme_master

employee_statutory_enrollment

bank_master

bank_branch_master

employee_bank_account

loan_type_master

advance_type_master

Workflow / Security
workflow_definition

workflow_step_definition

approval_matrix_master

delegation_of_authority_master

user_account

role_master

permission_master

role_permission_map

user_role_assignment

data_scope_policy

field_access_policy

Integration / Audit
integration_system_master

integration_mapping_master

external_code_map

biometric_device_master

employee_biometric_enrollment

audit_event_master

audit_entity_change

18. Relationships a senior model must enforce
Here is the relationship backbone:

tenant -> many legal_entity

tenant -> many org_unit

tenant -> many policy_master

tenant -> many rule_set_master

legal_entity -> many employee_assignment

legal_entity -> many payroll_group_master

legal_entity -> many holiday_calendar_master

person -> many work_relationship

work_relationship -> many employee_assignment

employee_assignment -> one current position_master

position_master -> one job_master

job_master -> one job_family_master

employee_assignment -> one org_unit

employee_assignment -> one location_master

person -> many person_identifier

person -> many person_document

policy_master -> many policy_assignment

rule_set_master -> many rule_definition

leave_policy_master -> many leave_policy_rule

attendance_policy_master -> many attendance_policy_rule

salary_structure_master -> many salary_structure_line

employee_assignment -> one current employee_compensation_assignment

employee_assignment -> many employee_bank_account

employee_assignment -> many employee_statutory_enrollment

workflow_definition -> many workflow_step_definition

role_master -> many permission_master through role_permission_map

That is the minimum serious backbone.

19. Anti-patterns to avoid
Do not design your HRMS master model like this:

one huge employee table with 300 columns

hardcoded leave types in code

hardcoded country logic in service layer

one single department table doing org, cost center, and branch together

bank data directly on employee row

one salary column instead of pay components

one manager_id only, with no history

one shift_id directly on employee, with no effective dates

one country field controlling all labor laws

one role column in user table

one approval workflow per module hardcoded in code

updating history rows in place instead of effective dating

That is exactly what breaks when the product becomes SaaS.

20. Best practical recommendation for your HRMS
For your product, I recommend the master schema be built in three layers:

Layer A: immutable global/reference masters
Rarely changed:

country

currency

locale

identifier types

document types

rule domains

workflow action types

Layer B: tenant-scoped configurable masters
Configured per customer:

legal entities

org units

grades

positions

leave types

payroll components

holiday calendars

salary structures

approval matrices

policy sets

Layer C: person and employment masters
High-value business entities:

person

work relationship

employee assignment

compensation assignment

compliance profile

bank accounts

documents

biometric enrollments

That split will keep the system maintainable.

21. What I recommend we do next
The next best step is not attendance or payroll transactions yet.

The right next step is:

I will design the actual DDL-grade master schema in order, one by one, starting with:

tenant

legal_entity

org_unit_type_master

org_unit

org_unit_hierarchy

person

work_relationship

employee_assignment

job_master

position_master

policy_master

rule_set_master

That will give you the real enterprise skeleton first.

In your documents, this direction is supported by the need for multi-entity and unlimited org hierarchy, employee master with documents and lifecycle history, configurable compliance rules, multi-currency payroll, configurable leave/attendance/payroll engines, no-code workflows, fine-grained permissions, and exportable immutable audit logs. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…


If you want, I will continue in the next response with Table 1 to Table 12, each with:

purpose

columns

PK/FK

relationships

effective-dating strategy

why it exists in a global HRMS SaaS

and PostgreSQL-ready sample schema.


Sources

create er diagram for this

Yes — here is a senior-level HRMS SaaS master ER diagram for the core foundation model.

I’m keeping it focused on the master architecture backbone first, so it stays readable and useful. This covers:

SaaS tenancy

localization

legal entity and org structure

person and employment

job / position / grade

policy / rules

payroll masters

attendance / leave masters

workflow / security

audit / integration

You can paste this directly into any Mermaid-supported editor.




How to read this diagram
This ERD is intentionally organized into layers:

Platform layer

TENANT

localization masters

feature and configuration

Enterprise layer

LEGAL_ENTITY

ORG_UNIT

ORG_UNIT_HIERARCHY

LOCATION_MASTER

Workforce layer

PERSON

WORK_RELATIONSHIP

EMPLOYEE_ASSIGNMENT

Job layer

JOB_MASTER

GRADE_MASTER

POSITION_MASTER

Policy and rules layer

RULE_SET_MASTER

RULE_DEFINITION

POLICY_MASTER

POLICY_ASSIGNMENT

Time / leave / payroll layer

attendance, shift, holiday, leave, payroll, statutory, bank

Access and workflow layer

users, roles, permissions, workflow, approval matrix, delegation

Audit and integration layer

audit events

external mapping

biometric and integration systems

Most important design choices reflected in this ERD
The biggest senior-level choices here are:

PERSON is separate from WORK_RELATIONSHIP

WORK_RELATIONSHIP is separate from EMPLOYEE_ASSIGNMENT

ORG_UNIT is generalized instead of hardcoding only branch/department

policy is separate from rule implementation

payroll components are master-driven

all critical assignments are effective-dated

audit is modeled as a first-class domain

multi-country and multi-currency support is built into the foundation

Recommended next step
The best next step is to split this into 3 focused ER diagrams so it becomes implementation-ready:

Core HR master ERD

Attendance / Leave / Shift ERD

Payroll / Compliance / Audit ERD

That will be much more usable for actual database design and DDL creation.


Create ER diagram for Core HR master ERD

Here is a focused Core HR Master ERD for your HRMS SaaS foundation.

It covers the core master domains only:

tenant and localization

legal entity and organization

person and employment

job / grade / position

contacts, addresses, identifiers, documents

user and access linkage

core audit trail for master data

You can paste this directly into any Mermaid-supported editor.




Scope of this Core HR ERD
This one intentionally excludes:

attendance transactions

shift assignments

leave transactions

payroll transactions

rule engine internals

workflow execution tables

It keeps only the Core HR master backbone.

What this ERD gets right for SaaS HRMS
The key enterprise-grade choices are:

PERSON is separate from WORK_RELATIONSHIP

WORK_RELATIONSHIP is separate from EMPLOYEE_ASSIGNMENT

ORG_UNIT is generalized and hierarchy-driven

POSITION_MASTER is separate from JOB_MASTER

identifiers and documents are separate child entities

user access is linked to assignment, not directly to raw person only

master changes are auditable

Best next diagram after this
The next one should be either:

Attendance / Shift / Leave ERD
or

Payroll / Compensation / Compliance ERD

If you want, I’ll create the Attendance / Shift / Leave ERD next in the same format.


Export the discussion as a pdf

I created the PDF for you.

You can download it here:
Download the PDF

If you want, I can also create a better formatted architecture document that includes:

Full Core HR ERD

Attendance / Leave ERD

Payroll / Compensation ERD

Rule engine architecture

Microservice boundaries

Database schema strategy for SaaS

That would give you a proper 20–30 page HRMS architecture document you could use for design reviews or proposals. 


Yes go ahead 


Thought for 14m 32s
Done — I created the full architecture document in both formats:

Download the DOCX
Download the PDF

It packages the discussion into a structured blueprint covering Core HR, attendance/leave, payroll/compliance, rule engine architecture, service boundaries, security/audit controls, and a phased delivery plan, aligned to the Oman blueprint, proposal, and Personnex references you shared. 
HRMS_Payroll_ESS_Blueprint_Oman

 
HRMS_Payroll_ESS_Proposal_AlSaw…

 
Personnex HR Suite features 202…


Next, I can produce a second volume with detailed table-by-table schema and PostgreSQL DDL.


Sources



