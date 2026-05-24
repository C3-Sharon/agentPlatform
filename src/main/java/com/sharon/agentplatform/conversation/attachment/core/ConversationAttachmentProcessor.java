package com.sharon.agentplatform.conversation.attachment.core;

import com.sharon.agentplatform.conversation.resource.dto.ConversationAttachmentUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ConversationAttachmentProcessor {

    boolean supports(String purpose, MultipartFile file);

    ConversationAttachmentUploadResponse process(String conversationId, String purpose, MultipartFile file);
}
