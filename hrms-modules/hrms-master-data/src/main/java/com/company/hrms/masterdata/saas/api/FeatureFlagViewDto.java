package com.company.hrms.masterdata.saas.api;

import java.time.Instant;
import java.util.UUID;

public record FeatureFlagViewDto(
        UUID id,
        String featureKey,
        String featureName,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
