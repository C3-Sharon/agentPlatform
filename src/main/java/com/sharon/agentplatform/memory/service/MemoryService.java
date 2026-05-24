package com.sharon.agentplatform.memory.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.memory.core.ChatMessage;
import com.sharon.agentplatform.memory.core.LongTermMemory;
import com.sharon.agentplatform.memory.core.MemoryRole;
import com.sharon.agentplatform.memory.dto.ConversationMessageResponse;
import com.sharon.agentplatform.memory.dto.MemoryViewResponse;
import com.sharon.agentplatform.memory.entity.ConversationMessageEntity;
import com.sharon.agentplatform.memory.store.LongTermMemoryStore;
import com.sharon.agentplatform.memory.store.ShortTermMemoryStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryService {

    private final ShortTermMemoryStore shortTermMemoryStore;
    private final LongTermMemoryStore longTermMemoryStore;
    private final ObjectMapper objectMapper;

    public MemoryService(
            ShortTermMemoryStore shortTermMemoryStore,
            LongTermMemoryStore longTermMemoryStore,
            ObjectMapper objectMapper
    ) {
        this.shortTermMemoryStore = shortTermMemoryStore;
        this.longTermMemoryStore = longTermMemoryStore;
        this.objectMapper = objectMapper;
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

    public List<ConversationMessageResponse> getConversationMessages(String conversationId) {
        return shortTermMemoryStore.listAllMessages(conversationId)
                .stream()
                .map(this::toConversationMessageResponse)
                .toList();
    }

    public void clearConversationMemory(String conversationId) {
        shortTermMemoryStore.clear(conversationId);
        longTermMemoryStore.clearByConversationId(conversationId);
    }

    private ConversationMessageResponse toConversationMessageResponse(ConversationMessageEntity entity) {
        ConversationMessageResponse response = new ConversationMessageResponse();
        response.setConversationId(entity.getConversationId());
        response.setRole(entity.getRole());
        response.setContent(entity.getContent());
        response.setModelId(entity.getModelId());
        response.setMetadata(parseMetadata(entity.getMetadataJson()));
        response.setCreatedAt(entity.getCreatedAt());
        return response;
    }

    private Object parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(metadataJson, Object.class);
        } catch (Exception exception) {
            return metadataJson;
        }
    }
}
