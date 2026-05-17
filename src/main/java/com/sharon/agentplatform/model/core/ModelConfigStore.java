package com.sharon.agentplatform.model.core;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ModelConfigStore {

    private final ModelProperties modelProperties;
    private final Map<String, ModelConfig> modelConfigMap = new ConcurrentHashMap<>();

    public ModelConfigStore(ModelProperties modelProperties) {
        this.modelProperties = modelProperties;
    }

    @PostConstruct
    public void init() {
        for (ModelConfig config : modelProperties.getModels()) {
            if (config.getId() == null || config.getId().isBlank()) {
                throw new IllegalArgumentException("Model id must not be blank");
            }

            if (modelConfigMap.containsKey(config.getId())) {
                throw new IllegalStateException("Duplicate model id: " + config.getId());
            }

            modelConfigMap.put(config.getId(), config);
        }
    }

    public Optional<ModelConfig> getById(String modelId) {
        return Optional.ofNullable(modelConfigMap.get(modelId));
    }

    public List<ModelConfig> listAll() {
        return List.copyOf(modelConfigMap.values());
    }

    public List<ModelConfig> listEnabled() {
        return modelConfigMap.values()
                .stream()
                .filter(ModelConfig::isEnabled)
                .toList();
    }
}