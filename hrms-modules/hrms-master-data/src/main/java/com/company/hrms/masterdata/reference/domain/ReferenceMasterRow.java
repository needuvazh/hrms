package com.company.hrms.masterdata.reference.domain;

import java.time.Instant;
import java.util.UUID;

public record ReferenceMasterRow(
        UUID id,
        String code,
        String name,
        String shortName,
        String iso2Code,
        String iso3Code,
        String phoneCode,
        String nationalityName,
        String defaultCurrencyCode,
        String defaultTimezone,
        Boolean gccFlag,
        Integer decimalPlaces,
        String nativeName,
        Boolean rtlEnabled,
        String countryCode,
        Boolean gccNationalFlag,
        Boolean omaniFlag,
        Integer displayOrder,
        Boolean dependentAllowed,
        Boolean emergencyContactAllowed,
        Boolean beneficiaryAllowed,
        String shortDescription,
        String documentFor,
        Boolean issueDateRequired,
        Boolean expiryDateRequired,
        Boolean alertRequired,
        Integer alertDaysBefore,
        Integer rankingOrder,
        Boolean expiryTrackingRequired,
        Boolean issuingBodyRequired,
        String description,
        UUID skillCategoryId,
        String skillCategoryName,
        boolean active,
        Instant createdAt,
        Instant updatedAt,
        String createdBy,
        String updatedBy
) {
}
