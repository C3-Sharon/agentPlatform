package com.sharon.agentplatform.model.dto;

import lombok.Data;

@Data
public class ModelChatResponse {
    private String answer;
public ModelChatResponse(String answer) {
    this.answer = answer;
}
}
