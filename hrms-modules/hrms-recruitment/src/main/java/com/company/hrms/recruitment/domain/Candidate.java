package com.company.hrms.recruitment.domain;

import com.company.hrms.recruitment.api.CandidateStatus;
import java.time.Instant;
import java.util.UUID;

public record Candidate(
        UUID id,
        String tenantId,
        UUID personId,
        String candidateCode,
        String firstName,
        String lastName,
        String email,
        String jobPostingCode,
        CandidateStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
