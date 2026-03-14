package com.company.hrms.recruitment.api;

import java.util.UUID;

public record UpdateCandidateStatusCommand(
        UUID candidateId,
        CandidateStatus status,
        String reason
) {
}
