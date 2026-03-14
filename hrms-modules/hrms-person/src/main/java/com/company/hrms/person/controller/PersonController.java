package com.company.hrms.person.controller;

import com.company.hrms.person.model.*;
import com.company.hrms.person.service.*;

import com.company.hrms.person.model.CreatePersonCommandDto;
import com.company.hrms.person.service.PersonModuleApi;
import com.company.hrms.person.model.PersonSearchQueryDto;
import com.company.hrms.person.model.PersonViewDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/persons")
@Tag(name = "PersonDto", description = "PersonDto profile management APIs")
public class PersonController {

    private final PersonModuleApi personModuleApi;

    public PersonController(PersonModuleApi personModuleApi) {
        this.personModuleApi = personModuleApi;
    }

    @PostMapping
    @Operation(summary = "Create person", description = "Creates a person profile that can later be linked to an employee or candidate lifecycle.")
    public Mono<PersonViewDto> createPerson(@Valid @RequestBody CreatePersonRequest request) {
        return personModuleApi.createPerson(new CreatePersonCommandDto(
                request.personCode(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.mobile(),
                request.countryCode(),
                request.nationalityCode()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get person", description = "Loads one person profile using the person UUID.")
    public Mono<PersonViewDto> getPerson(
            @Parameter(description = "Unique person identifier", example = "ecf8c677-9899-41d6-a2fe-b4bf4a47aa04")
            @PathVariable("id") UUID id
    ) {
        return personModuleApi.getPerson(id);
    }

    @GetMapping
    @Operation(summary = "Search persons", description = "Searches person profiles by keyword with paginated offset/limit controls.")
    public Flux<PersonViewDto> searchPersons(
            @Parameter(description = "Free-text search across person attributes such as code, name, and email", example = "john")
            @RequestParam(name = "q", required = false) String query,
            @Parameter(description = "Maximum rows to return", example = "50")
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit,
            @Parameter(description = "Zero-based result offset", example = "0")
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset
    ) {
        return personModuleApi.searchPersons(new PersonSearchQueryDto(query, limit, offset));
    }

    public record CreatePersonRequest(
            @Schema(description = "Business identifier for the person", example = "PER-10001")
            @NotBlank String personCode,
            @Schema(description = "Given name", example = "John")
            @NotBlank String firstName,
            @Schema(description = "Family name", example = "Doe")
            String lastName,
            @Schema(description = "Primary email address", example = "john.doe@acme.com")
            @NotBlank @Email String email,
            @Schema(description = "Mobile phone number", example = "+971501234567")
            String mobile,
            @Schema(description = "ISO country code used for residency", example = "AE")
            @NotBlank String countryCode,
            @Schema(description = "ISO nationality code", example = "IN")
            String nationalityCode
    ) {
    }
}
