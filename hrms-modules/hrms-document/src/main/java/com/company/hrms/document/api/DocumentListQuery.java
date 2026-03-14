package com.company.hrms.document.api;

public record DocumentListQuery(
        String entityType,
        String entityId,
        boolean includeArchived
) {
}
