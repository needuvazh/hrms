package com.company.hrms.auth.domain;

import java.util.UUID;

public record Role(
        UUID id,
        String tenantId,
        String code,
        String name
) {
}
