package com.company.hrms.masterdata.reference.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum ReferenceResource {
    COUNTRIES("countries", "master_data.countries", "country_code", "country_name", Set.of("country_code", "country_name", "iso2_code", "iso3_code", "native_name"), Set.of("country_code", "country_name", "iso2_code", "iso3_code", "created_at", "updated_at")),
    CURRENCIES("currencies", "master_data.currencies", "currency_code", "currency_name", Set.of("currency_code", "currency_name"), Set.of("currency_code", "currency_name", "created_at", "updated_at")),
    LANGUAGES("languages", "master_data.languages", "language_code", "language_name", Set.of("language_code", "language_name", "native_name"), Set.of("language_code", "language_name", "created_at", "updated_at")),
    NATIONALITIES("nationalities", "master_data.nationalities", "nationality_code", "nationality_name", Set.of("nationality_code", "nationality_name", "country_code"), Set.of("nationality_code", "nationality_name", "created_at", "updated_at")),
    RELIGIONS("religions", "master_data.religions", "religion_code", "religion_name", Set.of("religion_code", "religion_name"), Set.of("religion_code", "religion_name", "created_at", "updated_at")),
    GENDERS("genders", "master_data.genders", "gender_code", "gender_name", Set.of("gender_code", "gender_name"), Set.of("gender_code", "gender_name", "display_order", "created_at", "updated_at")),
    MARITAL_STATUSES("marital-statuses", "master_data.marital_statuses", "marital_status_code", "marital_status_name", Set.of("marital_status_code", "marital_status_name"), Set.of("marital_status_code", "marital_status_name", "created_at", "updated_at")),
    RELATIONSHIP_TYPES("relationship-types", "master_data.relationship_types", "relationship_type_code", "relationship_type_name", Set.of("relationship_type_code", "relationship_type_name"), Set.of("relationship_type_code", "relationship_type_name", "created_at", "updated_at")),
    DOCUMENT_TYPES("document-types", "master_data.document_types", "document_type_code", "document_type_name", Set.of("document_type_code", "document_type_name", "short_description"), Set.of("document_type_code", "document_type_name", "document_for", "created_at", "updated_at")),
    EDUCATION_LEVELS("education-levels", "master_data.education_levels", "education_level_code", "education_level_name", Set.of("education_level_code", "education_level_name"), Set.of("education_level_code", "education_level_name", "ranking_order", "created_at", "updated_at")),
    CERTIFICATION_TYPES("certification-types", "master_data.certification_types", "certification_type_code", "certification_type_name", Set.of("certification_type_code", "certification_type_name"), Set.of("certification_type_code", "certification_type_name", "created_at", "updated_at")),
    SKILL_CATEGORIES("skill-categories", "master_data.skill_categories", "skill_category_code", "skill_category_name", Set.of("skill_category_code", "skill_category_name", "description"), Set.of("skill_category_code", "skill_category_name", "created_at", "updated_at")),
    SKILLS("skills", "master_data.skills", "skill_code", "skill_name", Set.of("skill_code", "skill_name", "description"), Set.of("skill_code", "skill_name", "created_at", "updated_at"));

    private static final Map<String, ReferenceResource> BY_PATH = Arrays.stream(values())
            .collect(Collectors.toMap(ReferenceResource::path, Function.identity()));

    private final String path;
    private final String tableName;
    private final String codeColumn;
    private final String nameColumn;
    private final Set<String> searchColumns;
    private final Set<String> sortableColumns;

    ReferenceResource(
            String path,
            String tableName,
            String codeColumn,
            String nameColumn,
            Set<String> searchColumns,
            Set<String> sortableColumns
    ) {
        this.path = path;
        this.tableName = tableName;
        this.codeColumn = codeColumn;
        this.nameColumn = nameColumn;
        this.searchColumns = searchColumns;
        this.sortableColumns = sortableColumns;
    }

    public String path() {
        return path;
    }

    public String tableName() {
        return tableName;
    }

    public String codeColumn() {
        return codeColumn;
    }

    public String nameColumn() {
        return nameColumn;
    }

    public Set<String> searchColumns() {
        return searchColumns;
    }

    public Set<String> sortableColumns() {
        return sortableColumns;
    }

    public static ReferenceResource fromPath(String path) {
        ReferenceResource resource = BY_PATH.get(path);
        if (resource == null) {
            throw new IllegalArgumentException("Unsupported reference resource: " + path);
        }
        return resource;
    }
}
