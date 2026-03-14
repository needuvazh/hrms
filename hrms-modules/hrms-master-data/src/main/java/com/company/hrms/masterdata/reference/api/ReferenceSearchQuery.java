package com.company.hrms.masterdata.reference.api;

import java.util.UUID;

public record ReferenceSearchQuery(
        String q,
        Boolean active,
        int page,
        int size,
        String sort,
        UUID skillCategoryId
) {
}
