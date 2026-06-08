package com.sharon.agentplatform.agent.workflow.controller;

import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowResponse;
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

    public AgentWorkflowController(AgentWorkflowService agentWorkflowService) {
        this.agentWorkflowService = agentWorkflowService;
    }

    @GetMapping("/{runId}/workflow")
    public ApiResponse<AgentWorkflowResponse> getWorkflow(@PathVariable String runId) {
        return ApiResponse.success(agentWorkflowService.getWorkflow(runId));
    }
}
