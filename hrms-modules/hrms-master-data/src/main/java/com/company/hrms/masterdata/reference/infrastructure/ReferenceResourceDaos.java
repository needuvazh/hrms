package com.company.hrms.masterdata.reference.infrastructure;

import com.company.hrms.masterdata.reference.api.ReferenceMasterUpsertRequest;
import com.company.hrms.masterdata.reference.api.ReferenceOptionViewDto;
import com.company.hrms.masterdata.reference.api.ReferenceSearchQuery;
import com.company.hrms.masterdata.reference.domain.ReferenceMasterRow;
import com.company.hrms.masterdata.reference.domain.ReferenceResource;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

abstract class AbstractReferenceResourceDao implements ReferenceResourceDao {

    private final ReferenceMasterRepository repository;
    private final ReferenceResource resource;

    protected AbstractReferenceResourceDao(ReferenceMasterRepository repository, ReferenceResource resource) {
        this.repository = repository;
        this.resource = resource;
    }

    @Override
    public Mono<ReferenceMasterRow> create(ReferenceMasterUpsertRequest request, String actor) {
        return repository.create(resource, request, actor);
    }

    @Override
    public Mono<ReferenceMasterRow> update(UUID id, ReferenceMasterUpsertRequest request, String actor) {
        return repository.update(resource, id, request, actor);
    }

    @Override
    public Mono<ReferenceMasterRow> findById(UUID id) {
        return repository.findById(resource, id);
    }

    @Override
    public Flux<ReferenceMasterRow> list(ReferenceSearchQuery query) {
        return repository.list(resource, query);
    }

    @Override
    public Mono<Long> count(ReferenceSearchQuery query) {
        return repository.count(resource, query);
    }

    @Override
    public Mono<Void> updateStatus(UUID id, boolean active, String actor) {
        return repository.updateStatus(resource, id, active, actor);
    }

    @Override
    public Flux<ReferenceOptionViewDto> options(boolean activeOnly) {
        return repository.options(resource, activeOnly);
    }

    @Override
    public Mono<Boolean> existsById(UUID id) {
        return repository.existsById(resource, id);
    }

    @Override
    public Mono<Boolean> existsCode(String code, UUID excludeId) {
        return repository.existsCode(resource, code, excludeId);
    }

    @Override
    public Mono<Boolean> existsName(String name, UUID excludeId) {
        return repository.existsName(resource, name, excludeId);
    }

    @Override
    public Mono<Boolean> existsCurrencyCode(String currencyCode) {
        return repository.existsCurrencyCode(currencyCode);
    }

    @Override
    public Mono<Boolean> existsCountryCode(String countryCode) {
        return repository.existsCountryCode(countryCode);
    }

    @Override
    public Mono<Boolean> existsSkillCategoryById(UUID id) {
        return repository.existsById(ReferenceResource.SKILL_CATEGORIES, id);
    }

    @Override
    public Mono<String> resolveCurrencyCode(String currencyToken) {
        return repository.resolveCurrencyCode(currencyToken);
    }
}

@Repository("countriesReferenceDao")
class CountriesReferenceDao extends AbstractReferenceResourceDao {
    CountriesReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.COUNTRIES); }
}

@Repository("currenciesReferenceDao")
class CurrenciesReferenceDao extends AbstractReferenceResourceDao {
    CurrenciesReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.CURRENCIES); }
}

@Repository("languagesReferenceDao")
class LanguagesReferenceDao extends AbstractReferenceResourceDao {
    LanguagesReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.LANGUAGES); }
}

@Repository("nationalitiesReferenceDao")
class NationalitiesReferenceDao extends AbstractReferenceResourceDao {
    NationalitiesReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.NATIONALITIES); }
}

@Repository("religionsReferenceDao")
class ReligionsReferenceDao extends AbstractReferenceResourceDao {
    ReligionsReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.RELIGIONS); }
}

@Repository("gendersReferenceDao")
class GendersReferenceDao extends AbstractReferenceResourceDao {
    GendersReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.GENDERS); }
}

@Repository("maritalStatusesReferenceDao")
class MaritalStatusesReferenceDao extends AbstractReferenceResourceDao {
    MaritalStatusesReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.MARITAL_STATUSES); }
}

@Repository("relationshipTypesReferenceDao")
class RelationshipTypesReferenceDao extends AbstractReferenceResourceDao {
    RelationshipTypesReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.RELATIONSHIP_TYPES); }
}

@Repository("documentTypesReferenceDao")
class DocumentTypesReferenceDao extends AbstractReferenceResourceDao {
    DocumentTypesReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.DOCUMENT_TYPES); }
}

@Repository("educationLevelsReferenceDao")
class EducationLevelsReferenceDao extends AbstractReferenceResourceDao {
    EducationLevelsReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.EDUCATION_LEVELS); }
}

@Repository("certificationTypesReferenceDao")
class CertificationTypesReferenceDao extends AbstractReferenceResourceDao {
    CertificationTypesReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.CERTIFICATION_TYPES); }
}

@Repository("skillCategoriesReferenceDao")
class SkillCategoriesReferenceDao extends AbstractReferenceResourceDao {
    SkillCategoriesReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.SKILL_CATEGORIES); }
}

@Repository("skillsReferenceDao")
class SkillsReferenceDao extends AbstractReferenceResourceDao {
    SkillsReferenceDao(ReferenceMasterRepository repository) { super(repository, ReferenceResource.SKILLS); }
}
