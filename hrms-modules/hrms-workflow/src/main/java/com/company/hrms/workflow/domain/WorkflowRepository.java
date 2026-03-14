package com.company.hrms.workflow.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface WorkflowRepository {

    Mono<WorkflowDefinition> findActiveDefinitionByKey(String tenantId, String workflowKey);

    Mono<WorkflowInstance> saveInstance(WorkflowInstance instance);

    Mono<WorkflowInstance> updateInstance(WorkflowInstance instance);

    Mono<WorkflowInstance> findInstanceById(String tenantId, UUID workflowInstanceId);

    Mono<Integer> countSteps(String tenantId, UUID workflowInstanceId);

    Mono<WorkflowStep> saveStep(String tenantId, WorkflowStep step);
}
