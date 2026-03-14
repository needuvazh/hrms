package com.company.hrms.masterdata.api;

public record LookupValueView(
        String lookupType,
        String lookupCode,
        String lookupLabel,
        int sortOrder
) {
}
