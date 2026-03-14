package com.company.hrms.masterdata.reference.api;

import com.company.hrms.masterdata.reference.domain.DocumentFor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;

public record ReferenceMasterUpsertRequest(
        @NotBlank String code,
        @NotBlank String name,
        String shortName,
        String iso2Code,
        String iso3Code,
        String phoneCode,
        String nationalityName,
        String defaultCurrencyCode,
        String defaultTimezone,
        Boolean gccFlag,
        @PositiveOrZero Integer decimalPlaces,
        String nativeName,
        Boolean rtlEnabled,
        String countryCode,
        Boolean gccNationalFlag,
        Boolean omaniFlag,
        @PositiveOrZero Integer displayOrder,
        Boolean dependentAllowed,
        Boolean emergencyContactAllowed,
        Boolean beneficiaryAllowed,
        String shortDescription,
        DocumentFor documentFor,
        Boolean issueDateRequired,
        Boolean expiryDateRequired,
        Boolean alertRequired,
        @PositiveOrZero Integer alertDaysBefore,
        @PositiveOrZero Integer rankingOrder,
        Boolean expiryTrackingRequired,
        Boolean issuingBodyRequired,
        String description,
        UUID skillCategoryId,
        Boolean active
) {
}
