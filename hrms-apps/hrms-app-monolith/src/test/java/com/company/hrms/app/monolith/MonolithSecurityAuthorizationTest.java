package com.company.hrms.app.monolith;

import com.company.hrms.app.monolith.config.MonolithSecurityPolicyConfiguration;
import com.company.hrms.employee.api.EmployeeModuleApi;
import com.company.hrms.employee.api.EmployeeSearchQuery;
import com.company.hrms.employee.api.EmployeeView;
import com.company.hrms.employee.infrastructure.web.EmployeeController;
import com.company.hrms.person.api.PersonModuleApi;
import com.company.hrms.person.api.PersonSearchQuery;
import com.company.hrms.person.api.PersonView;
import com.company.hrms.person.infrastructure.web.PersonController;
import com.company.hrms.platform.starter.security.api.JwtTokenClaims;
import com.company.hrms.platform.starter.security.api.JwtTokenService;
import com.company.hrms.recruitment.api.CandidateSearchQuery;
import com.company.hrms.recruitment.api.CandidateStatus;
import com.company.hrms.recruitment.api.CandidateView;
import com.company.hrms.recruitment.api.RecruitmentModuleApi;
import com.company.hrms.recruitment.infrastructure.web.RecruitmentController;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(
        classes = MonolithSecurityAuthorizationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration,com.company.hrms.platform.outbox.config.OutboxAutoConfiguration"
        }
)
@AutoConfigureWebTestClient
class MonolithSecurityAuthorizationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtTokenService jwtTokenService;

    @Test
    void employeeEndpointsRequireAuthentication() {
        webTestClient.get()
                .uri("/api/v1/employees")
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void employeeReadRequiresPermission() {
        String tokenWithoutPermission = jwtToken(Set.of("HR_USER"), Set.of());

        webTestClient.get()
                .uri("/api/v1/employees")
                .header("Authorization", "Bearer " + tokenWithoutPermission)
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void employeeReadAllowedWithPermissionAndMatchingTenant() {
        String tokenWithRead = jwtToken(Set.of("HR_ADMIN"), Set.of("EMPLOYEE_READ"));

        webTestClient.get()
                .uri("/api/v1/employees")
                .header("Authorization", "Bearer " + tokenWithRead)
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void tenantMismatchBetweenHeaderAndTokenIsForbidden() {
        String tokenDefaultTenant = jwtToken(Set.of("HR_ADMIN"), Set.of("EMPLOYEE_READ"));

        webTestClient.get()
                .uri("/api/v1/employees")
                .header("Authorization", "Bearer " + tokenDefaultTenant)
                .header("X-Tenant-Id", "other-tenant")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void employeeCreateRequiresWritePermission() {
        String tokenWithReadOnly = jwtToken(Set.of("HR_ADMIN"), Set.of("EMPLOYEE_READ"));

        webTestClient.post()
                .uri("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenWithReadOnly)
                .header("X-Tenant-Id", "default")
                .bodyValue("""
                        {
                          "employeeCode":"EMP-1001",
                          "firstName":"Alice",
                          "lastName":"Johnson",
                          "email":"alice@hrms.local",
                          "departmentCode":"ENG",
                          "jobTitle":"Engineer"
                        }
                        """)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void personEndpointsRequirePermission() {
        String tokenWithoutPersonPermission = jwtToken(Set.of("HR_USER"), Set.of("EMPLOYEE_READ"));

        webTestClient.get()
                .uri("/api/v1/persons")
                .header("Authorization", "Bearer " + tokenWithoutPersonPermission)
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isForbidden();

        String tokenWithPersonRead = jwtToken(Set.of("HR_USER"), Set.of("PERSON_READ"));
        webTestClient.get()
                .uri("/api/v1/persons")
                .header("Authorization", "Bearer " + tokenWithPersonRead)
                .header("X-Tenant-Id", "default")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void recruitmentWriteEndpointsRequireRecruitmentWritePermission() {
        String tokenReadOnly = jwtToken(Set.of("HR_USER"), Set.of("RECRUITMENT_READ"));

        webTestClient.patch()
                .uri("/api/v1/recruitment/candidates/11111111-1111-1111-1111-111111111111/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenReadOnly)
                .header("X-Tenant-Id", "default")
                .bodyValue("""
                        {
                          "status":"OFFER_ACCEPTED",
                          "reason":"ready"
                        }
                        """)
                .exchange()
                .expectStatus().isForbidden();

        String tokenWithWrite = jwtToken(Set.of("HR_ADMIN"), Set.of("RECRUITMENT_WRITE"));

        webTestClient.patch()
                .uri("/api/v1/recruitment/candidates/11111111-1111-1111-1111-111111111111/status")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + tokenWithWrite)
                .header("X-Tenant-Id", "default")
                .bodyValue("""
                        {
                          "status":"OFFER_ACCEPTED",
                          "reason":"ready"
                        }
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    private String jwtToken(Set<String> roles, Set<String> permissions) {
        String[] value = new String[1];
        StepVerifier.create(jwtTokenService.issueToken(new JwtTokenClaims(
                                UUID.fromString("21111111-1111-1111-1111-111111111111"),
                                "admin",
                                "default",
                                roles,
                                permissions))
                        .map(token -> token.tokenValue()))
                .assertNext(token -> value[0] = token)
                .verifyComplete();
        return value[0];
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(MonolithSecurityPolicyConfiguration.class)
    static class TestApplication {

        @Bean
        EmployeeModuleApi employeeModuleApi() {
            return new EmployeeModuleApi() {
                @Override
                public Mono<EmployeeView> createEmployee(com.company.hrms.employee.api.CreateEmployeeCommand command) {
                    return Mono.just(new EmployeeView(
                            UUID.randomUUID(),
                            "default",
                            command.employeeCode(),
                            command.firstName(),
                            command.lastName(),
                            command.email(),
                            command.departmentCode(),
                            command.jobTitle(),
                            Instant.now(),
                            Instant.now()));
                }

                @Override
                public Mono<EmployeeView> getEmployee(UUID employeeId) {
                    return Mono.just(new EmployeeView(
                            employeeId,
                            "default",
                            "EMP-TEST",
                            "Test",
                            "User",
                            "test@hrms.local",
                            "ENG",
                            "Engineer",
                            Instant.now(),
                            Instant.now()));
                }

                @Override
                public Flux<EmployeeView> searchEmployees(EmployeeSearchQuery query) {
                    return Flux.just(new EmployeeView(
                            UUID.randomUUID(),
                            "default",
                            "EMP-TEST",
                            "Test",
                            "User",
                            "test@hrms.local",
                            "ENG",
                            "Engineer",
                            Instant.now(),
                            Instant.now()));
                }
            };
        }

        @Bean
        EmployeeController employeeController(EmployeeModuleApi employeeModuleApi) {
            return new EmployeeController(employeeModuleApi);
        }

        @Bean
        PersonModuleApi personModuleApi() {
            return new PersonModuleApi() {
                @Override
                public Mono<PersonView> createPerson(com.company.hrms.person.api.CreatePersonCommand command) {
                    Instant now = Instant.now();
                    return Mono.just(new PersonView(
                            UUID.randomUUID(),
                            "default",
                            command.personCode(),
                            command.firstName(),
                            command.lastName(),
                            command.email(),
                            command.mobile(),
                            command.countryCode(),
                            command.nationalityCode(),
                            now,
                            now));
                }

                @Override
                public Mono<PersonView> getPerson(UUID personId) {
                    Instant now = Instant.now();
                    return Mono.just(new PersonView(
                            personId,
                            "default",
                            "PER-TEST",
                            "Test",
                            "Person",
                            "person@hrms.local",
                            "+96800000000",
                            "OM",
                            "OM",
                            now,
                            now));
                }

                @Override
                public Flux<PersonView> searchPersons(PersonSearchQuery query) {
                    Instant now = Instant.now();
                    return Flux.just(new PersonView(
                            UUID.randomUUID(),
                            "default",
                            "PER-TEST",
                            "Test",
                            "Person",
                            "person@hrms.local",
                            "+96800000000",
                            "OM",
                            "OM",
                            now,
                            now));
                }
            };
        }

        @Bean
        PersonController personController(PersonModuleApi personModuleApi) {
            return new PersonController(personModuleApi);
        }

        @Bean
        RecruitmentModuleApi recruitmentModuleApi() {
            return new RecruitmentModuleApi() {
                @Override
                public Mono<CandidateView> createCandidate(com.company.hrms.recruitment.api.CreateCandidateCommand command) {
                    Instant now = Instant.now();
                    return Mono.just(new CandidateView(
                            UUID.randomUUID(),
                            "default",
                            command.personId(),
                            command.candidateCode(),
                            command.firstName(),
                            command.lastName(),
                            command.email(),
                            command.jobPostingCode(),
                            CandidateStatus.APPLIED,
                            now,
                            now));
                }

                @Override
                public Mono<CandidateView> getCandidate(UUID candidateId) {
                    Instant now = Instant.now();
                    return Mono.just(new CandidateView(
                            candidateId,
                            "default",
                            UUID.randomUUID(),
                            "CAN-TEST",
                            "Test",
                            "Candidate",
                            "candidate@hrms.local",
                            "ENG-001",
                            CandidateStatus.OFFER,
                            now,
                            now));
                }

                @Override
                public Flux<CandidateView> searchCandidates(CandidateSearchQuery query) {
                    Instant now = Instant.now();
                    return Flux.just(new CandidateView(
                            UUID.randomUUID(),
                            "default",
                            UUID.randomUUID(),
                            "CAN-TEST",
                            "Test",
                            "Candidate",
                            "candidate@hrms.local",
                            "ENG-001",
                            CandidateStatus.OFFER,
                            now,
                            now));
                }

                @Override
                public Mono<CandidateView> updateCandidateStatus(com.company.hrms.recruitment.api.UpdateCandidateStatusCommand command) {
                    Instant now = Instant.now();
                    return Mono.just(new CandidateView(
                            command.candidateId(),
                            "default",
                            UUID.randomUUID(),
                            "CAN-TEST",
                            "Test",
                            "Candidate",
                            "candidate@hrms.local",
                            "ENG-001",
                            command.status(),
                            now,
                            now));
                }

                @Override
                public Mono<CandidateView> hireCandidate(com.company.hrms.recruitment.api.HireCandidateCommand command) {
                    Instant now = Instant.now();
                    return Mono.just(new CandidateView(
                            command.candidateId(),
                            "default",
                            UUID.randomUUID(),
                            "CAN-TEST",
                            "Test",
                            "Candidate",
                            "candidate@hrms.local",
                            "ENG-001",
                            CandidateStatus.HIRED,
                            now,
                            now));
                }
            };
        }

        @Bean
        RecruitmentController recruitmentController(RecruitmentModuleApi recruitmentModuleApi) {
            return new RecruitmentController(recruitmentModuleApi);
        }
    }
}
