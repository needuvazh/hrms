package com.company.hrms.wps.domain;

import com.company.hrms.payroll.api.PayslipView;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WpsPayrollValidator {

    public WpsValidationResult validate(List<PayslipView> payslips) {
        List<String> errors = new ArrayList<>();

        if (payslips == null || payslips.isEmpty()) {
            errors.add("No payslips available for WPS export");
        } else {
            for (PayslipView payslip : payslips) {
                if (payslip.payrollEmployeeRecord() == null) {
                    errors.add("Payslip without payroll record");
                    continue;
                }
                if (payslip.payrollEmployeeRecord().employeeId() == null) {
                    errors.add("Missing employee id for payslip");
                }
                if (payslip.payrollEmployeeRecord().netAmount() == null
                        || payslip.payrollEmployeeRecord().netAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add("Invalid net amount for employee " + payslip.payrollEmployeeRecord().employeeId());
                }
            }
        }

        boolean valid = errors.isEmpty();
        String summary = valid
                ? "Validated " + (payslips == null ? 0 : payslips.size()) + " entries"
                : String.join("; ", errors);

        return new WpsValidationResult(valid, summary, List.copyOf(errors));
    }
}
