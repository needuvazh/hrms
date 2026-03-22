package com.company.hrms.masterdata.reference.interfaces.rest;

import com.company.hrms.masterdata.reference.api.ReferenceMasterViewDto;
import java.time.Instant;
import java.util.UUID;

record CountriesViewDto(
        UUID id,
        String code,
        String countryCode,
        String name,
        String shortName,
        String iso2Code,
        String iso3Code,
        String phoneCode,
        String nationalityName,
        String defaultCurrencyCode,
        String defaultTimezone,
        String nativeName,
        String description,
        Boolean gccFlag,
        Boolean rtlEnabled,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}

record CurrenciesViewDto(
        UUID id,
        String code,
        String name,
        String symbol,
        String shortDescription,
        String description,
        Integer decimalPlaces,
        boolean active,
        Instant updatedAt
) {}

record LanguagesViewDto(
        UUID id,
        String code,
        String name,
        String nativeName,
        String shortDescription,
        String description,
        Boolean rtlEnabled,
        boolean active,
        Instant updatedAt
) {}

record NationalitiesViewDto(UUID id, String code, String name, String countryCode, Boolean gccNationalFlag, Boolean omaniFlag, boolean active, Instant updatedAt) {}

record ReligionsViewDto(UUID id, String code, String name, boolean active, Instant updatedAt) {}

record GendersViewDto(UUID id, String code, String name, Integer displayOrder, boolean active, Instant updatedAt) {}

record MaritalStatusesViewDto(UUID id, String code, String name, boolean active, Instant updatedAt) {}

record RelationshipTypesViewDto(UUID id, String code, String name, Boolean dependentAllowed, Boolean emergencyContactAllowed, Boolean beneficiaryAllowed, boolean active, Instant updatedAt) {}

record DocumentTypesViewDto(UUID id, String code, String name, String shortDescription, String documentFor, Boolean issueDateRequired, Boolean expiryDateRequired, Boolean alertRequired, Integer alertDaysBefore, boolean active, Instant updatedAt) {}

record EducationLevelsViewDto(UUID id, String code, String name, Integer rankingOrder, boolean active, Instant updatedAt) {}

record CertificationTypesViewDto(UUID id, String code, String name, Boolean expiryTrackingRequired, Boolean issuingBodyRequired, boolean active, Instant updatedAt) {}

record SkillCategoriesViewDto(UUID id, String code, String name, String description, boolean active, Instant updatedAt) {}

record SkillsViewDto(UUID id, String code, String name, UUID skillCategoryId, String skillCategoryName, String description, boolean active, Instant updatedAt) {}

final class ReferenceViewDtoMapper {

    private ReferenceViewDtoMapper() {}

    static CountriesViewDto toCountries(ReferenceMasterViewDto v) {
        return new CountriesViewDto(
                v.id(),
                v.code(),
                v.countryCode(),
                v.name(),
                v.shortName(),
                v.iso2Code(),
                v.iso3Code(),
                v.phoneCode(),
                v.nationalityName(),
                v.defaultCurrencyCode(),
                v.defaultTimezone(),
                v.nativeName(),
                v.description(),
                v.gccFlag(),
                v.rtlEnabled(),
                v.active(),
                v.createdAt(),
                v.updatedAt());
    }

    static CurrenciesViewDto toCurrencies(ReferenceMasterViewDto v) {
        return new CurrenciesViewDto(
                v.id(),
                v.code(),
                v.name(),
                v.shortName(),
                v.shortDescription(),
                v.description(),
                v.decimalPlaces(),
                v.active(),
                v.updatedAt());
    }

    static LanguagesViewDto toLanguages(ReferenceMasterViewDto v) {
        return new LanguagesViewDto(
                v.id(),
                v.code(),
                v.name(),
                v.nativeName(),
                v.shortDescription(),
                v.description(),
                v.rtlEnabled(),
                v.active(),
                v.updatedAt());
    }

    static NationalitiesViewDto toNationalities(ReferenceMasterViewDto v) {
        return new NationalitiesViewDto(v.id(), v.code(), v.name(), v.countryCode(), v.gccNationalFlag(), v.omaniFlag(), v.active(), v.updatedAt());
    }

    static ReligionsViewDto toReligions(ReferenceMasterViewDto v) {
        return new ReligionsViewDto(v.id(), v.code(), v.name(), v.active(), v.updatedAt());
    }

    static GendersViewDto toGenders(ReferenceMasterViewDto v) {
        return new GendersViewDto(v.id(), v.code(), v.name(), v.displayOrder(), v.active(), v.updatedAt());
    }

    static MaritalStatusesViewDto toMaritalStatuses(ReferenceMasterViewDto v) {
        return new MaritalStatusesViewDto(v.id(), v.code(), v.name(), v.active(), v.updatedAt());
    }

    static RelationshipTypesViewDto toRelationshipTypes(ReferenceMasterViewDto v) {
        return new RelationshipTypesViewDto(v.id(), v.code(), v.name(), v.dependentAllowed(), v.emergencyContactAllowed(), v.beneficiaryAllowed(), v.active(), v.updatedAt());
    }

    static DocumentTypesViewDto toDocumentTypes(ReferenceMasterViewDto v) {
        return new DocumentTypesViewDto(v.id(), v.code(), v.name(), v.shortDescription(), v.documentFor() == null ? null : v.documentFor().name(), v.issueDateRequired(), v.expiryDateRequired(), v.alertRequired(), v.alertDaysBefore(), v.active(), v.updatedAt());
    }

    static EducationLevelsViewDto toEducationLevels(ReferenceMasterViewDto v) {
        return new EducationLevelsViewDto(v.id(), v.code(), v.name(), v.rankingOrder(), v.active(), v.updatedAt());
    }

    static CertificationTypesViewDto toCertificationTypes(ReferenceMasterViewDto v) {
        return new CertificationTypesViewDto(v.id(), v.code(), v.name(), v.expiryTrackingRequired(), v.issuingBodyRequired(), v.active(), v.updatedAt());
    }

    static SkillCategoriesViewDto toSkillCategories(ReferenceMasterViewDto v) {
        return new SkillCategoriesViewDto(v.id(), v.code(), v.name(), v.description(), v.active(), v.updatedAt());
    }

    static SkillsViewDto toSkills(ReferenceMasterViewDto v) {
        return new SkillsViewDto(v.id(), v.code(), v.name(), v.skillCategoryId(), v.skillCategoryName(), v.description(), v.active(), v.updatedAt());
    }
}
