package com.sharon.agentplatform.memory.store;

import com.sharon.agentplatform.memory.core.ChatMessage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ShortTermMemoryStore {

    private static final int MAX_MESSAGES_PER_CONVERSATION = 20;

    private final Map<String, List<ChatMessage>> conversationMessages = new ConcurrentHashMap<>();

    public void addMessage(String conversationId, ChatMessage message) {
        List<ChatMessage> messages = conversationMessages.computeIfAbsent(
                conversationId,
                key -> Collections.synchronizedList(new ArrayList<>())
        );

        messages.add(message);

        trimIfNecessary(messages);
    }

    public List<ChatMessage> getMessages(String conversationId) {
        List<ChatMessage> messages = conversationMessages.get(conversationId);

        if (messages == null) {
            return List.of();
        }

        synchronized (messages) {
            return new ArrayList<>(messages);
        }
    }

    public void clear(String conversationId) {
        conversationMessages.remove(conversationId);
    }

    private void trimIfNecessary(List<ChatMessage> messages) {
        synchronized (messages) {
            while (messages.size() > MAX_MESSAGES_PER_CONVERSATION) {
                messages.remove(0);
            }
        }
    }
}