package com.sharon.agentplatform.conversation.resource.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.conversation.resource.dto.ConversationResourceResponse;
import com.sharon.agentplatform.conversation.resource.service.ConversationResourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations/{conversationId}/resources")
public class ConversationResourceController {

    private final ConversationResourceService conversationResourceService;

    public ConversationResourceController(ConversationResourceService conversationResourceService) {
        this.conversationResourceService = conversationResourceService;
    }

    @GetMapping
    public ApiResponse<List<ConversationResourceResponse>> listResources(@PathVariable String conversationId) {
        return ApiResponse.success(conversationResourceService.listResources(conversationId));
    }
}
