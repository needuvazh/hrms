package com.company.hrms.recruitment.api;

import java.util.UUID;

public record HireCandidateCommand(
        UUID candidateId,
        String employeeCode,
        String departmentCode,
        String jobTitle
) {
}
