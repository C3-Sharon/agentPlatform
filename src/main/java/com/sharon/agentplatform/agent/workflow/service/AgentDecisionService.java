package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionResponse;
import com.sharon.agentplatform.agent.workflow.dto.AgentDecisionViewResponse;
import com.sharon.agentplatform.agent.workflow.model.AgentDecision;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowRun;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AgentDecisionService {

    private final AgentWorkflowModelService agentWorkflowModelService;

    public AgentDecisionService(AgentWorkflowModelService agentWorkflowModelService) {
        this.agentWorkflowModelService = agentWorkflowModelService;
    }

    public AgentDecisionViewResponse getDecisions(String runId) {
        AgentWorkflowRun workflowRun = agentWorkflowModelService.getWorkflowRun(runId);
        List<AgentDecisionResponse> decisions = toDecisionResponses(workflowRun.getDecisions());

        AgentDecisionViewResponse response = new AgentDecisionViewResponse();
        response.setRunId(workflowRun.getRunId());
        response.setConversationId(workflowRun.getConversationId());
        response.setModelId(workflowRun.getModelId());
        response.setStatus(workflowRun.getStatus());
        response.setCreatedAt(workflowRun.getCreatedAt());
        response.setDecisionCount(decisions.size());
        response.setDecisions(decisions);
        return response;
    }

    private List<AgentDecisionResponse> toDecisionResponses(List<AgentDecision> decisions) {
        List<AgentDecisionResponse> responses = new ArrayList<>();
        for (AgentDecision decision : safeDecisions(decisions)) {
            AgentDecisionResponse response = new AgentDecisionResponse();
            response.setDecisionOrder(decision.getDecisionOrder());
            response.setTraceStepOrder(decision.getTraceStepOrder());
            response.setType(decision.getType());
            response.setSource(decision.getSource());
            response.setStatus(decision.getStatus());
            response.setSummary(decision.getSummary());
            response.setIntent(decision.getIntent());
            response.setNeedSkill(decision.getNeedSkill());
            response.setSkillName(decision.getSkillName());
            response.setParams(decision.getParams());
            response.setMissingParams(decision.getMissingParams());
            response.setReason(decision.getReason());
            response.setPendingStore(decision.getPendingStore());
            response.setRawData(decision.getRawData());
            response.setDurationMs(decision.getDurationMs());
            response.setTraceTimestamp(decision.getTraceTimestamp());
            response.setCreatedAt(decision.getCreatedAt());
            responses.add(response);
        }
        return responses;
    }

    private List<AgentDecision> safeDecisions(List<AgentDecision> decisions) {
        return decisions == null ? List.of() : decisions;
    }
}
