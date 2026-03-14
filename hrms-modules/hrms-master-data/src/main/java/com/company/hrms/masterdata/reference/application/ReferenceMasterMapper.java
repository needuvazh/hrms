package com.company.hrms.masterdata.reference.application;

import com.company.hrms.masterdata.reference.api.ReferenceMasterViewDto;
import com.company.hrms.masterdata.reference.domain.DocumentFor;
import com.company.hrms.masterdata.reference.domain.ReferenceMasterRow;
import org.springframework.stereotype.Component;

@Component
public class ReferenceMasterMapper {

    public ReferenceMasterViewDto toView(ReferenceMasterRow row) {
        return new ReferenceMasterViewDto(
                row.id(),
                row.code(),
                row.name(),
                row.shortName(),
                row.iso2Code(),
                row.iso3Code(),
                row.phoneCode(),
                row.nationalityName(),
                row.defaultCurrencyCode(),
                row.defaultTimezone(),
                row.gccFlag(),
                row.decimalPlaces(),
                row.nativeName(),
                row.rtlEnabled(),
                row.countryCode(),
                row.gccNationalFlag(),
                row.omaniFlag(),
                row.displayOrder(),
                row.dependentAllowed(),
                row.emergencyContactAllowed(),
                row.beneficiaryAllowed(),
                row.shortDescription(),
                row.documentFor() == null ? null : DocumentFor.valueOf(row.documentFor()),
                row.issueDateRequired(),
                row.expiryDateRequired(),
                row.alertRequired(),
                row.alertDaysBefore(),
                row.rankingOrder(),
                row.expiryTrackingRequired(),
                row.issuingBodyRequired(),
                row.description(),
                row.skillCategoryId(),
                row.skillCategoryName(),
                row.active(),
                row.createdAt(),
                row.updatedAt(),
                row.createdBy(),
                row.updatedBy());
    }
}
