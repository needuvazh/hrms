package com.company.hrms.masterdata.saas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public record TenantLanguageUpsertRequest(
        @NotBlank String languageCode,
        Boolean defaultLanguage,
        Boolean active,
        @PositiveOrZero Integer displayOrder
) {
}
