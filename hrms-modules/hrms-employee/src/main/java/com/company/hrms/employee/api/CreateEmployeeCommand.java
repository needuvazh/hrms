package com.company.hrms.employee.api;

import java.util.UUID;

public record CreateEmployeeCommand(
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String departmentCode,
        String jobTitle,
        UUID personId
) {

    public CreateEmployeeCommand(
            String employeeCode,
            String firstName,
            String lastName,
            String email,
            String departmentCode,
            String jobTitle
    ) {
        this(employeeCode, firstName, lastName, email, departmentCode, jobTitle, null);
    }
}
