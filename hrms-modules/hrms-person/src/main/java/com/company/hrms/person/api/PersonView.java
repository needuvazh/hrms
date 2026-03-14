package com.company.hrms.person.api;

import java.time.Instant;
import java.util.UUID;

public record PersonView(
        UUID id,
        String tenantId,
        String personCode,
        String firstName,
        String lastName,
        String email,
        String mobile,
        String countryCode,
        String nationalityCode,
        Instant createdAt,
        Instant updatedAt
) {
}
