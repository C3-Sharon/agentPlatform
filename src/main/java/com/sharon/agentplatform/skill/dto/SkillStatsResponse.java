package com.sharon.agentplatform.skill.dto;

import java.time.LocalDateTime;

public class SkillStatsResponse {

    private String skillName;
    private Long callCount;
    private Long successCount;
    private Long failCount;
    private Double avgDurationMs;
    private LocalDateTime lastCalledAt;

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Long getCallCount() {
        return callCount;
    }

    public void setCallCount(Long callCount) {
        this.callCount = callCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getFailCount() {
        return failCount;
    }

    public void setFailCount(Long failCount) {
        this.failCount = failCount;
    }

    public Double getAvgDurationMs() {
        return avgDurationMs;
    }

    public void setAvgDurationMs(Double avgDurationMs) {
        this.avgDurationMs = avgDurationMs;
    }

    public LocalDateTime getLastCalledAt() {
        return lastCalledAt;
    }

    public void setLastCalledAt(LocalDateTime lastCalledAt) {
        this.lastCalledAt = lastCalledAt;
    }
}
