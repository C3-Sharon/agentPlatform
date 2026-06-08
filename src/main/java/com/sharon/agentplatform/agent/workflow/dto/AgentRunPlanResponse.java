package com.sharon.agentplatform.agent.workflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AgentRunPlanResponse {

    private String runId;
    private String conversationId;
    private String modelId;
    private String status;
    private LocalDateTime createdAt;
    private String summary;
    private List<AgentPlanStepResponse> steps;

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<AgentPlanStepResponse> getSteps() {
        return steps;
    }

    public void setSteps(List<AgentPlanStepResponse> steps) {
        this.steps = steps;
    }
}
