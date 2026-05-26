package com.sharon.agentplatform.model.core;

import lombok.Data;

import java.util.List;

@Data
public class ModelConfig {

    private String id;
    private String displayName;
    private String provider;
    private String type;
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Double temperature;
    private Boolean enabled = true;
    private List<String> capabilities;

    public String getProvider() {
        if (provider == null || provider.isBlank()) {
            return "unknown";
        }
        return provider;
    }

    public String getType() {
        if (type == null || type.isBlank()) {
            return "openai-compatible";
        }
        return type;
    }

    public boolean isEnabled() {
        return enabled == null || enabled;
    }

    public List<String> getCapabilities() {
        if (capabilities == null || capabilities.isEmpty()) {
            return List.of("chat");
        }
        return capabilities;
    }

    public ModelProvider getClientProvider() {
        String clientType = getType().toLowerCase();
        if ("mock".equals(clientType)) {
            return ModelProvider.MOCK;
        }
        if ("openai-compatible".equals(clientType)) {
            return ModelProvider.OPENAI_COMPATIBLE;
        }
        throw new IllegalArgumentException("Unsupported model type: " + getType());
    }
}
