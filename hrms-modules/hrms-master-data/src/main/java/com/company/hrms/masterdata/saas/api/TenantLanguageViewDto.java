package com.company.hrms.masterdata.saas.api;

import java.time.Instant;
import java.util.UUID;

public record TenantLanguageViewDto(
        UUID id,
        String tenantCode,
        String languageCode,
        String languageName,
        boolean defaultLanguage,
        boolean active,
        Integer displayOrder,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
