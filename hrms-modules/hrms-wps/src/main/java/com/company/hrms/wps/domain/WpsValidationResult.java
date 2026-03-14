package com.company.hrms.wps.domain;

import java.util.List;

public record WpsValidationResult(
        boolean valid,
        String summary,
        List<String> errors
) {
}
