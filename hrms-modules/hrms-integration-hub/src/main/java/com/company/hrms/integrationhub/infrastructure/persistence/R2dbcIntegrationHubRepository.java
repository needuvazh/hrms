package com.company.hrms.integrationhub.infrastructure.persistence;

import com.company.hrms.integrationhub.domain.IntegrationDefinition;
import com.company.hrms.integrationhub.domain.IntegrationEndpoint;
import com.company.hrms.integrationhub.domain.IntegrationExecution;
import com.company.hrms.integrationhub.domain.IntegrationHubRepository;
import com.company.hrms.integrationhub.domain.IntegrationProviderType;
import com.company.hrms.integrationhub.domain.IntegrationStatus;
import java.time.Instant;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.DatabaseClient.GenericExecuteSpec;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcIntegrationHubRepository implements IntegrationHubRepository {

    private final DatabaseClient databaseClient;

    public R2dbcIntegrationHubRepository(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<IntegrationDefinition> saveDefinition(IntegrationDefinition definition) {
        return databaseClient.sql("""
                        INSERT INTO integration_hub.integration_definitions(
                            id, tenant_id, integration_key, provider_type, display_name, status, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :integrationKey, :providerType, :displayName, :status, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, integration_key, provider_type, display_name, status, created_at, updated_at
                        """)
                .bind("id", definition.id())
                .bind("tenantId", definition.tenantId())
                .bind("integrationKey", definition.integrationKey())
                .bind("providerType", definition.providerType().name())
                .bind("displayName", definition.displayName())
                .bind("status", definition.status().name())
                .bind("createdAt", definition.createdAt())
                .bind("updatedAt", definition.updatedAt())
                .map((row, metadata) -> mapDefinition(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("integration_key", String.class),
                        row.get("provider_type", String.class),
                        row.get("display_name", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<IntegrationDefinition> findDefinitionById(String tenantId, UUID definitionId) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, integration_key, provider_type, display_name, status, created_at, updated_at
                        FROM integration_hub.integration_definitions
                        WHERE tenant_id = :tenantId
                          AND id = :definitionId
                        """)
                .bind("tenantId", tenantId)
                .bind("definitionId", definitionId)
                .map((row, metadata) -> mapDefinition(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("integration_key", String.class),
                        row.get("provider_type", String.class),
                        row.get("display_name", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<IntegrationEndpoint> saveEndpoint(IntegrationEndpoint endpoint) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO integration_hub.integration_endpoints(
                            id, definition_id, tenant_id, endpoint_key, base_url, auth_type, configuration_json, status, created_at, updated_at
                        ) VALUES (
                            :id, :definitionId, :tenantId, :endpointKey, :baseUrl, :authType, :configurationJson, :status, :createdAt, :updatedAt
                        )
                        RETURNING id, definition_id, tenant_id, endpoint_key, base_url, auth_type, configuration_json, status, created_at, updated_at
                        """)
                .bind("id", endpoint.id())
                .bind("definitionId", endpoint.definitionId())
                .bind("tenantId", endpoint.tenantId())
                .bind("endpointKey", endpoint.endpointKey())
                .bind("baseUrl", endpoint.baseUrl())
                .bind("status", endpoint.status().name())
                .bind("createdAt", endpoint.createdAt())
                .bind("updatedAt", endpoint.updatedAt());

        spec = bindNullable(spec, "authType", endpoint.authType(), String.class);
        spec = bindNullable(spec, "configurationJson", endpoint.configurationJson(), String.class);

        return spec.map((row, metadata) -> mapEndpoint(
                        row.get("id", UUID.class),
                        row.get("definition_id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("endpoint_key", String.class),
                        row.get("base_url", String.class),
                        row.get("auth_type", String.class),
                        row.get("configuration_json", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<IntegrationEndpoint> findEndpointById(String tenantId, UUID endpointId) {
        return databaseClient.sql("""
                        SELECT id, definition_id, tenant_id, endpoint_key, base_url, auth_type, configuration_json, status, created_at, updated_at
                        FROM integration_hub.integration_endpoints
                        WHERE tenant_id = :tenantId
                          AND id = :endpointId
                        """)
                .bind("tenantId", tenantId)
                .bind("endpointId", endpointId)
                .map((row, metadata) -> mapEndpoint(
                        row.get("id", UUID.class),
                        row.get("definition_id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("endpoint_key", String.class),
                        row.get("base_url", String.class),
                        row.get("auth_type", String.class),
                        row.get("configuration_json", String.class),
                        row.get("status", String.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<IntegrationExecution> saveExecution(IntegrationExecution execution) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        INSERT INTO integration_hub.integration_executions(
                            id, tenant_id, definition_id, endpoint_id, operation, payload_json,
                            status, external_reference, error_message, attempted_at,
                            completed_at, created_at, updated_at
                        ) VALUES (
                            :id, :tenantId, :definitionId, :endpointId, :operation, :payloadJson,
                            :status, :externalReference, :errorMessage, :attemptedAt,
                            :completedAt, :createdAt, :updatedAt
                        )
                        RETURNING id, tenant_id, definition_id, endpoint_id, operation, payload_json,
                                  status, external_reference, error_message, attempted_at,
                                  completed_at, created_at, updated_at
                        """)
                .bind("id", execution.id())
                .bind("tenantId", execution.tenantId())
                .bind("definitionId", execution.definitionId())
                .bind("endpointId", execution.endpointId())
                .bind("operation", execution.operation())
                .bind("payloadJson", execution.payloadJson())
                .bind("status", execution.status().name())
                .bind("attemptedAt", execution.attemptedAt())
                .bind("createdAt", execution.createdAt())
                .bind("updatedAt", execution.updatedAt());

        spec = bindNullable(spec, "externalReference", execution.externalReference(), String.class);
        spec = bindNullable(spec, "errorMessage", execution.errorMessage(), String.class);
        spec = bindNullable(spec, "completedAt", execution.completedAt(), Instant.class);

        return spec.map((row, metadata) -> mapExecution(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("definition_id", UUID.class),
                        row.get("endpoint_id", UUID.class),
                        row.get("operation", String.class),
                        row.get("payload_json", String.class),
                        row.get("status", String.class),
                        row.get("external_reference", String.class),
                        row.get("error_message", String.class),
                        row.get("attempted_at", Instant.class),
                        row.get("completed_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Mono<IntegrationExecution> updateExecution(IntegrationExecution execution) {
        GenericExecuteSpec spec = databaseClient.sql("""
                        UPDATE integration_hub.integration_executions
                        SET status = :status,
                            external_reference = :externalReference,
                            error_message = :errorMessage,
                            completed_at = :completedAt,
                            updated_at = :updatedAt
                        WHERE tenant_id = :tenantId
                          AND id = :id
                        RETURNING id, tenant_id, definition_id, endpoint_id, operation, payload_json,
                                  status, external_reference, error_message, attempted_at,
                                  completed_at, created_at, updated_at
                        """)
                .bind("id", execution.id())
                .bind("tenantId", execution.tenantId())
                .bind("status", execution.status().name())
                .bind("updatedAt", execution.updatedAt());

        spec = bindNullable(spec, "externalReference", execution.externalReference(), String.class);
        spec = bindNullable(spec, "errorMessage", execution.errorMessage(), String.class);
        spec = bindNullable(spec, "completedAt", execution.completedAt(), Instant.class);

        return spec.map((row, metadata) -> mapExecution(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("definition_id", UUID.class),
                        row.get("endpoint_id", UUID.class),
                        row.get("operation", String.class),
                        row.get("payload_json", String.class),
                        row.get("status", String.class),
                        row.get("external_reference", String.class),
                        row.get("error_message", String.class),
                        row.get("attempted_at", Instant.class),
                        row.get("completed_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .one();
    }

    @Override
    public Flux<IntegrationExecution> findExecutionsByDefinition(String tenantId, UUID definitionId, int limit) {
        return databaseClient.sql("""
                        SELECT id, tenant_id, definition_id, endpoint_id, operation, payload_json,
                               status, external_reference, error_message, attempted_at,
                               completed_at, created_at, updated_at
                        FROM integration_hub.integration_executions
                        WHERE tenant_id = :tenantId
                          AND definition_id = :definitionId
                        ORDER BY attempted_at DESC
                        LIMIT :limit
                        """)
                .bind("tenantId", tenantId)
                .bind("definitionId", definitionId)
                .bind("limit", limit)
                .map((row, metadata) -> mapExecution(
                        row.get("id", UUID.class),
                        row.get("tenant_id", String.class),
                        row.get("definition_id", UUID.class),
                        row.get("endpoint_id", UUID.class),
                        row.get("operation", String.class),
                        row.get("payload_json", String.class),
                        row.get("status", String.class),
                        row.get("external_reference", String.class),
                        row.get("error_message", String.class),
                        row.get("attempted_at", Instant.class),
                        row.get("completed_at", Instant.class),
                        row.get("created_at", Instant.class),
                        row.get("updated_at", Instant.class)))
                .all();
    }

    private IntegrationDefinition mapDefinition(
            UUID id,
            String tenantId,
            String integrationKey,
            String providerType,
            String displayName,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new IntegrationDefinition(
                id,
                tenantId,
                integrationKey,
                IntegrationProviderType.valueOf(providerType),
                displayName,
                IntegrationStatus.valueOf(status),
                createdAt,
                updatedAt);
    }

    private IntegrationEndpoint mapEndpoint(
            UUID id,
            UUID definitionId,
            String tenantId,
            String endpointKey,
            String baseUrl,
            String authType,
            String configurationJson,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new IntegrationEndpoint(
                id,
                definitionId,
                tenantId,
                endpointKey,
                baseUrl,
                authType,
                configurationJson,
                IntegrationStatus.valueOf(status),
                createdAt,
                updatedAt);
    }

    private IntegrationExecution mapExecution(
            UUID id,
            String tenantId,
            UUID definitionId,
            UUID endpointId,
            String operation,
            String payloadJson,
            String status,
            String externalReference,
            String errorMessage,
            Instant attemptedAt,
            Instant completedAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        return new IntegrationExecution(
                id,
                tenantId,
                definitionId,
                endpointId,
                operation,
                payloadJson,
                IntegrationStatus.valueOf(status),
                externalReference,
                errorMessage,
                attemptedAt,
                completedAt,
                createdAt,
                updatedAt);
    }

    private <T> GenericExecuteSpec bindNullable(GenericExecuteSpec spec, String name, T value, Class<T> type) {
        if (value == null) {
            return spec.bindNull(name, type);
        }
        return spec.bind(name, value);
    }
}
