package com.sharon.agentplatform.agent.core;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
@Data
public class AgentTrace {

    private AgentStep step;
    private AgentTraceStatus status;
    private String detail;
    private Map<String, Object> data;
    private LocalDateTime timestamp;
    private Long durationMs;

    public AgentTrace(
            AgentStep step,
            AgentTraceStatus status,
            String detail,
            Map<String, Object> data,
            Long durationMs
    ) {
        this.step = step;
        this.status = status;
        this.detail = detail;
        this.data = data;
        this.timestamp = LocalDateTime.now();
        this.durationMs = durationMs;
    }

    public static AgentTrace success(AgentStep step, String detail) {
        return new AgentTrace(step, AgentTraceStatus.SUCCESS, detail, Map.of(), null);
    }

    public static AgentTrace success(AgentStep step, String detail, Map<String, Object> data) {
        return new AgentTrace(step, AgentTraceStatus.SUCCESS, detail, data, null);
    }

    public static AgentTrace failed(AgentStep step, String detail) {
        return new AgentTrace(step, AgentTraceStatus.FAILED, detail, Map.of(), null);
    }

    public static AgentTrace failed(AgentStep step, String detail, Map<String, Object> data) {
        return new AgentTrace(step, AgentTraceStatus.FAILED, detail, data, null);
    }

    public static AgentTrace skipped(AgentStep step, String detail) {
        return new AgentTrace(step, AgentTraceStatus.SKIPPED, detail, Map.of(), null);
    }

    public static AgentTrace withDuration(
            AgentStep step,
            AgentTraceStatus status,
            String detail,
            Map<String, Object> data,
            Long durationMs
    ) {
        return new AgentTrace(step, status, detail, data, durationMs);
    }


}