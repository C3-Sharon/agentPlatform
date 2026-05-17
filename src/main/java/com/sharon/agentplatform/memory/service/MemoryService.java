package com.sharon.agentplatform.memory.service;

import com.sharon.agentplatform.memory.core.ChatMessage;
import com.sharon.agentplatform.memory.core.LongTermMemory;
import com.sharon.agentplatform.memory.core.MemoryRole;
import com.sharon.agentplatform.memory.dto.MemoryViewResponse;
import com.sharon.agentplatform.memory.store.LongTermMemoryStore;
import com.sharon.agentplatform.memory.store.ShortTermMemoryStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryService {

    private final ShortTermMemoryStore shortTermMemoryStore;
    private final LongTermMemoryStore longTermMemoryStore;

    public MemoryService(
            ShortTermMemoryStore shortTermMemoryStore,
            LongTermMemoryStore longTermMemoryStore
    ) {
        this.shortTermMemoryStore = shortTermMemoryStore;
        this.longTermMemoryStore = longTermMemoryStore;
    }

    public void addUserMessage(String conversationId, String content) {
        shortTermMemoryStore.addMessage(
                conversationId,
                new ChatMessage(MemoryRole.USER, content)
        );
    }

    public void addAssistantMessage(String conversationId, String content) {
        shortTermMemoryStore.addMessage(
                conversationId,
                new ChatMessage(MemoryRole.ASSISTANT, content)
        );
    }

    public List<ChatMessage> getShortTermMessages(String conversationId) {
        return shortTermMemoryStore.getMessages(conversationId);
    }

    public void addLongTermMemory(String conversationId, String key, String value) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Memory key must not be blank");
        }

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Memory value must not be blank");
        }

        longTermMemoryStore.add(
                new LongTermMemory(conversationId, key, value)
        );
    }

    public List<LongTermMemory> getLongTermMemories(String conversationId) {
        return longTermMemoryStore.listByConversationId(conversationId);
    }

    public MemoryViewResponse getMemoryView(String conversationId) {
        return new MemoryViewResponse(
                conversationId,
                getShortTermMessages(conversationId),
                getLongTermMemories(conversationId)
        );
    }

    public void clearConversationMemory(String conversationId) {
        shortTermMemoryStore.clear(conversationId);
        longTermMemoryStore.clearByConversationId(conversationId);
    }
}