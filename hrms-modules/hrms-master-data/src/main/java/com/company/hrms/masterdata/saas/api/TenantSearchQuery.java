package com.company.hrms.masterdata.saas.api;

public record TenantSearchQuery(
        String q,
        Boolean active,
        int page,
        int size,
        String sort,
        boolean all
) {
}
