package com.sharon.agentplatform.model.core;

import com.sharon.agentplatform.common.exception.BusinessException;

public interface ModelClient {

    ModelProvider provider();

    String chat(ModelConfig config, String systemPrompt, String userMessage);

    default String visionChat(ModelConfig config, String userMessage, String imageDataUrl) {
        throw new BusinessException("Model client does not support vision: " + config.getId());
    }
}
