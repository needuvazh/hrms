package com.company.hrms.recruitment.api;

public record CandidateSearchQuery(
        String query,
        CandidateStatus status,
        int limit,
        int offset
) {
}
