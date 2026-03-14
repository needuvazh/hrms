package com.company.hrms.pasi.domain;

import com.company.hrms.payroll.api.PayrollEmployeeRecordView;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class PasiCalculator {

    public CalculationResult calculate(PasiContributionRule rule, PayrollEmployeeRecordView payrollEmployeeRecord) {
        BigDecimal gross = payrollEmployeeRecord.grossAmount();
        BigDecimal contributable = rule.salaryCap() == null
                ? gross
                : gross.min(rule.salaryCap());

        BigDecimal employee = contributable
                .multiply(rule.employeeRatePercent())
                .divide(new BigDecimal("100"), 3, RoundingMode.HALF_UP)
                .setScale(3, RoundingMode.HALF_UP);

        BigDecimal employer = contributable
                .multiply(rule.employerRatePercent())
                .divide(new BigDecimal("100"), 3, RoundingMode.HALF_UP)
                .setScale(3, RoundingMode.HALF_UP);

        return new CalculationResult(
                contributable.setScale(3, RoundingMode.HALF_UP),
                employee,
                employer,
                employee.add(employer).setScale(3, RoundingMode.HALF_UP));
    }

    public record CalculationResult(
            BigDecimal contributableSalary,
            BigDecimal employeeContribution,
            BigDecimal employerContribution,
            BigDecimal totalContribution
    ) {
    }
}
