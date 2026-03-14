package com.company.hrms.person.repository;

import com.company.hrms.person.model.*;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PersonRepository {

    Mono<PersonDto> save(PersonDto person);

    Mono<PersonDto> findById(UUID personId, String tenantId);

    Flux<PersonDto> search(String searchQuery, int limit, int offset, String tenantId);
}
