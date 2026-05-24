package com.sharon.agentplatform.memory.store;

import com.sharon.agentplatform.memory.core.ChatMessage;
import com.sharon.agentplatform.memory.core.MemoryRole;
import com.sharon.agentplatform.memory.entity.ConversationMessageEntity;
import com.sharon.agentplatform.memory.repository.ConversationMessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ShortTermMemoryStore {

    private static final int MAX_MESSAGES_PER_CONVERSATION = 20;

    private final ConversationMessageRepository conversationMessageRepository;

    public ShortTermMemoryStore(ConversationMessageRepository conversationMessageRepository) {
        this.conversationMessageRepository = conversationMessageRepository;
    }

    public void addMessage(String conversationId, ChatMessage message) {
        if (conversationId == null || conversationId.isBlank() || message == null) {
            return;
        }
        if (message.getContent() == null || message.getContent().isBlank()) {
            return;
        }

        ConversationMessageEntity entity = new ConversationMessageEntity();
        entity.setConversationId(conversationId);
        entity.setRole(message.getRole() == null ? MemoryRole.SYSTEM.name() : message.getRole().name());
        entity.setContent(message.getContent());
        entity.setModelId(null);
        entity.setMetadataJson(null);
        entity.setCreatedAt(message.getTimestamp() == null ? LocalDateTime.now() : message.getTimestamp());
        conversationMessageRepository.save(entity);
    }

    public List<ChatMessage> getMessages(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return List.of();
        }

        List<ConversationMessageEntity> entities = new ArrayList<>(
                conversationMessageRepository.findTop20ByConversationIdOrderByCreatedAtDesc(conversationId)
        );
        entities.sort(Comparator.comparing(ConversationMessageEntity::getCreatedAt));

        return entities.stream()
                .map(this::toChatMessage)
                .toList();
    }

    public List<ConversationMessageEntity> listAllMessages(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return List.of();
        }
        return conversationMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    @Transactional
    public void clear(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        conversationMessageRepository.deleteByConversationId(conversationId);
    }

    private ChatMessage toChatMessage(ConversationMessageEntity entity) {
        return new ChatMessage(toMemoryRole(entity.getRole()), entity.getContent(), entity.getCreatedAt());
    }

    private MemoryRole toMemoryRole(String role) {
        if (role == null || role.isBlank()) {
            return MemoryRole.SYSTEM;
        }
        try {
            return MemoryRole.valueOf(role);
        } catch (IllegalArgumentException exception) {
            return MemoryRole.SYSTEM;
        }
    }
}
