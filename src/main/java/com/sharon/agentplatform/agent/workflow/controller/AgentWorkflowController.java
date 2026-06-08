package com.sharon.agentplatform.agent.workflow.controller;

import com.sharon.agentplatform.agent.workflow.dto.AgentActionObservationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowResponse;
import com.sharon.agentplatform.agent.workflow.service.AgentActionObservationService;
import com.sharon.agentplatform.agent.workflow.service.AgentWorkflowService;
import com.sharon.agentplatform.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/runs")
public class AgentWorkflowController {

    private final AgentWorkflowService agentWorkflowService;
    private final AgentActionObservationService agentActionObservationService;

    public AgentWorkflowController(AgentWorkflowService agentWorkflowService,
                                   AgentActionObservationService agentActionObservationService) {
        this.agentWorkflowService = agentWorkflowService;
        this.agentActionObservationService = agentActionObservationService;
    }

    @GetMapping("/{runId}/workflow")
    public ApiResponse<AgentWorkflowResponse> getWorkflow(@PathVariable String runId) {
        return ApiResponse.success(agentWorkflowService.getWorkflow(runId));
    }

    @GetMapping("/{runId}/actions")
    public ApiResponse<AgentActionObservationResponse> getActions(@PathVariable String runId) {
        return ApiResponse.success(agentActionObservationService.getActions(runId));
    }
}
