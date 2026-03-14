package com.company.hrms.person.api;

public record PersonSearchQuery(
        String query,
        int limit,
        int offset
) {
}
