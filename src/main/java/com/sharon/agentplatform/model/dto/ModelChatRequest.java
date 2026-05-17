package com.sharon.agentplatform.model.dto;

import lombok.Data;

@Data
public class ModelChatRequest {

    private String message;
    private String modelId;

}