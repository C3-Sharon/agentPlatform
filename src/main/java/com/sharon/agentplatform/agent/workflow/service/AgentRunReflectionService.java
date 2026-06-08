package com.sharon.agentplatform.agent.workflow.service;

import com.sharon.agentplatform.agent.workflow.dto.AgentRunReflectionResponse;
import com.sharon.agentplatform.agent.workflow.model.AgentReflection;
import com.sharon.agentplatform.agent.workflow.model.AgentWorkflowRun;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentRunReflectionService {

    private final AgentWorkflowModelService agentWorkflowModelService;

    public AgentRunReflectionService(AgentWorkflowModelService agentWorkflowModelService) {
        this.agentWorkflowModelService = agentWorkflowModelService;
    }

    public AgentRunReflectionResponse reflect(String runId) {
        AgentWorkflowRun workflowRun = agentWorkflowModelService.getWorkflowRun(runId);
        AgentReflection reflection = workflowRun.getReflection();

        AgentRunReflectionResponse response = new AgentRunReflectionResponse();
        response.setRunId(workflowRun.getRunId());
        response.setConversationId(workflowRun.getConversationId());
        response.setModelId(workflowRun.getModelId());
        response.setStatus(workflowRun.getStatus());
        response.setCreatedAt(workflowRun.getCreatedAt());
        response.setWhatWentWell(reflection == null ? List.of() : reflection.getWhatWentWell());
        response.setWhatNeedsAttention(reflection == null ? List.of() : reflection.getWhatNeedsAttention());
        response.setSuggestedNextSteps(reflection == null ? List.of() : reflection.getSuggestedNextSteps());
        return response;
    }
}
