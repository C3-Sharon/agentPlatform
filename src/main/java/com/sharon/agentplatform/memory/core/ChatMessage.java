package com.sharon.agentplatform.memory.core;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ChatMessage {

    private MemoryRole role;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessage() {
    }

    public ChatMessage(MemoryRole role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(MemoryRole role, String content, LocalDateTime timestamp) {
        this.role = role;
        this.content = content;
        this.timestamp = timestamp;
    }

}