package com.sharon.agentplatform.model.core;

import com.sharon.agentplatform.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Collection;
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

        ModelConfig config = modelConfigStore.getById(actualModelId)
                .orElseThrow(() -> new IllegalArgumentException("Model not found: " + actualModelId));

        if (!config.isEnabled()) {
            throw new BusinessException("Model is disabled: " + actualModelId);
        }

        ModelClient client = modelClients.get(config.getClientProvider());

        if (client == null) {
            throw new IllegalStateException("No ModelClient found for model type: " + config.getType());
        }

        return client.chat(config, systemPrompt, userMessage);
    }

    private String normalizeModelId(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return DEFAULT_MODEL_ID;
        }
        return modelId;
    }
}
