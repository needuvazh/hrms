package com.company.hrms.recruitment.api;

import java.time.Instant;
import java.util.UUID;

public record CandidateView(
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
