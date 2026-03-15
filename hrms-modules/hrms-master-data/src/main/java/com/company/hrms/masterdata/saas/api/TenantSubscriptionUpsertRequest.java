package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record TenantSubscriptionUpsertRequest(
        @NotNull UUID subscriptionPlanId,
        @NotNull LocalDate subscriptionStartDate,
        LocalDate subscriptionEndDate,
        Boolean autoRenew,
        Boolean active
) {
}
