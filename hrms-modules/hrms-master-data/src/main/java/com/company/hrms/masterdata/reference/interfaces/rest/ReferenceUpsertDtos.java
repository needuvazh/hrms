package com.company.hrms.masterdata.reference.interfaces.rest;

import com.company.hrms.masterdata.reference.api.ReferenceMasterUpsertRequest;
import com.company.hrms.masterdata.reference.domain.DocumentFor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
abstract class AbstractReferenceUpsertRequestDto {

    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private String shortName;
    private String iso2Code;
    private String iso3Code;
    private String phoneCode;
    private String nationalityName;
    private String defaultCurrencyCode;
    private String defaultTimezone;
    private Boolean gccFlag;
    @PositiveOrZero
    private Integer decimalPlaces;
    private String nativeName;
    private Boolean rtlEnabled;
    private String countryCode;
    private Boolean gccNationalFlag;
    private Boolean omaniFlag;
    @PositiveOrZero
    private Integer displayOrder;
    private Boolean dependentAllowed;
    private Boolean emergencyContactAllowed;
    private Boolean beneficiaryAllowed;
    private String shortDescription;
    private DocumentFor documentFor;
    private Boolean issueDateRequired;
    private Boolean expiryDateRequired;
    private Boolean alertRequired;
    @PositiveOrZero
    private Integer alertDaysBefore;
    @PositiveOrZero
    private Integer rankingOrder;
    private Boolean expiryTrackingRequired;
    private Boolean issuingBodyRequired;
    private String description;
    private UUID skillCategoryId;
    private Boolean active;

    ReferenceMasterUpsertRequest toReferenceRequest() {
        return new ReferenceMasterUpsertRequest(
                code,
                name,
                shortName,
                iso2Code,
                iso3Code,
                phoneCode,
                nationalityName,
                defaultCurrencyCode,
                defaultTimezone,
                gccFlag,
                decimalPlaces,
                nativeName,
                rtlEnabled,
                countryCode,
                gccNationalFlag,
                omaniFlag,
                displayOrder,
                dependentAllowed,
                emergencyContactAllowed,
                beneficiaryAllowed,
                shortDescription,
                documentFor,
                issueDateRequired,
                expiryDateRequired,
                alertRequired,
                alertDaysBefore,
                rankingOrder,
                expiryTrackingRequired,
                issuingBodyRequired,
                description,
                skillCategoryId,
                active);
    }
}

final class CountriesUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

@Getter
@Setter
final class CurrenciesUpsertRequestDto extends AbstractReferenceUpsertRequestDto {

    private String symbol;

    @Override
    ReferenceMasterUpsertRequest toReferenceRequest() {
        if (StringUtils.hasText(symbol)) {
            setShortName(symbol);
        }
        return super.toReferenceRequest();
    }
}

final class LanguagesUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class NationalitiesUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class ReligionsUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class GendersUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class MaritalStatusesUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class RelationshipTypesUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class DocumentTypesUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class EducationLevelsUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class CertificationTypesUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class SkillCategoriesUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}

final class SkillsUpsertRequestDto extends AbstractReferenceUpsertRequestDto {}
