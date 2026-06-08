package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.workflow.dto.AgentActionObservationResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentActionResponse;
import com.sharon.agentplatform.agent.workflow.model.AgentAction;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowRun;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentActionObservationService {

    private final AgentWorkflowModelService agentWorkflowModelService;

    public AgentActionObservationService(AgentWorkflowModelService agentWorkflowModelService) {
        this.agentWorkflowModelService = agentWorkflowModelService;
    }

    public AgentActionObservationResponse getActions(String runId) {
        AgentWorkflowRun workflowRun = agentWorkflowModelService.getWorkflowRun(runId);
        List<AgentActionResponse> actions = toActionResponses(workflowRun.getActions());

        AgentActionObservationResponse response = new AgentActionObservationResponse();
        response.setRunId(workflowRun.getRunId());
        response.setConversationId(workflowRun.getConversationId());
        response.setModelId(workflowRun.getModelId());
        response.setStatus(workflowRun.getStatus());
        response.setCreatedAt(workflowRun.getCreatedAt());
        response.setActionCount(actions.size());
        response.setActions(actions);
        return response;
    }

    private List<AgentActionResponse> toActionResponses(List<AgentAction> actions) {
        List<AgentActionResponse> responses = new ArrayList<>();
        for (AgentAction action : safeActions(actions)) {
            AgentActionResponse response = new AgentActionResponse();
            response.setActionOrder(action.getActionOrder());
            response.setTraceStepOrder(action.getTraceStepOrder());
            response.setType(action.getType());
            response.setName(action.getName());
            response.setStatus(action.getStatus());
            response.setInput(action.getInput());
            response.setObservation(action.getObservation() == null ? null : action.getObservation().getData());
            response.setErrorMessage(action.getObservation() == null ? null : action.getObservation().getErrorMessage());
            response.setDurationMs(action.getDurationMs());
            response.setTraceTimestamp(action.getTraceTimestamp());
            response.setCreatedAt(action.getCreatedAt());
            responses.add(response);
        }
        return responses;
    }

    private List<AgentAction> safeActions(List<AgentAction> actions) {
        return actions == null ? List.of() : actions;
    }
}
