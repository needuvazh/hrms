package com.company.hrms.wps.model;

import com.company.hrms.payroll.model.PayslipViewDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WpsPayrollValidator {

    public WpsValidationResultDto validate(List<PayslipViewDto> payslips) {
        List<String> errors = new ArrayList<>();

        if (payslips == null || payslips.isEmpty()) {
            errors.add("No payslips available for WPS export");
        } else {
            for (PayslipViewDto payslip : payslips) {
                if (payslip.payrollEmployeeRecord() == null) {
                    errors.add("PayslipDto without payroll record");
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

        return new WpsValidationResultDto(valid, summary, List.copyOf(errors));
    }
}
