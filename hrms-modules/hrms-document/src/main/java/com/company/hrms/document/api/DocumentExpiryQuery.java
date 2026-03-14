package com.company.hrms.document.api;

import java.time.LocalDate;

public record DocumentExpiryQuery(
        LocalDate fromDate,
        LocalDate toDate,
        boolean includeArchived
) {
}
