package com.company.hrms.recruitment.api;

import java.util.UUID;

public record CreateCandidateCommand(
        UUID personId,
        String candidateCode,
        String firstName,
        String lastName,
        String email,
        String jobPostingCode
) {
}
