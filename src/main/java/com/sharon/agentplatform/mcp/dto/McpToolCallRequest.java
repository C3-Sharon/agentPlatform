package com.sharon.agentplatform.mcp.dto;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
@Data
public class McpToolCallRequest {

    private String toolName;
    private Map<String, Object> arguments = new HashMap<>();

    public String getToolName() {
        return toolName;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

}