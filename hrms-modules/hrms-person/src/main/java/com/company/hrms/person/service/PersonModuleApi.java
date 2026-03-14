package com.company.hrms.person.service;

import com.company.hrms.person.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonModuleApi {

    Mono<PersonViewDto> createPerson(CreatePersonCommandDto command);

    Mono<PersonViewDto> getPerson(UUID personId);

    Flux<PersonViewDto> searchPersons(PersonSearchQueryDto query);
}
