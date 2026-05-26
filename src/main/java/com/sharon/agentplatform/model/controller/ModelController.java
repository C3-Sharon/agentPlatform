package com.sharon.agentplatform.model.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.model.core.ModelConfig;
import com.sharon.agentplatform.model.core.ModelConfigStore;
import com.sharon.agentplatform.model.dto.ModelChatRequest;
import com.sharon.agentplatform.model.dto.ModelChatResponse;
import com.sharon.agentplatform.model.service.ModelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelService modelService;
    private final ModelConfigStore modelConfigStore;

    public ModelController(
            ModelService modelService,
            ModelConfigStore modelConfigStore
    ) {
        this.modelService = modelService;
        this.modelConfigStore = modelConfigStore;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> listModels() {
        List<Map<String, Object>> models = modelConfigStore.listAll()
                .stream()
                .map(this::toModelView)
                .toList();

        return ApiResponse.success(models);
    }

    @GetMapping("/{modelId}")
    public ApiResponse<Map<String, Object>> getModel(@PathVariable String modelId) {
        return modelConfigStore.getById(modelId)
                .map(config -> ApiResponse.success(toModelView(config)))
                .orElseGet(() -> ApiResponse.fail("Model not found: " + modelId));
    }

    @PostMapping("/test-chat")
    public ApiResponse<ModelChatResponse> testChat(
            @RequestBody ModelChatRequest request
    ) {
        String answer = modelService.chat(
                request.getModelId(),
                request.getMessage()
        );
        return ApiResponse.success(new ModelChatResponse(answer));
    }

    private Map<String, Object> toModelView(ModelConfig config) {
        return Map.of(
                "id", config.getId(),
                "displayName", config.getDisplayName(),
                "provider", config.getProvider(),
                "type", config.getType(),
                "capabilities", config.getCapabilities(),
                "modelName", config.getModelName() == null ? "" : config.getModelName(),
                "baseUrl", config.getBaseUrl() == null ? "" : config.getBaseUrl(),
                "enabled", config.isEnabled()
        );
    }
}
