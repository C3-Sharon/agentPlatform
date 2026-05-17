package com.sharon.agentplatform.agent.service;

import com.sharon.agentplatform.agent.core.AgentRuntime;
import com.sharon.agentplatform.agent.dto.ChatRequest;
import com.sharon.agentplatform.agent.dto.ChatResponse;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final AgentRuntime agentRuntime;

    public AgentService(AgentRuntime agentRuntime) {
        this.agentRuntime = agentRuntime;
    }

    public ChatResponse chat(ChatRequest request) {
        return agentRuntime.run(request);
    }
}