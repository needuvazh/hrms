package com.company.hrms.auth.controller;

import com.company.hrms.auth.model.AuthTokenCommandDto;
import com.company.hrms.auth.model.AuthTokenViewDto;
import com.company.hrms.auth.model.CurrentUserViewDto;
import com.company.hrms.auth.model.RoleViewDto;
import com.company.hrms.auth.service.AuthModuleApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Validated
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and current-user authorization APIs")
public class AuthController {

    private final AuthModuleApi authModuleApi;

    public AuthController(AuthModuleApi authModuleApi) {
        this.authModuleApi = authModuleApi;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and issue access token", description = "Authenticates username and password, then returns JWT with role, permission, and scope details.")
    public Mono<AuthTokenViewDto> login(@Valid @RequestBody LoginRequest request) {
        return authModuleApi.issueToken(new AuthTokenCommandDto(request.username(), request.password()));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns details of the currently authenticated user resolved from the security context.")
    public Mono<CurrentUserViewDto> currentUser() {
        return authModuleApi.currentUser();
    }

    @GetMapping("/me/roles")
    @Operation(summary = "Get current user roles", description = "Returns the role assignments for the currently authenticated user.")
    public Flux<RoleViewDto> currentUserRoles() {
        return authModuleApi.getRolesForCurrentUser();
    }

    public record LoginRequest(
            @Schema(description = "Unique user login name", example = "admin")
            @NotBlank String username,
            @Schema(description = "Raw account password", example = "admin")
            @NotBlank String password
    ) {
    }
}
