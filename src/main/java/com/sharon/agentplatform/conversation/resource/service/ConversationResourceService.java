package com.sharon.agentplatform.conversation.resource.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.conversation.resource.dto.ConversationResourceResponse;
import com.sharon.agentplatform.conversation.resource.entity.ConversationResourceEntity;
import com.sharon.agentplatform.conversation.resource.repository.ConversationResourceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationResourceService {

    private final ConversationResourceRepository conversationResourceRepository;
    private final ObjectMapper objectMapper;

    public ConversationResourceService(ConversationResourceRepository conversationResourceRepository,
                                       ObjectMapper objectMapper) {
        this.conversationResourceRepository = conversationResourceRepository;
        this.objectMapper = objectMapper;
    }

    public ConversationResourceResponse createResource(String conversationId,
                                                       String resourceType,
                                                       String resourceId,
                                                       String purpose,
                                                       String displayName,
                                                       String mimeType,
                                                       Long sizeBytes,
                                                       Object metadata) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new BusinessException("conversationId is required");
        }
        if (resourceType == null || resourceType.isBlank()) {
            throw new BusinessException("resourceType is required");
        }
        if (resourceId == null || resourceId.isBlank()) {
            throw new BusinessException("resourceId is required");
        }

        LocalDateTime now = LocalDateTime.now();
        ConversationResourceEntity entity = new ConversationResourceEntity();
        entity.setConversationId(conversationId);
        entity.setResourceType(resourceType);
        entity.setResourceId(resourceId);
        entity.setPurpose(purpose);
        entity.setDisplayName(displayName);
        entity.setMimeType(mimeType);
        entity.setSizeBytes(sizeBytes);
        entity.setMetadataJson(toMetadataJson(metadata));
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);

        return toResponse(conversationResourceRepository.save(entity));
    }

    public List<ConversationResourceResponse> listResources(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new BusinessException("conversationId is required");
        }

        return conversationResourceRepository.findByConversationIdOrderByCreatedAtDesc(conversationId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<ConversationResourceEntity> findLatestResource(String conversationId, String resourceType) {
        if (conversationId == null || conversationId.isBlank() || resourceType == null || resourceType.isBlank()) {
            return Optional.empty();
        }

        return conversationResourceRepository.findFirstByConversationIdAndResourceTypeOrderByCreatedAtDesc(conversationId, resourceType);
    }

    private ConversationResourceResponse toResponse(ConversationResourceEntity entity) {
        ConversationResourceResponse response = new ConversationResourceResponse();
        response.setId(entity.getId());
        response.setConversationId(entity.getConversationId());
        response.setResourceType(entity.getResourceType());
        response.setResourceId(entity.getResourceId());
        response.setPurpose(entity.getPurpose());
        response.setDisplayName(entity.getDisplayName());
        response.setMimeType(entity.getMimeType());
        response.setSizeBytes(entity.getSizeBytes());
        response.setMetadata(parseMetadata(entity.getMetadataJson()));
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }

    private String toMetadataJson(Object metadata) {
        if (metadata == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception exception) {
            return String.valueOf(metadata);
        }
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
