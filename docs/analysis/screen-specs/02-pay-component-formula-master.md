# 02 - Pay Component Formula Master

## Purpose

Define dynamic component formulas with dependency-safe execution.

## UI sections

1. Formula list grid
2. Formula editor
3. Dependency graph preview
4. Validation results panel
5. Version timeline and audit tabs

## Fields

- `formulaCode`
- `componentId`
- `expression`
- `dependsOnComponents[]`
- `priorityOrder`
- `roundingRuleId`
- `effectiveFrom`
- `effectiveTo`
- `status`

## Example expressions

- `round(COMP.BASIC * 0.40, 3)`
- `if(VAR.OT_HOURS > 0, round(VAR.OT_HOURS * (COMP.BASIC / 30 / 8) * 1.25, 3), 0)`

## Validations

- expression parser must pass before save.
- no circular dependencies.
- referenced components must be active on formula effective date.
- `priorityOrder` must not conflict in same component scope.

## API contracts

## Create

`POST /api/v1/pay-component-formulas`

```json
{
  "formulaCode": "HRA_40",
  "componentId": "d65f5f6e-5cab-4d3b-b88f-b2c5fd7d63f6",
  "expression": "round(COMP.BASIC * 0.40, 3)",
  "dependsOnComponents": ["BASIC"],
  "priorityOrder": 20,
  "roundingRuleId": "2e8f48ed-5778-4387-b9f5-7d044f73ea36",
  "effectiveFrom": "2026-04-01"
}
```

## Validate expression

`POST /api/v1/pay-component-formulas/validate`

```json
{
  "expression": "round(COMP.BASIC * 0.40, 3)",
  "dependsOnComponents": ["BASIC"]
}
```

## Activate

`POST /api/v1/pay-component-formulas/{formulaId}/activate`

## DDL draft

```sql
create table payroll.pay_component_formula (
  id uuid primary key,
  tenant_id uuid not null,
  formula_code varchar(40) not null,
  component_id uuid not null,
  expression text not null,
  dependency_codes text not null,
  priority_order int not null,
  rounding_rule_id uuid,
  effective_from date not null,
  effective_to date,
  status varchar(20) not null,
  version int not null,
  created_at timestamptz not null,
  created_by varchar(100) not null,
  updated_at timestamptz not null,
  updated_by varchar(100) not null,
  unique (tenant_id, formula_code, version)
);
```

## Audit events

- `PAY_COMPONENT_FORMULA_CREATED`
- `PAY_COMPONENT_FORMULA_VALIDATED`
- `PAY_COMPONENT_FORMULA_ACTIVATED`

## Test cases

1. save formula with cyclic dependency -> reject.
2. validate malformed expression -> reject with parse error line info.
3. activate formula with inactive referenced component -> reject.
4. run evaluation smoke test with sample payload -> deterministic result.
