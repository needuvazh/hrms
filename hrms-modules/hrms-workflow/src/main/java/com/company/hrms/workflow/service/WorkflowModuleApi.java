package com.company.hrms.workflow.service;

import com.company.hrms.workflow.model.*;
import com.company.hrms.contracts.workflow.AdvanceWorkflowCommandDto;
import com.company.hrms.contracts.workflow.StartWorkflowCommandDto;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface WorkflowModuleApi {

    Mono<WorkflowInstanceViewDto> startWorkflow(StartWorkflowCommandDto command);

    Mono<WorkflowInstanceViewDto> advanceWorkflow(AdvanceWorkflowCommandDto command);

    Mono<WorkflowInstanceViewDto> getWorkflowInstance(UUID workflowInstanceId);
}
