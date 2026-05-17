package com.sharon.agentplatform.agent.controller;

import com.sharon.agentplatform.agent.dto.ChatRequest;
import com.sharon.agentplatform.agent.dto.ChatResponse;
import com.sharon.agentplatform.agent.service.AgentService;
import com.sharon.agentplatform.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        ChatResponse response = agentService.chat(request);
        return ApiResponse.success(response);
    }
}