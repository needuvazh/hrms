package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotNull;

public record TenantStatusUpdateRequest(
        @NotNull Boolean active
) {
}
