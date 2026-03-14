package com.company.hrms.employee.model;

import java.time.Instant;
import java.util.UUID;

public record EmployeeViewDto(
        UUID id,
        String tenantId,
        String employeeCode,
        UUID personId,
        String firstName,
        String lastName,
        String email,
        String departmentCode,
        String jobTitle,
        Instant createdAt,
        Instant updatedAt
) {

    public EmployeeViewDto(
            UUID id,
            String tenantId,
            String employeeCode,
            String firstName,
            String lastName,
            String email,
            String departmentCode,
            String jobTitle,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(id, tenantId, employeeCode, null, firstName, lastName, email, departmentCode, jobTitle, createdAt, updatedAt);
    }
}
