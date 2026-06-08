package com.sharon.agentplatform.agent.workflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AgentRunTimelineResponse {

    private String runId;
    private String conversationId;
    private String modelId;
    private String status;
    private LocalDateTime createdAt;
    private Integer itemCount;
    private List<AgentTimelineItemResponse> items;

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

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public List<AgentTimelineItemResponse> getItems() {
        return items;
    }

    public void setItems(List<AgentTimelineItemResponse> items) {
        this.items = items;
    }
}
