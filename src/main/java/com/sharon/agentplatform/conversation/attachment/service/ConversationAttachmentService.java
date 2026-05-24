package com.sharon.agentplatform.conversation.attachment.service;

import com.sharon.agentplatform.common.exception.BusinessException;
import com.sharon.agentplatform.conversation.attachment.core.ConversationAttachmentProcessor;
import com.sharon.agentplatform.conversation.resource.dto.ConversationAttachmentUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ConversationAttachmentService {

    private static final String DEFAULT_PURPOSE = "resume";

    private final List<ConversationAttachmentProcessor> processors;

    public ConversationAttachmentService(List<ConversationAttachmentProcessor> processors) {
        this.processors = processors;
    }

    public ConversationAttachmentUploadResponse upload(String conversationId, String purpose, MultipartFile file) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new BusinessException("conversationId is required");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException("attachment file is required");
        }

        String normalizedPurpose = normalizePurpose(purpose);
        return processors.stream()
                .filter(processor -> processor.supports(normalizedPurpose, file))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Unsupported attachment purpose or file type: " + normalizedPurpose))
                .process(conversationId, normalizedPurpose, file);
    }

    private String normalizePurpose(String purpose) {
        if (purpose == null || purpose.isBlank()) {
            return DEFAULT_PURPOSE;
        }
        return purpose.trim();
    }
}
