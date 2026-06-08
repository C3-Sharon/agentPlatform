package com.sharon.agentplatform.agent.workflow.controller;

import com.sharon.agentplatform.agent.workflow.dto.AgentActionObservationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionViewResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunExplanationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunReflectionResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowResponse;
import com.sharon.agentplatform.agent.workflow.service.AgentActionObservationService;
import com.sharon.agentplatform.agent.workflow.service.AgentDecisionService;
import com.sharon.agentplatform.agent.workflow.service.AgentRunExplanationService;
import com.sharon.agentplatform.agent.workflow.service.AgentRunReflectionService;
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
    private final AgentDecisionService agentDecisionService;
    private final AgentRunExplanationService agentRunExplanationService;
    private final AgentRunReflectionService agentRunReflectionService;

    public AgentWorkflowController(AgentWorkflowService agentWorkflowService,
                                   AgentActionObservationService agentActionObservationService,
                                   AgentDecisionService agentDecisionService,
                                   AgentRunExplanationService agentRunExplanationService,
                                   AgentRunReflectionService agentRunReflectionService) {
        this.agentWorkflowService = agentWorkflowService;
        this.agentActionObservationService = agentActionObservationService;
        this.agentDecisionService = agentDecisionService;
        this.agentRunExplanationService = agentRunExplanationService;
        this.agentRunReflectionService = agentRunReflectionService;
    }

    @GetMapping("/{runId}/workflow")
    public ApiResponse<AgentWorkflowResponse> getWorkflow(@PathVariable String runId) {
        return ApiResponse.success(agentWorkflowService.getWorkflow(runId));
    }

    @GetMapping("/{runId}/actions")
    public ApiResponse<AgentActionObservationResponse> getActions(@PathVariable String runId) {
        return ApiResponse.success(agentActionObservationService.getActions(runId));
    }

    @GetMapping("/{runId}/decisions")
    public ApiResponse<AgentDecisionViewResponse> getDecisions(@PathVariable String runId) {
        return ApiResponse.success(agentDecisionService.getDecisions(runId));
    }

    @GetMapping("/{runId}/explain")
    public ApiResponse<AgentRunExplanationResponse> explain(@PathVariable String runId) {
        return ApiResponse.success(agentRunExplanationService.explain(runId));
    }

    @GetMapping("/{runId}/reflection")
    public ApiResponse<AgentRunReflectionResponse> reflect(@PathVariable String runId) {
        return ApiResponse.success(agentRunReflectionService.reflect(runId));
    }
}
