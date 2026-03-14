package com.company.hrms.person.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonRepository {

    Mono<Person> save(Person person);

    Mono<Person> findById(UUID personId, String tenantId);

    Flux<Person> search(String searchQuery, int limit, int offset, String tenantId);
}
