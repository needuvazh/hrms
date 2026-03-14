package com.company.hrms.employee.api;

public record EmployeeSearchQuery(
        String query,
        int limit,
        int offset
) {
}
