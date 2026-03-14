package com.company.hrms.person.api;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonModuleApi {

    Mono<PersonView> createPerson(CreatePersonCommand command);

    Mono<PersonView> getPerson(UUID personId);

    Flux<PersonView> searchPersons(PersonSearchQuery query);
}
