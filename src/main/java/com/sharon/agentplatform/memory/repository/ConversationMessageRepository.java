package com.sharon.agentplatform.memory.repository;

import com.sharon.agentplatform.memory.entity.ConversationMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessageEntity, Long> {

    List<ConversationMessageEntity> findTop20ByConversationIdOrderByCreatedAtDesc(String conversationId);

    List<ConversationMessageEntity> findByConversationIdOrderByCreatedAtAsc(String conversationId);

    void deleteByConversationId(String conversationId);
}
