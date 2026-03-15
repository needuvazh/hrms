package com.company.hrms.masterdata.saas.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SubscriptionPlanViewDto(
        UUID id,
        String planCode,
        String planName,
        String description,
        Integer maxUsers,
        Integer maxStorageGb,
        BigDecimal monthlyPrice,
        BigDecimal annualPrice,
        String currencyCode,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
