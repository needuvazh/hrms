package com.company.hrms.employee.controller;

import com.company.hrms.employee.model.*;
import com.company.hrms.employee.service.*;

import com.company.hrms.contracts.employee.CreateEmployeeCommandDto;
import com.company.hrms.employee.service.EmployeeModuleApi;
import com.company.hrms.employee.model.EmployeeSearchQueryDto;
import com.company.hrms.employee.model.EmployeeViewDto;
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
@RequestMapping("/api/v1/employees")
@Tag(name = "EmployeeDto", description = "EmployeeDto master data APIs")
public class EmployeeController {

    private final EmployeeModuleApi employeeModuleApi;

    public EmployeeController(EmployeeModuleApi employeeModuleApi) {
        this.employeeModuleApi = employeeModuleApi;
    }

    @PostMapping
    @Operation(summary = "Create employee", description = "Creates an employee profile and links it to person and organization metadata.")
    public Mono<EmployeeViewDto> createEmployee(@Valid @RequestBody CreateEmployeeRequest request) {
        CreateEmployeeCommandDto command = new CreateEmployeeCommandDto(
                request.employeeCode(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.departmentCode(),
                request.jobTitle(),
                request.personId());

        return employeeModuleApi.createEmployee(command);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee", description = "Loads one employee by unique employee identifier.")
    public Mono<EmployeeViewDto> getEmployee(
            @Parameter(description = "Unique employee identifier", example = "a31fffd4-35af-42ea-9872-f5e100f8d3a9")
            @PathVariable("id") UUID id
    ) {
        return employeeModuleApi.getEmployee(id);
    }

    @GetMapping
    @Operation(summary = "Search employees", description = "Searches employees using keyword and pagination parameters.")
    public Flux<EmployeeViewDto> searchEmployees(
            @Parameter(description = "Free-text search over employee code, name, email, department and title", example = "finance")
            @RequestParam(name = "q", required = false) String query,
            @Parameter(description = "Maximum rows to return", example = "50")
            @RequestParam(name = "limit", required = false, defaultValue = "50") int limit,
            @Parameter(description = "Zero-based result offset", example = "0")
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset
    ) {
        return employeeModuleApi.searchEmployees(new EmployeeSearchQueryDto(query, limit, offset));
    }

    public record CreateEmployeeRequest(
            @Schema(description = "Business employee code", example = "EMP-20001")
            @NotBlank String employeeCode,
            @Schema(description = "Given name", example = "Aisha")
            @NotBlank String firstName,
            @Schema(description = "Family name", example = "Khan")
            String lastName,
            @Schema(description = "Primary work email", example = "aisha.khan@acme.com")
            @NotBlank @Email String email,
            @Schema(description = "Organization department code", example = "FIN")
            String departmentCode,
            @Schema(description = "EmployeeDto job title", example = "Senior Accountant")
            String jobTitle,
            @Schema(description = "Linked person profile identifier", example = "ecf8c677-9899-41d6-a2fe-b4bf4a47aa04")
            UUID personId
    ) {
    }
}
