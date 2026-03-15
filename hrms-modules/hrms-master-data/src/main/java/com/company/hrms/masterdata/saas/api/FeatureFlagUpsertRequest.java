package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotBlank;

public record FeatureFlagUpsertRequest(
        @NotBlank String featureKey,
        @NotBlank String featureName,
        String description,
        Boolean active
) {
}
