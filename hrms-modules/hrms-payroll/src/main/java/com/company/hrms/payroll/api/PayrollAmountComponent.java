package com.company.hrms.payroll.api;

import java.math.BigDecimal;

public record PayrollAmountComponent(
        String code,
        String name,
        BigDecimal amount
) {
}
