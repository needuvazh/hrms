package com.company.hrms.masterdata.saas.api;

import java.util.UUID;

public record FeatureFlagOptionViewDto(
        UUID id,
        String featureKey,
        String featureName
) {
}
