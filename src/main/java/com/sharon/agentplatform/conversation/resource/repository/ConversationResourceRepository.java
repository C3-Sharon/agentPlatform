package com.sharon.agentplatform.conversation.resource.repository;

import com.sharon.agentplatform.conversation.resource.entity.ConversationResourceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationResourceRepository extends JpaRepository<ConversationResourceEntity, Long> {

    List<ConversationResourceEntity> findByConversationIdOrderByCreatedAtDesc(String conversationId);

    Optional<ConversationResourceEntity> findFirstByConversationIdAndResourceTypeOrderByCreatedAtDesc(String conversationId, String resourceType);
}
