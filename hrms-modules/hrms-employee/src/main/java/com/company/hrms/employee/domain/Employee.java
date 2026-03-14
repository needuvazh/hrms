package com.company.hrms.employee.domain;

import java.time.Instant;
import java.util.UUID;

public record Employee(
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

    public Employee(
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
