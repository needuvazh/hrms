package com.company.hrms.workflow.service;

import com.company.hrms.workflow.model.*;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface WorkflowModuleApi {

    Mono<WorkflowInstanceViewDto> startWorkflow(StartWorkflowCommandDto command);

    Mono<WorkflowInstanceViewDto> advanceWorkflow(AdvanceWorkflowCommandDto command);

    Mono<WorkflowInstanceViewDto> getWorkflowInstance(UUID workflowInstanceId);
}
