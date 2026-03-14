package com.company.hrms.workflow.api;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface WorkflowModuleApi {

    Mono<WorkflowInstanceView> startWorkflow(StartWorkflowCommand command);

    Mono<WorkflowInstanceView> advanceWorkflow(AdvanceWorkflowCommand command);

    Mono<WorkflowInstanceView> getWorkflowInstance(UUID workflowInstanceId);
}
