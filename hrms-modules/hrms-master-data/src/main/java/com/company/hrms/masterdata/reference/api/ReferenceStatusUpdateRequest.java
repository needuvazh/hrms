package com.company.hrms.masterdata.reference.api;

import jakarta.validation.constraints.NotNull;

public record ReferenceStatusUpdateRequest(
        @NotNull Boolean active
) {
}
