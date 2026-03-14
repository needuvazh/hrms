package com.company.hrms.payroll.model;

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
public class ReviewPayrollRunCommandDto {
    private final UUID payrollRunId;
    private final PayrollApprovalDecision decision;
    private final String actor;
    private final String comments;
}
