package com.sharon.agentplatform.agent.workflow.model;

import java.time.LocalDateTime;
import java.util.Map;

public class AgentDecision {

    private Integer decisionOrder;
    private Integer traceStepOrder;
    private String type;
    private String source;
    private String status;
    private String summary;
    private String intent;
    private Boolean needSkill;
    private String skillName;
    private Object params;
    private Object missingParams;
    private String reason;
    private String pendingStore;
    private Map<String, Object> rawData;
    private Long durationMs;
    private LocalDateTime traceTimestamp;
    private LocalDateTime createdAt;

    public Integer getDecisionOrder() {
        return decisionOrder;
    }

    public void setDecisionOrder(Integer decisionOrder) {
        this.decisionOrder = decisionOrder;
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Boolean getNeedSkill() {
        return needSkill;
    }

    public void setNeedSkill(Boolean needSkill) {
        this.needSkill = needSkill;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Object getMissingParams() {
        return missingParams;
    }

    public void setMissingParams(Object missingParams) {
        this.missingParams = missingParams;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPendingStore() {
        return pendingStore;
    }

    public void setPendingStore(String pendingStore) {
        this.pendingStore = pendingStore;
    }

    public Map<String, Object> getRawData() {
        return rawData;
    }

    public void setRawData(Map<String, Object> rawData) {
        this.rawData = rawData;
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
