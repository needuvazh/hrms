package com.company.hrms.payroll.model;

import java.time.LocalDate;
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
public class DefinePayrollPeriodCommandDto {
    private final String periodCode;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final String description;
}
