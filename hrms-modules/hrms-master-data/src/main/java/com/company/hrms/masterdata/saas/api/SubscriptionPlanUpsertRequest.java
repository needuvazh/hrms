package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record SubscriptionPlanUpsertRequest(
        @NotBlank String planCode,
        @NotBlank String planName,
        String description,
        @PositiveOrZero Integer maxUsers,
        @PositiveOrZero Integer maxStorageGb,
        @PositiveOrZero BigDecimal monthlyPrice,
        @PositiveOrZero BigDecimal annualPrice,
        String currencyCode,
        Boolean active
) {
}
