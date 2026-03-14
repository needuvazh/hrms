# Page 10 - ESS, MSS, and Optional Mobile Capabilities

## Extracted intent

- ESS and MSS are primary operating channels for requests and approvals.

## Requirements

- Employee self-service for profile, leave, payslip, requests, and exit.
- Manager inbox for multi-level approvals.
- Mobile parity for key request/approval flows.

## Screen requirements

1. `SCR-ESS-001 ESS Dashboard`
   - Why: single entry point for employee actions.
2. `SCR-ESS-002/003/004/005/006`
   - Why: self-service coverage reduces HR workload and increases transparency.
3. `SCR-MSS-001 Manager Approval Inbox`
   - Why: payroll-cycle requests must be approved on time.
4. `SCR-MSS-002 Team Calendar`
   - Why: contextual approval decisions.

## API contracts

- `GET /api/v1/ess/dashboard`
- `POST /api/v1/ess/profile-change-requests`
- `POST /api/v1/leave-requests`
- `GET /api/v1/approvals/inbox`
- `POST /api/v1/approvals/{approvalId}/actions`

## Rules and validations

- Approval action requires role + delegation check.
- ESS access restricted to self-owned records except delegated scenarios.

## Data objects touched

- `EssRequest`
- `ApprovalTask`
- `Delegation`
- `LeaveBalance`

## Acceptance criteria samples

- Given manager delegation is active, when delegate opens approval inbox, then only delegated scope requests are visible.
- Given employee submits leave request, when manager approves, then leave balance and team calendar refresh within same process cycle.

## SaaS forward notes

- Keep ESS request types extensible through workflow metadata.
