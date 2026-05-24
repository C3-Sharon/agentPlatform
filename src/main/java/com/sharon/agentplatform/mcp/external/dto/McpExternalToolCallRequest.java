package com.sharon.agentplatform.mcp.external.dto;

import java.util.Map;

public class McpExternalToolCallRequest {
    private Map<String, Object> arguments;
    public Map<String, Object> getArguments() { return arguments; }
    public void setArguments(Map<String, Object> arguments) { this.arguments = arguments; }
}
