package com.sharon.agentplatform.agent.workflow.model;

import java.time.LocalDateTime;
import java.util.List;

public class AgentWorkflowRun {

    private String runId;
    private String conversationId;
    private String modelId;
    private String userMessage;
    private String answer;
    private String status;
    private String errorMessage;
    private Long durationMs;
    private LocalDateTime createdAt;
    private List<AgentWorkflowStage> stages;
    private List<AgentDecision> decisions;
    private List<AgentAction> actions;
    private AgentReflection reflection;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public List<AgentWorkflowStage> getStages() {
        return stages;
    }

    public void setStages(List<AgentWorkflowStage> stages) {
        this.stages = stages;
    }

    public List<AgentDecision> getDecisions() {
        return decisions;
    }

    public void setDecisions(List<AgentDecision> decisions) {
        this.decisions = decisions;
    }

    public List<AgentAction> getActions() {
        return actions;
    }

    public void setActions(List<AgentAction> actions) {
        this.actions = actions;
    }

    public AgentReflection getReflection() {
        return reflection;
    }

    public void setReflection(AgentReflection reflection) {
        this.reflection = reflection;
    }
}
