package com.sharon.agentplatform.model.core;

import org.springframework.stereotype.Component;

@Component
public class MockModelClient implements ModelClient {

    @Override
    public ModelProvider provider() {
        return ModelProvider.MOCK;
    }

    @Override
    public String chat(ModelConfig config, String systemPrompt, String userMessage) {
        return "这是 mock-model 的回复。你刚才的问题是：" + userMessage;
    }
}
