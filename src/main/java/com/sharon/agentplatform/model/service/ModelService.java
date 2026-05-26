package com.sharon.agentplatform.model.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.common.exception.ModelCallException;
import com.sharon.agentplatform.model.core.ModelRouter;
import org.springframework.stereotype.Service;

@Service
public class ModelService {

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
}
