package com.sharon.agentplatform.memory.store;

import com.sharon.agentplatform.memory.core.LongTermMemory;

import java.util.List;

public interface LongTermMemoryStore {

    void add(LongTermMemory memory);

    List<LongTermMemory> listByConversationId(String conversationId);

    void clearByConversationId(String conversationId);
}