package com.sharon.agentplatform.agent.pending;

import com.sharon.agentplatform.agent.core.PendingSkillCall;

import java.util.Optional;

public interface PendingSkillCallStore {

    void save(String conversationId, PendingSkillCall pendingSkillCall);

    Optional<PendingSkillCall> get(String conversationId);

    void remove(String conversationId);

    String storeType();
}
