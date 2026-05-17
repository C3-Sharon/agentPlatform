package com.sharon.agentplatform.agent.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String conversationId;
    private String modelId;
    private String message;
}
