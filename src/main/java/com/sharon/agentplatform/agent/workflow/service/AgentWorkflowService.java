package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowStageResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentWorkflowStepResponse;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowRun;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowStage;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowStep;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentWorkflowService {

    private final AgentWorkflowModelService agentWorkflowModelService;

    public AgentWorkflowService(AgentWorkflowModelService agentWorkflowModelService) {
        this.agentWorkflowModelService = agentWorkflowModelService;
    }

    public AgentWorkflowResponse getWorkflow(String runId) {
        AgentWorkflowRun workflowRun = agentWorkflowModelService.getWorkflowRun(runId);

        AgentWorkflowResponse response = new AgentWorkflowResponse();
        response.setRunId(workflowRun.getRunId());
        response.setConversationId(workflowRun.getConversationId());
        response.setModelId(workflowRun.getModelId());
        response.setUserMessage(workflowRun.getUserMessage());
        response.setAnswer(workflowRun.getAnswer());
        response.setStatus(workflowRun.getStatus());
        response.setErrorMessage(workflowRun.getErrorMessage());
        response.setDurationMs(workflowRun.getDurationMs());
        response.setCreatedAt(workflowRun.getCreatedAt());
        response.setStages(toStageResponses(workflowRun.getStages()));
        return response;
    }

    private List<AgentWorkflowStageResponse> toStageResponses(List<AgentWorkflowStage> stages) {
        List<AgentWorkflowStageResponse> responses = new ArrayList<>();
        for (AgentWorkflowStage stage : safeStages(stages)) {
            AgentWorkflowStageResponse response = new AgentWorkflowStageResponse();
            response.setStage(stage.getStage());
            response.setStatus(stage.getStatus());
            response.setSummary(stage.getSummary());
            response.setStartedAt(stage.getStartedAt());
            response.setFinishedAt(stage.getFinishedAt());
            response.setDurationMs(stage.getDurationMs());
            response.setSteps(toStepResponses(stage.getSteps()));
            responses.add(response);
        }
        return responses;
    }

    private List<AgentWorkflowStepResponse> toStepResponses(List<AgentWorkflowStep> steps) {
        List<AgentWorkflowStepResponse> responses = new ArrayList<>();
        for (AgentWorkflowStep step : safeSteps(steps)) {
            AgentWorkflowStepResponse response = new AgentWorkflowStepResponse();
            response.setStepOrder(step.getStepOrder());
            response.setStep(step.getStep());
            response.setStatus(step.getStatus());
            response.setDetail(step.getDetail());
            response.setData(step.getData());
            response.setDurationMs(step.getDurationMs());
            response.setTraceTimestamp(step.getTraceTimestamp());
            response.setCreatedAt(step.getCreatedAt());
            responses.add(response);
        }
        return responses;
    }

    private List<AgentWorkflowStage> safeStages(List<AgentWorkflowStage> stages) {
        return stages == null ? List.of() : stages;
    }

    private List<AgentWorkflowStep> safeSteps(List<AgentWorkflowStep> steps) {
        return steps == null ? List.of() : steps;
    }
}
