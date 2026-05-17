package com.sharon.agentplatform.memory.core;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
public class LongTermMemory {

    private String id;
    private String conversationId;
    private String key;
    private String value;
    private LocalDateTime createdAt;

    public LongTermMemory() {
    }

    public LongTermMemory(String conversationId, String key, String value) {
        this.id = UUID.randomUUID().toString();
        this.conversationId = conversationId;
        this.key = key;
        this.value = value;
        this.createdAt = LocalDateTime.now();
    }
}