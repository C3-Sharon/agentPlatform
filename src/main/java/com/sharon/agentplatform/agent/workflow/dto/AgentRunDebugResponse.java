package com.sharon.agentplatform.agent.workflow.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AgentRunDebugResponse {

    private String runId;
    private String conversationId;
    private String modelId;
    private String status;
    private String userMessage;
    private String answer;
    private String errorMessage;
    private Long durationMs;
    private LocalDateTime createdAt;
    private AgentRunDebugSummaryResponse summary;
    private List<String> debugHints;
    private Object replayContext;

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

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public AgentRunDebugSummaryResponse getSummary() {
        return summary;
    }

    public void setSummary(AgentRunDebugSummaryResponse summary) {
        this.summary = summary;
    }

    public List<String> getDebugHints() {
        return debugHints;
    }

    public void setDebugHints(List<String> debugHints) {
        this.debugHints = debugHints;
    }

    public Object getReplayContext() {
        return replayContext;
    }

    public void setReplayContext(Object replayContext) {
        this.replayContext = replayContext;
    }
}
