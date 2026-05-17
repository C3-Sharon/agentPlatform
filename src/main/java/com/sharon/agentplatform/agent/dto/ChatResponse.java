package com.sharon.agentplatform.agent.dto;

import com.sharon.agentplatform.agent.core.AgentTrace;
import lombok.Data;

import java.util.List;
@Data
public class ChatResponse {

    private String conversationId;
    private String answer;
    private String usedModel;
    private List<String> usedSkills;
    private List<AgentTrace> trace;

    public ChatResponse(
            String conversationId,
            String answer,
            String usedModel,
            List<String> usedSkills,
            List<AgentTrace> trace
    ) {
        this.conversationId = conversationId;
        this.answer = answer;
        this.usedModel = usedModel;
        this.usedSkills = usedSkills;
        this.trace = trace;
    }

}
