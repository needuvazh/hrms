package com.company.hrms.contracts.document;

import java.time.LocalDate;

public record ExpiryDateDto(LocalDate value) {

    public static ExpiryDateDto of(LocalDate value) {
        return new ExpiryDateDto(value);
    }

    public static ExpiryDateDto empty() {
        return new ExpiryDateDto(null);
    }

    public boolean exists() {
        return value != null;
    }

    public boolean isOnOrBefore(LocalDate date) {
        return value != null && (value.isBefore(date) || value.isEqual(date));
    }
}
