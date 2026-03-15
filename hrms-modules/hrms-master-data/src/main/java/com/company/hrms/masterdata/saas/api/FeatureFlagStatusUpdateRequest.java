package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotNull;

public record FeatureFlagStatusUpdateRequest(
        @NotNull Boolean active
) {
}
