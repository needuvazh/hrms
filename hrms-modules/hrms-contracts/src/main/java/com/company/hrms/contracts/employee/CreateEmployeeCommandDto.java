package com.company.hrms.contracts.employee;

import java.util.UUID;

public record CreateEmployeeCommandDto(
        String employeeCode,
        String firstName,
        String lastName,
        String email,
        String departmentCode,
        String jobTitle,
        UUID personId
) {

    public CreateEmployeeCommandDto(
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
