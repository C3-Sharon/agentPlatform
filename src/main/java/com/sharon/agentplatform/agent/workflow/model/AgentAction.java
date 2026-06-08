package com.sharon.agentplatform.agent.workflow.model;

import java.time.LocalDateTime;

public class AgentAction {

    private Integer actionOrder;
    private Integer traceStepOrder;
    private String type;
    private String name;
    private String status;
    private Object input;
    private AgentObservation observation;
    private Long durationMs;
    private LocalDateTime traceTimestamp;
    private LocalDateTime createdAt;

    public Integer getActionOrder() {
        return actionOrder;
    }

    public void setActionOrder(Integer actionOrder) {
        this.actionOrder = actionOrder;
    }

    public Integer getTraceStepOrder() {
        return traceStepOrder;
    }

    public void setTraceStepOrder(Integer traceStepOrder) {
        this.traceStepOrder = traceStepOrder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getInput() {
        return input;
    }

    public void setInput(Object input) {
        this.input = input;
    }

    public AgentObservation getObservation() {
        return observation;
    }

    public void setObservation(AgentObservation observation) {
        this.observation = observation;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public LocalDateTime getTraceTimestamp() {
        return traceTimestamp;
    }

    public void setTraceTimestamp(LocalDateTime traceTimestamp) {
        this.traceTimestamp = traceTimestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
