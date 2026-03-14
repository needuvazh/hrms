package com.company.hrms.workflow.repository;

import com.company.hrms.workflow.model.*;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface WorkflowRepository {

    Mono<WorkflowDefinitionDto> findActiveDefinitionByKey(String tenantId, String workflowKey);

    Mono<WorkflowInstanceDto> saveInstance(WorkflowInstanceDto instance);

    Mono<WorkflowInstanceDto> updateInstance(WorkflowInstanceDto instance);

    Mono<WorkflowInstanceDto> findInstanceById(String tenantId, UUID workflowInstanceId);

    Mono<Integer> countSteps(String tenantId, UUID workflowInstanceId);

    Mono<WorkflowStepDto> saveStep(String tenantId, WorkflowStepDto step);
}
