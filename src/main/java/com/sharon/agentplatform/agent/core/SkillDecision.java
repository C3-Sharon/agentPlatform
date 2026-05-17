package com.sharon.agentplatform.agent.core;

import java.util.HashMap;
import java.util.Map;

public class SkillDecision {

    private boolean needSkill;
    private String skillName;
    private Map<String, Object> params = new HashMap<>();
    private String reason;

    public boolean isNeedSkill() {
        return needSkill;
    }

    public void setNeedSkill(boolean needSkill) {
        this.needSkill = needSkill;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params == null ? new HashMap<>() : params;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static SkillDecision noSkill(String reason) {
        SkillDecision decision = new SkillDecision();
        decision.setNeedSkill(false);
        decision.setSkillName("none");
        decision.setReason(reason);
        decision.setParams(Map.of());
        return decision;
    }
}
