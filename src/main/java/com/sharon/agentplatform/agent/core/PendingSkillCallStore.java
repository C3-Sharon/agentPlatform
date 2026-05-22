package com.sharon.agentplatform.agent.core;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class PendingSkillCallStore {

    private final ConcurrentMap<String, PendingSkillCall> pendingCalls = new ConcurrentHashMap<>();

    public void save(PendingSkillCall pending) {
        if (pending == null || pending.getConversationId() == null || pending.getConversationId().isBlank()) {
            return;
        }
        pendingCalls.put(pending.getConversationId(), pending);
    }

    public Optional<PendingSkillCall> get(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(pendingCalls.get(conversationId));
    }

    public void clear(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        pendingCalls.remove(conversationId);
    }
}
