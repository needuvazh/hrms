package com.company.hrms.document.domain;

import java.time.LocalDate;

public record ExpiryDate(LocalDate value) {

    public static ExpiryDate of(LocalDate value) {
        return new ExpiryDate(value);
    }

    public static ExpiryDate empty() {
        return new ExpiryDate(null);
    }

    public boolean exists() {
        return value != null;
    }

    public boolean isOnOrBefore(LocalDate date) {
        return value != null && (value.isBefore(date) || value.isEqual(date));
    }
}
