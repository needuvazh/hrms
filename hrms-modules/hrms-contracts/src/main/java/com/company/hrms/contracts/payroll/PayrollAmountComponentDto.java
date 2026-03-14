package com.company.hrms.contracts.payroll;

import java.math.BigDecimal;

public record PayrollAmountComponentDto(
        String code,
        String name,
        BigDecimal amount
) {
}
