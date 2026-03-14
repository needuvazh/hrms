package com.company.hrms.pasi.model;

import com.company.hrms.contracts.payroll.PayrollEmployeeRecordViewDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class PasiCalculator {

    public CalculationResultDto calculate(PasiContributionRuleDto rule, PayrollEmployeeRecordViewDto payrollEmployeeRecord) {
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

        return new CalculationResultDto(
                contributable.setScale(3, RoundingMode.HALF_UP),
                employee,
                employer,
                employee.add(employer).setScale(3, RoundingMode.HALF_UP));
    }

    public record CalculationResultDto(
            BigDecimal contributableSalary,
            BigDecimal employeeContribution,
            BigDecimal employerContribution,
            BigDecimal totalContribution
    ) {
    }
}
