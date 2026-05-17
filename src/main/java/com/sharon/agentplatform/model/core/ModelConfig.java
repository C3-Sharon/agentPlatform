package com.sharon.agentplatform.model.core;

import lombok.Data;

@Data
public class ModelConfig {

    private String id;
    private ModelProvider provider;
    private String displayName;
    private String baseUrl;
    private String apiKey;
    private String modelName;
    private Double temperature;
    private boolean enabled = true;



}
