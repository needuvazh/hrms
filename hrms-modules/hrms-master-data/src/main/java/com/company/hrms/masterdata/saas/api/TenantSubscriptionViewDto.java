package com.company.hrms.masterdata.saas.api;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record TenantSubscriptionViewDto(
        String tenantCode,
        UUID subscriptionPlanId,
        String subscriptionPlanCode,
        String subscriptionPlanName,
        LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate,
        boolean autoRenew,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
