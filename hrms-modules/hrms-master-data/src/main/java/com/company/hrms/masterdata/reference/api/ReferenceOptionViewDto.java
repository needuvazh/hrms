package com.company.hrms.masterdata.reference.api;

import java.util.UUID;

public record ReferenceOptionViewDto(
        UUID id,
        String code,
        String name
) {
}
