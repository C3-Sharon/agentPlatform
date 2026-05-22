package com.sharon.agentplatform.agent.core;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PendingSkillCall {

    private String conversationId;
    private String skillName;
    private Map<String, Object> knownParams;
    private List<String> missingParams;
    private LocalDateTime createdAt;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Map<String, Object> getKnownParams() {
        return knownParams;
    }

    public void setKnownParams(Map<String, Object> knownParams) {
        this.knownParams = knownParams;
    }

    public List<String> getMissingParams() {
        return missingParams;
    }

    public void setMissingParams(List<String> missingParams) {
        this.missingParams = missingParams;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
