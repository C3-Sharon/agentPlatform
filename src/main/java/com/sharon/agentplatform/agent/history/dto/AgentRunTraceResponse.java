package com.sharon.agentplatform.agent.history.dto;

import java.time.LocalDateTime;

public class AgentRunTraceResponse {

    private Integer stepOrder;
    private String step;
    private String status;
    private String detail;
    private Object data;
    private Long durationMs;
    private LocalDateTime traceTimestamp;
    private LocalDateTime createdAt;

    public Integer getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
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
