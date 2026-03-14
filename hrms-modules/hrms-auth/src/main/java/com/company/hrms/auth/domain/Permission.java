package com.company.hrms.auth.domain;

import java.util.UUID;

public record Permission(
        UUID id,
        String tenantId,
        String code,
        String name
) {
}
