package com.sharon.agentplatform.model.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.common.exception.ModelCallException;
import com.sharon.agentplatform.model.core.ModelRouter;
import com.sharon.agentplatform.model.dto.VisionChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
public class ModelService {

    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;

    private final ModelRouter modelRouter;

    public ModelService(ModelRouter modelRouter) {
        this.modelRouter = modelRouter;
    }

    public String chat(String modelId, String userMessage) {
        return chatWithContext(
                modelId,
                """
                你是 AI Agent Platform 中的默认智能助手。
                你的回答要清晰、简洁、有帮助。
                """,
                userMessage
        );
    }

    public String chatWithContext(String modelId, String systemPrompt, String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            return "请输入有效的问题。";
        }

        try {
            return modelRouter.chat(modelId, systemPrompt, userMessage);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ModelCallException("Model call failed for modelId: " + modelId, exception);
        }
    }

    public VisionChatResponse visionChat(String modelId, String message, MultipartFile image) {
        validateVisionChatRequest(modelId, message, image);
        modelRouter.requireCapability(modelId, "vision");

        try {
            String contentType = image.getContentType();
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
            String dataUrl = "data:" + contentType + ";base64," + base64Image;
            String answer = modelRouter.visionChat(modelId, message, dataUrl);

            VisionChatResponse response = new VisionChatResponse();
            response.setModelId(modelId);
            response.setAnswer(answer);
            response.setImageFileName(image.getOriginalFilename());
            response.setContentType(contentType);
            response.setSizeBytes(image.getSize());
            return response;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ModelCallException("Vision model call failed for modelId: " + modelId, exception);
        }
    }

    private void validateVisionChatRequest(String modelId, String message, MultipartFile image) {
        if (modelId == null || modelId.isBlank()) {
            throw new BusinessException("modelId is required");
        }
        if (message == null || message.isBlank()) {
            throw new BusinessException("message is required");
        }
        if (image == null || image.isEmpty()) {
            throw new BusinessException("image is required");
        }
        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new BusinessException("Image is too large, max 5MB");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("image contentType must start with image/");
        }
    }
}
