package com.sharon.agentplatform.agent.workflow.controller;

import com.sharon.agentplatform.agent.workflow.dto.AgentActionObservationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionViewResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunDebugResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunExplanationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunPlanResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunReflectionResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentRunTimelineResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowResponse;
import com.sharon.agentplatform.agent.workflow.service.AgentActionObservationService;
import com.sharon.agentplatform.agent.workflow.service.AgentDecisionService;
import com.sharon.agentplatform.agent.workflow.service.AgentRunDebugService;
import com.sharon.agentplatform.agent.workflow.service.AgentRunExplanationService;
import com.sharon.agentplatform.agent.workflow.service.AgentRunPlanService;
import com.sharon.agentplatform.agent.workflow.service.AgentRunReflectionService;
import com.sharon.agentplatform.agent.workflow.service.AgentRunTimelineService;
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
    private final AgentRunPlanService agentRunPlanService;
    private final AgentRunTimelineService agentRunTimelineService;
    private final AgentRunDebugService agentRunDebugService;

    public AgentWorkflowController(AgentWorkflowService agentWorkflowService,
                                   AgentActionObservationService agentActionObservationService,
                                   AgentDecisionService agentDecisionService,
                                   AgentRunExplanationService agentRunExplanationService,
                                   AgentRunReflectionService agentRunReflectionService,
                                   AgentRunPlanService agentRunPlanService,
                                   AgentRunTimelineService agentRunTimelineService,
                                   AgentRunDebugService agentRunDebugService) {
        this.agentWorkflowService = agentWorkflowService;
        this.agentActionObservationService = agentActionObservationService;
        this.agentDecisionService = agentDecisionService;
        this.agentRunExplanationService = agentRunExplanationService;
        this.agentRunReflectionService = agentRunReflectionService;
        this.agentRunPlanService = agentRunPlanService;
        this.agentRunTimelineService = agentRunTimelineService;
        this.agentRunDebugService = agentRunDebugService;
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

    @GetMapping("/{runId}/plan")
    public ApiResponse<AgentRunPlanResponse> getPlan(@PathVariable String runId) {
        return ApiResponse.success(agentRunPlanService.getPlan(runId));
    }

    @GetMapping("/{runId}/timeline")
    public ApiResponse<AgentRunTimelineResponse> getTimeline(@PathVariable String runId) {
        return ApiResponse.success(agentRunTimelineService.getTimeline(runId));
    }

    @GetMapping("/{runId}/debug")
    public ApiResponse<AgentRunDebugResponse> debug(@PathVariable String runId) {
        return ApiResponse.success(agentRunDebugService.debug(runId));
    }
}
