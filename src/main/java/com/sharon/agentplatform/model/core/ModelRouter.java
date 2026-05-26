package com.sharon.agentplatform.model.core;

import com.sharon.agentplatform.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ModelRouter {

    private static final String DEFAULT_MODEL_ID = "mock-model";

    private final ModelConfigStore modelConfigStore;
    private final Map<ModelProvider, ModelClient> modelClients;

    public ModelRouter(
            ModelConfigStore modelConfigStore,
            Collection<ModelClient> modelClientList
    ) {
        this.modelConfigStore = modelConfigStore;
        this.modelClients = modelClientList.stream()
                .collect(Collectors.toMap(
                        ModelClient::provider,
                        Function.identity()
                ));
    }

    public String chat(String modelId, String systemPrompt, String userMessage) {
        String actualModelId = normalizeModelId(modelId);

        ModelConfig config = getRequiredConfig(actualModelId);

        ModelClient client = getClient(config);

        return client.chat(config, systemPrompt, userMessage);
    }

    public String visionChat(String modelId, String userMessage, String imageDataUrl) {
        String actualModelId = normalizeModelId(modelId);
        ModelConfig config = requireCapability(actualModelId, "vision");
        ModelClient client = getClient(config);
        return client.visionChat(config, userMessage, imageDataUrl);
    }

    public boolean supportsCapability(String modelId, String capability) {
        ModelConfig config = getRequiredConfig(normalizeModelId(modelId));
        return hasCapability(config, capability);
    }

    public ModelConfig requireCapability(String modelId, String capability) {
        ModelConfig config = getRequiredConfig(normalizeModelId(modelId));
        if (hasCapability(config, capability)) {
            return config;
        }
        throw new BusinessException("Model does not support " + capability + ": " + normalizeModelId(modelId));
    }

    private ModelConfig getRequiredConfig(String modelId) {
        ModelConfig config = modelConfigStore.getById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + modelId));

        if (!config.isEnabled()) {
            throw new BusinessException("Model is disabled: " + modelId);
        }

        return config;
    }

    private ModelClient getClient(ModelConfig config) {
        ModelClient client = modelClients.get(config.getClientProvider());

        if (client == null) {
            throw new IllegalStateException("No ModelClient found for model type: " + config.getType());
        }

        return client;
    }

    private boolean hasCapability(ModelConfig config, String capability) {
        String normalizedCapability = capability.toLowerCase(Locale.ROOT);
        return config.getCapabilities()
                .stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.equals(normalizedCapability)
                        || ("vision".equals(normalizedCapability) && "multimodal".equals(value)));
    }

    private String normalizeModelId(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return DEFAULT_MODEL_ID;
        }
        return modelId;
    }
}
