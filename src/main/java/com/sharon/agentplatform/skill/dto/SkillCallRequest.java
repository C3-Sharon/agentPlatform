package com.sharon.agentplatform.skill.dto;

import java.util.HashMap;
import java.util.Map;

public class SkillCallRequest {

    private Map<String, Object> params = new HashMap<>();

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
