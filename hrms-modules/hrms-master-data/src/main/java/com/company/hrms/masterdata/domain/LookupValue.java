package com.company.hrms.masterdata.domain;

public record LookupValue(
        String lookupType,
        String lookupCode,
        String lookupLabel,
        int sortOrder
) {
}
