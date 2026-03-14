package com.company.hrms.auth.service;

import com.company.hrms.auth.model.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AuthModuleApi {

    Mono<AuthTokenViewDto> issueToken(AuthTokenCommandDto command);

    Mono<CurrentUserViewDto> currentUser();

    Flux<RoleViewDto> getRolesForCurrentUser();

    Mono<ProvisionedUserAccountViewDto> provisionUserAccount(ProvisionUserAccountCommandDto command);
}
