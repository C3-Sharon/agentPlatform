package com.sharon.agentplatform.model.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.common.exception.BusinessException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenAiCompatibleModelClient implements ModelClient {

    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public OpenAiCompatibleModelClient(ObjectMapper objectMapper) {
        this.restClient = RestClient.builder().build();
        this.objectMapper = objectMapper;
    }

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

    @Override
    public String visionChat(ModelConfig config, String userMessage, String imageDataUrl) {
        Map<String, Object> requestBody = Map.of(
                "model", config.getModelName(),
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(
                                        Map.of(
                                                "type", "text",
                                                "text", userMessage
                                        ),
                                        Map.of(
                                                "type", "image_url",
                                                "image_url", Map.of("url", imageDataUrl)
                                        )
                                )
                        )
                )
        );

        try {
            RestClient.RequestBodySpec requestSpec = restClient.post()
                    .uri(buildChatCompletionsUrl(config))
                    .contentType(MediaType.APPLICATION_JSON);

            if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
                requestSpec = requestSpec.header("Authorization", "Bearer " + config.getApiKey());
            }

            String responseBody = requestSpec
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return extractAnswer(responseBody);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("OpenAI-compatible vision call failed: " + exception.getMessage(), exception);
        }
    }

    private String buildChatCompletionsUrl(ModelConfig config) {
        String baseUrl = config.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new BusinessException("Model baseUrl is required for vision chat: " + config.getId());
        }

        String normalizedBaseUrl = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;

        if (normalizedBaseUrl.endsWith("/chat/completions")) {
            return normalizedBaseUrl;
        }
        if (normalizedBaseUrl.endsWith("/v1")) {
            return normalizedBaseUrl + "/chat/completions";
        }
        return normalizedBaseUrl + "/v1/chat/completions";
    }

    private String extractAnswer(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new BusinessException("Vision model returned empty response");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isTextual()) {
                return content.asText();
            }
            if (!content.isMissingNode() && !content.isNull()) {
                return content.toString();
            }
        } catch (Exception exception) {
            throw new BusinessException("Failed to parse vision model response: " + exception.getMessage(), exception);
        }

        throw new BusinessException("Vision model response does not contain answer content");
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
