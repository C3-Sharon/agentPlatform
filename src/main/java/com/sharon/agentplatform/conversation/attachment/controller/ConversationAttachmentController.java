package com.sharon.agentplatform.conversation.attachment.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.conversation.attachment.service.ConversationAttachmentService;
import com.sharon.agentplatform.conversation.resource.dto.ConversationAttachmentUploadResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/conversations/{conversationId}/attachments")
public class ConversationAttachmentController {

    private final ConversationAttachmentService conversationAttachmentService;

    public ConversationAttachmentController(ConversationAttachmentService conversationAttachmentService) {
        this.conversationAttachmentService = conversationAttachmentService;
    }

    @PostMapping
    public ApiResponse<ConversationAttachmentUploadResponse> upload(@PathVariable String conversationId,
                                                                    @RequestParam("file") MultipartFile file,
                                                                    @RequestParam(value = "purpose", required = false) String purpose) {
        return ApiResponse.success(conversationAttachmentService.upload(conversationId, purpose, file));
    }
}
