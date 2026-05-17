package com.sharon.agentplatform.model.core;

public interface ModelClient {

    ModelProvider provider();

    String chat(ModelConfig config, String systemPrompt, String userMessage);
}
