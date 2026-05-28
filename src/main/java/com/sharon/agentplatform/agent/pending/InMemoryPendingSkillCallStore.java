package com.sharon.agentplatform.agent.pending;

import com.sharon.agentplatform.agent.core.PendingSkillCall;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@ConditionalOnProperty(prefix = "agentplatform.pending-skill", name = "store-type", havingValue = "memory", matchIfMissing = true)
public class InMemoryPendingSkillCallStore implements PendingSkillCallStore {

    private final ConcurrentMap<String, PendingSkillCall> pendingCalls = new ConcurrentHashMap<>();

    @Override
    public void save(String conversationId, PendingSkillCall pendingSkillCall) {
        if (conversationId == null || conversationId.isBlank() || pendingSkillCall == null) {
            return;
        }
        pendingCalls.put(conversationId, pendingSkillCall);
    }

    @Override
    public Optional<PendingSkillCall> get(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(pendingCalls.get(conversationId));
    }

    @Override
    public void remove(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        pendingCalls.remove(conversationId);
    }

    @Override
    public String storeType() {
        return "memory";
    }
}
