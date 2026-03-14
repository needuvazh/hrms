package com.company.hrms.employee.api;

import java.time.Instant;
import java.util.UUID;

public record EmployeeView(
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

    public EmployeeView(
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
