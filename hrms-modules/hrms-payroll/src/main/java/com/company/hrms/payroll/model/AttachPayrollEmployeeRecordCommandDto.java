package com.company.hrms.payroll.model;

import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@AllArgsConstructor
@Accessors(fluent = true)
@EqualsAndHashCode
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AttachPayrollEmployeeRecordCommandDto {
    private final UUID payrollRunId;
    private final UUID employeeId;
    private final List<PayrollAmountComponentDto> earnings;
    private final List<PayrollAmountComponentDto> deductions;
    private final String remarks;
}
