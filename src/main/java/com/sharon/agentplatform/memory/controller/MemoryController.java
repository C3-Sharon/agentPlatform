package com.sharon.agentplatform.memory.controller;

import com.sharon.agentplatform.common.ApiResponse;
import com.sharon.agentplatform.memory.dto.AddLongTermMemoryRequest;
import com.sharon.agentplatform.memory.dto.MemoryViewResponse;
import com.sharon.agentplatform.memory.service.MemoryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @GetMapping("/{conversationId}")
    public ApiResponse<MemoryViewResponse> getMemory(
            @PathVariable String conversationId
    ) {
        return ApiResponse.success(
                memoryService.getMemoryView(conversationId)
        );
    }

    @PostMapping("/{conversationId}/long-term")
    public ApiResponse<Void> addLongTermMemory(
            @PathVariable String conversationId,
            @RequestBody AddLongTermMemoryRequest request
    ) {
        memoryService.addLongTermMemory(
                conversationId,
                request.getKey(),
                request.getValue()
        );

        return ApiResponse.success("Long-term memory added", null);
    }

    @DeleteMapping("/{conversationId}")
    public ApiResponse<Void> clearMemory(
            @PathVariable String conversationId
    ) {
        memoryService.clearConversationMemory(conversationId);
        return ApiResponse.success("Memory cleared", null);
    }
}