package com.sharon.agentplatform.skill.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SkillContext {

    private final Map<String, Object> params;
    private final Map<String, Object> attributes;

    public SkillContext(Map<String, Object> params) {
        this.params = params == null ? new HashMap<>() : new HashMap<>(params);
        this.attributes = new HashMap<>();
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    public String getStringParam(String key) {
        Object value = params.get(key);
        return value == null ? null : value.toString();
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }
}
