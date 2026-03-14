# Page 09 - Payslips, Salary Documents, Loan and Advance

## Extracted intent

- Employee-facing payroll outputs and financing workflows.

## Requirements

- Payslip generation, protection, and delivery.
- Salary certificate and letter generation.
- Loan/advance request, approval, and EMI recovery.

## Screen requirements

1. `SCR-PT-007 Payslip Generation and Delivery`
   - Why: legal salary statement and employee transparency.
2. `SCR-ESS-004 Payslip and Salary Letters`
   - Why: reduce HR operational overhead.
3. `SCR-PT-011 Loan and Advance Lifecycle`
   - Why: finance control for employee advances with payroll recovery.

## API contracts

- `POST /api/v1/payroll-runs/{runId}/payslips/generate`
- `GET /api/v1/employees/{employeeId}/payslips`
- `POST /api/v1/employees/{employeeId}/documents/salary-certificate`
- `POST /api/v1/loans/requests`
- `GET /api/v1/loans/{loanId}/schedule`

## Rules and validations

- Payslip PDFs must be watermark-enabled and password-protected.
- Loan approval must check tenure/grade/eligibility rules.

## Data objects touched

- `Payslip`
- `DocumentRequest`
- `LoanRequest`
- `LoanRepaymentSchedule`

## Acceptance criteria samples

- Given published payslips, when employee opens ESS payslip list, then only self-owned records are accessible.
- Given approved loan request, when next payroll run is computed, then EMI deduction is auto-added as configured.

## SaaS forward notes

- Document templates must be tenant-localized and policy-driven.
