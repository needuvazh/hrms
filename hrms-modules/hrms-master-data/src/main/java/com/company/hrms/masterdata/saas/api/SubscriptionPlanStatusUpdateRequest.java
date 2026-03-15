package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotNull;

public record SubscriptionPlanStatusUpdateRequest(
        @NotNull Boolean active
) {
}
