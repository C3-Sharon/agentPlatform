package com.sharon.agentplatform.agent.workflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AgentRunReflectionResponse {
    private String runId;
    private String conversationId;
    private String modelId;
    private String status;
    private List<String> whatWentWell;
    private List<String> whatNeedsAttention;
    private List<String> suggestedNextSteps;
    private LocalDateTime createdAt;

    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<String> getWhatWentWell() { return whatWentWell; }
    public void setWhatWentWell(List<String> whatWentWell) { this.whatWentWell = whatWentWell; }
    public List<String> getWhatNeedsAttention() { return whatNeedsAttention; }
    public void setWhatNeedsAttention(List<String> whatNeedsAttention) { this.whatNeedsAttention = whatNeedsAttention; }
    public List<String> getSuggestedNextSteps() { return suggestedNextSteps; }
    public void setSuggestedNextSteps(List<String> suggestedNextSteps) { this.suggestedNextSteps = suggestedNextSteps; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
