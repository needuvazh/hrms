package com.company.hrms.person.service.impl;

import com.company.hrms.person.model.*;
import com.company.hrms.person.repository.*;
import com.company.hrms.person.service.*;

import com.company.hrms.person.model.CreatePersonCommandDto;
import com.company.hrms.person.service.PersonModuleApi;
import com.company.hrms.person.model.PersonSearchQueryDto;
import com.company.hrms.person.model.PersonViewDto;
import com.company.hrms.person.model.PersonDto;
import com.company.hrms.person.repository.PersonRepository;
import com.company.hrms.platform.audit.api.AuditEvent;
import com.company.hrms.platform.audit.api.AuditEventPublisher;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.outbox.api.OutboxEvent;
import com.company.hrms.platform.outbox.api.OutboxPublisher;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.api.TenantContextAccessor;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class PersonApplicationService implements PersonModuleApi {

    private static final int DEFAULT_LIMIT = 50;

    private final PersonRepository personRepository;
    private final TenantContextAccessor tenantContextAccessor;
    private final EnablementGuard enablementGuard;
    private final AuditEventPublisher auditEventPublisher;
    private final OutboxPublisher outboxPublisher;

    public PersonApplicationService(
            PersonRepository personRepository,
            TenantContextAccessor tenantContextAccessor,
            EnablementGuard enablementGuard,
            AuditEventPublisher auditEventPublisher,
            OutboxPublisher outboxPublisher
    ) {
        this.personRepository = personRepository;
        this.tenantContextAccessor = tenantContextAccessor;
        this.enablementGuard = enablementGuard;
        this.auditEventPublisher = auditEventPublisher;
        this.outboxPublisher = outboxPublisher;
    }

    @Override
    public Mono<PersonViewDto> createPerson(CreatePersonCommandDto command) {
        return enablementGuard.requireFeatureEnabled("person.registry")
                .then(requireTenant())
                .flatMap(tenantId -> {
                    validate(command);
                    Instant now = Instant.now();
                    PersonDto person = new PersonDto(
                            UUID.randomUUID(),
                            tenantId,
                            command.personCode(),
                            command.firstName(),
                            command.lastName(),
                            command.email(),
                            command.mobile(),
                            command.countryCode(),
                            command.nationalityCode(),
                            now,
                            now);

                    return personRepository.save(person)
                            .flatMap(saved -> auditEventPublisher.publish(AuditEvent.detailed(
                                            "system",
                                            tenantId,
                                            "PERSON_CREATED",
                                            "PERSON",
                                            saved.id().toString(),
                                            "person",
                                            1L,
                                            List.of("personCode", "firstName", "lastName", "email", "mobile", "countryCode", "nationalityCode"),
                                            Map.of(),
                                            personSnapshot(saved),
                                            "system",
                                            "system",
                                            null,
                                            "PersonDto registered",
                                            "hrms-person",
                                            null,
                                            null,
                                            null,
                                            null,
                                            null,
                                            Map.of(
                                                    "personCode", saved.personCode(),
                                                    "email", saved.email()),
                                            false))
                                    .then(outboxPublisher.publish(new OutboxEvent(
                                            tenantId,
                                            "PERSON",
                                            saved.id().toString(),
                                            "PersonCreated",
                                            personCreatedPayload(saved),
                                            now)))
                                    .thenReturn(saved))
                            .map(this::toView);
                });
    }

    @Override
    public Mono<PersonViewDto> getPerson(UUID personId) {
        return enablementGuard.requireFeatureEnabled("person.registry")
                .then(requireTenant())
                .flatMap(tenantId -> personRepository.findById(personId, tenantId)
                        .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.NOT_FOUND, "PERSON_NOT_FOUND", "PersonDto not found")))
                        .map(this::toView));
    }

    @Override
    public Flux<PersonViewDto> searchPersons(PersonSearchQueryDto query) {
        int limit = query.limit() > 0 ? query.limit() : DEFAULT_LIMIT;
        int offset = Math.max(query.offset(), 0);

        return enablementGuard.requireFeatureEnabled("person.search")
                .thenMany(requireTenant().flatMapMany(tenantId -> personRepository.search(query.query(), limit, offset, tenantId)
                        .map(this::toView)));
    }

    private Mono<String> requireTenant() {
        return tenantContextAccessor.currentTenantId()
                .switchIfEmpty(Mono.error(new HrmsException(HttpStatus.BAD_REQUEST, "TENANT_REQUIRED", "Tenant is required")));
    }

    private void validate(CreatePersonCommandDto command) {
        if (!StringUtils.hasText(command.personCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "PERSON_CODE_REQUIRED", "PersonDto code is required");
        }
        if (!StringUtils.hasText(command.firstName())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "FIRST_NAME_REQUIRED", "First name is required");
        }
        if (!StringUtils.hasText(command.email())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "EMAIL_REQUIRED", "Email is required");
        }
        if (!StringUtils.hasText(command.countryCode())) {
            throw new HrmsException(HttpStatus.BAD_REQUEST, "COUNTRY_REQUIRED", "CountryDto code is required");
        }
    }

    private PersonViewDto toView(PersonDto person) {
        return new PersonViewDto(
                person.id(),
                person.tenantId(),
                person.personCode(),
                person.firstName(),
                person.lastName(),
                person.email(),
                person.mobile(),
                person.countryCode(),
                person.nationalityCode(),
                person.createdAt(),
                person.updatedAt());
    }

    private String personCreatedPayload(PersonDto person) {
        return "{\"personId\":\"%s\",\"personCode\":\"%s\"}".formatted(person.id(), person.personCode());
    }

    private Map<String, Object> personSnapshot(PersonDto person) {
        Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("personCode", person.personCode());
        snapshot.put("firstName", person.firstName());
        snapshot.put("lastName", person.lastName());
        snapshot.put("email", person.email());
        snapshot.put("mobile", person.mobile());
        snapshot.put("countryCode", person.countryCode());
        snapshot.put("nationalityCode", person.nationalityCode());
        return snapshot;
    }
}
