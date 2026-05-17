package com.sharon.agentplatform.model.core;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenAiCompatibleModelClient implements ModelClient {

    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    @Override
    public ModelProvider provider() {
        return ModelProvider.OPENAI_COMPATIBLE;
    }

    @Override
    public String chat(ModelConfig config, String systemPrompt, String userMessage) {
        ChatClient chatClient = chatClientCache.computeIfAbsent(
                config.getId(),
                key -> createChatClient(config)
        );

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .content();
    }

    private ChatClient createChatClient(ModelConfig config) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .build();

        OpenAiChatOptions.Builder optionsBuilder = OpenAiChatOptions.builder()
                .model(config.getModelName());

        if (config.getTemperature() != null) {
            optionsBuilder.temperature(config.getTemperature());
        }

        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(optionsBuilder.build())
                .build();

        return ChatClient.builder(chatModel)
                .defaultSystem("""
                        你是 AI Agent Platform 中的智能助手。
                        你的回答要清晰、准确、有帮助。
                        """)
                .build();
    }
}