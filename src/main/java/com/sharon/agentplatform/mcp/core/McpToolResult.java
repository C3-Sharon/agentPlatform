package com.sharon.agentplatform.mcp.core;

import lombok.Data;

@Data
public class McpToolResult {

    private boolean success;
    private Object content;
    private String errorMessage;

    private McpToolResult(boolean success, Object content, String errorMessage) {
        this.success = success;
        this.content = content;
        this.errorMessage = errorMessage;
    }

    public static McpToolResult success(Object content) {
        return new McpToolResult(true, content, null);
    }

    public static McpToolResult fail(String errorMessage) {
        return new McpToolResult(false, null, errorMessage);
    }
}