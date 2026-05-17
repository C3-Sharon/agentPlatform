package com.sharon.agentplatform.memory.dto;

import com.sharon.agentplatform.memory.core.ChatMessage;
import com.sharon.agentplatform.memory.core.LongTermMemory;

import java.util.List;

public class MemoryViewResponse {

    private String conversationId;
    private List<ChatMessage> shortTermMessages;
    private List<LongTermMemory> longTermMemories;

    public MemoryViewResponse(
            String conversationId,
            List<ChatMessage> shortTermMessages,
            List<LongTermMemory> longTermMemories
    ) {
        this.conversationId = conversationId;
        this.shortTermMessages = shortTermMessages;
        this.longTermMemories = longTermMemories;
    }

    public String getConversationId() {
        return conversationId;
    }

    public List<ChatMessage> getShortTermMessages() {
        return shortTermMessages;
    }

    public List<LongTermMemory> getLongTermMemories() {
        return longTermMemories;
    }
}